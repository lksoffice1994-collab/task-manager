package com.learning.helper;

import com.learning.model.UserProfilePhoto;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface ProfilePhotoHelper {

    void saveProfilePhoto(String userId, MultipartFile photo);

    Optional<UserProfilePhoto> getProfilePhoto(String userId);

}
