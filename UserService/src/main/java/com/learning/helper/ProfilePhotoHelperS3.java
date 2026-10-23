package com.learning.helper;

import com.learning.entity.UserProfilePhotoEntity;
import com.learning.exceptions.UserServiceExceptions;
import com.learning.model.UserProfilePhoto;
import com.learning.repository.UserProfilePhotoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

@Component
@Primary
public class ProfilePhotoHelperS3 implements ProfilePhotoHelper{

    private static final long MAX_PHOTO_SIZE_BYTES = 1024L * 1024L;
    private static final String PHOTO_PREFIX = "profile-photos/";

    private final UserProfilePhotoRepository profilePhotoRepository;
    private final S3Client s3Client;
    private final String bucketName;


    public ProfilePhotoHelperS3(UserProfilePhotoRepository profilePhotoRepository,
                                S3Client s3Client,
                                @Value("${user.profile-photo.bucket}") String bucketName) {
        this.profilePhotoRepository = profilePhotoRepository;
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @Override
    public void saveProfilePhoto(String userId, MultipartFile photo) {
        validatePhoto(photo);

        String contentType = photo.getContentType().toLowerCase(Locale.ROOT);
        String extension = extensionFor(contentType);
        String safeUserId = userId.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_");
        String imageKey = PHOTO_PREFIX + safeUserId + "_" + System.currentTimeMillis() + extension;

        boolean mappingSaved = false;

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(imageKey)
                    .contentType(contentType)
                    .build();

            try (var inputStream = photo.getInputStream()) {
                s3Client.putObject(
                        putRequest,
                        RequestBody.fromInputStream(inputStream, photo.getSize()));
            }

            UserProfilePhotoEntity mapping = profilePhotoRepository.findById(userId)
                    .orElseGet(UserProfilePhotoEntity::new);
            String previousImageKey = mapping.getImagePath();

            mapping.setUserId(userId);
            mapping.setImagePath(imageKey);
            profilePhotoRepository.save(mapping);
            mappingSaved = true;

            deletePreviousPhoto(previousImageKey, imageKey);
        } catch (IOException | RuntimeException ex) {
            // If the database update failed, clean up the newly uploaded object.
            if (!mappingSaved) {
                deletePhotoQuietly(imageKey);
            }
            throw new UserServiceExceptions("Could not save profile photo");
        }
    }

    @Override
    public Optional<UserProfilePhoto> getProfilePhoto(String userId) {
        return profilePhotoRepository.findById(userId)
                .flatMap(mapping -> readProfilePhoto(mapping.getImagePath()));
    }

    private void validatePhoto(MultipartFile photo) {
        if (photo == null || photo.isEmpty()) {
            throw new UserServiceExceptions("A photo is required");
        }
        if (photo.getSize() > MAX_PHOTO_SIZE_BYTES) {
            throw new UserServiceExceptions("Photo must be 1 MB or smaller");
        }
        if (photo.getContentType() == null) {
            throw new UserServiceExceptions("Photo must be a JPG, PNG, or WebP image");
        }
    }

    private String extensionFor(String contentType) {
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new UserServiceExceptions(
                    "Photo must be a JPG, PNG, or WebP image");
        };
    }

    private Optional<UserProfilePhoto> readProfilePhoto(String imageKey) {
        if (imageKey == null || !imageKey.startsWith(PHOTO_PREFIX)) {
            return Optional.empty();
        }

        try {
            ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(
                    GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(imageKey)
                            .build());

            String contentType = response.response().contentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                contentType = contentTypeFor(imageKey);
            }

            return Optional.of(new UserProfilePhoto(response.asByteArray(), contentType));
        } catch (RuntimeException ex) {
            // Missing objects or S3 errors should leave the default photo in use.
            return Optional.empty();
        }
    }

    private String contentTypeFor(String imageKey) {
        String lowerKey = imageKey.toLowerCase(Locale.ROOT);
        if (lowerKey.endsWith(".jpg") || lowerKey.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lowerKey.endsWith(".png")) {
            return "image/png";
        }
        if (lowerKey.endsWith(".webp")) {
            return "image/webp";
        }
        return "application/octet-stream";
    }

    private void deletePreviousPhoto(String previousImageKey, String currentImageKey) {
        if (previousImageKey != null
                && previousImageKey.startsWith(PHOTO_PREFIX)
                && !previousImageKey.equals(currentImageKey)) {
            deletePhotoQuietly(previousImageKey);
        }
    }

    private void deletePhotoQuietly(String imageKey) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(imageKey)
                    .build());
        } catch (RuntimeException ignored) {
            // A stale object should not make the profile update fail.
        }
    }
}