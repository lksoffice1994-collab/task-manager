package com.learning.repository;

import com.learning.entity.UserProfilePhotoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfilePhotoRepository extends JpaRepository<UserProfilePhotoEntity, String> {
}
