package com.learning.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Set;

import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.FetchType.EAGER;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userId;
    private String username;

    @Column(nullable = false, unique = true)
    private String email;
    private String contactNumber;
    private String password;
    private String address;

    @Enumerated(EnumType.STRING)
    private UserType userType;

    private boolean isOtpValidated;
    private boolean isActive;

    @ManyToMany(fetch = EAGER, cascade = {PERSIST})
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"), // FK to UserEntity.id PK
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id") // FK to RoleEntity.id PK
    )
    private Set<RoleEntity> roles;

    private String otp;
}
