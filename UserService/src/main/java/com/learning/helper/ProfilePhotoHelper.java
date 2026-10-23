package com.learning.helper;

import com.learning.entity.UserProfilePhotoEntity;
import com.learning.exceptions.UserServiceExceptions;
import com.learning.model.UserProfilePhoto;
import com.learning.repository.UserProfilePhotoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@Component
public class ProfilePhotoHelper {

    private static final long MAX_PHOTO_SIZE_BYTES = 1024L * 1024L;

    private final UserProfilePhotoRepository profilePhotoRepository;
    private final String storageDirectory;

    public ProfilePhotoHelper(
            UserProfilePhotoRepository profilePhotoRepository,
            @Value("${user.profile-photo.storage-dir:${java.io.tmpdir}/learning-profile-photos}") String storageDirectory) {
        this.profilePhotoRepository = profilePhotoRepository;
        this.storageDirectory = storageDirectory;
    }

    public void saveProfilePhoto(String userId, MultipartFile photo) {
        validatePhoto(photo);

        String extension = extensionFor(photo.getContentType());
        Path directory = Paths.get(storageDirectory).toAbsolutePath().normalize();
        String safeUserId = userId.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_");
        String imageId = safeUserId + "_" + System.currentTimeMillis();
        Path imagePath = directory.resolve(imageId + extension).normalize();
        if (!imagePath.startsWith(directory)) {
            throw new UserServiceExceptions("Invalid user ID for photo path");
        }

        try {
            Files.createDirectories(directory);
            try (InputStream photoStream = photo.getInputStream()) {
                Files.copy(photoStream, imagePath, StandardCopyOption.REPLACE_EXISTING);
            }

            UserProfilePhotoEntity mapping = profilePhotoRepository.findById(userId)
                    .orElseGet(UserProfilePhotoEntity::new);
            String previousImagePath = mapping.getImagePath();
            mapping.setUserId(userId);
            mapping.setImagePath(imagePath.toString());
            profilePhotoRepository.save(mapping);

            if (previousImagePath != null && !previousImagePath.equals(imagePath.toString())) {
                deleteManagedPhoto(directory, previousImagePath);
            }
        } catch (IOException | RuntimeException ex) {
            try {
                Files.deleteIfExists(imagePath);
            } catch (IOException ignored) {
                // Preserve the original storage error.
            }
            throw new UserServiceExceptions("Could not save profile photo");
        }
    }

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
    }

    private String extensionFor(String contentType) {
        return switch (contentType == null ? "" : contentType.toLowerCase()) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new UserServiceExceptions("Photo must be a JPG, PNG, or WebP image");
        };
    }

    private Optional<UserProfilePhoto> readProfilePhoto(String imagePath) {
        try {
            Path path = Paths.get(imagePath);
            if (!Files.isRegularFile(path)) {
                return Optional.empty();
            }
            String fileName = path.getFileName().toString().toLowerCase();
            String contentType;
            if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (fileName.endsWith(".png")) {
                contentType = "image/png";
            } else if (fileName.endsWith(".webp")) {
                contentType = "image/webp";
            } else {
                return Optional.empty();
            }
            return Optional.of(new UserProfilePhoto(Files.readAllBytes(path), contentType));
        } catch (IOException | RuntimeException ex) {
            return Optional.empty();
        }
    }

    private void deleteManagedPhoto(Path directory, String previousImagePath) {
        try {
            Path previousPath = Paths.get(previousImagePath).toAbsolutePath().normalize();
            if (previousPath.startsWith(directory)) {
                Files.deleteIfExists(previousPath);
            }
        } catch (IOException | RuntimeException ignored) {
            // A stale prior photo should not make the new photo update fail.
        }
    }
}
