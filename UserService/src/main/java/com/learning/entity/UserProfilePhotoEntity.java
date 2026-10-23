package com.learning.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_profile_photo")
@Getter
@Setter
@NoArgsConstructor
public class UserProfilePhotoEntity {

    @Id
    @Column(name = "user_id", nullable = false, length = 255)
    private String userId;

    @Column(name = "image_path", nullable = false, length = 2048)
    private String imagePath;
}
