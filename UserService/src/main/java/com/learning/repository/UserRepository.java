package com.learning.repository;

import com.learning.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    // Add any custom query methods if needed

    UserEntity findByUsernameAndPassword(String username, String password);
    UserEntity findByEmailAndPassword(String email, String password);

    UserEntity findByEmailOrUserId(String email, String userId);

    UserEntity findByEmail(String email);

    UserEntity findByUserId(String userId);
}
