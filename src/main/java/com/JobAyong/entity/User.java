package com.JobAyong.entity;

import com.JobAyong.constant.Gender;
import com.JobAyong.constant.UserRole;
import com.JobAyong.constant.UserStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Getter
@Setter
public class User {
    @Id
    @Column(name = "email")
    private String email;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "birth", nullable = false)
    private LocalDate birth;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Lob
    @Column(name = "profile_image", columnDefinition = "LONGBLOB")
    private byte[] profileImage;

    @Column(name = "original_filename")
    private String originalFilename;

    @Column(name = "job")
    private String job;

    @Column(name = "company")
    private String company;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UserRole getUserRole() {
        return UserRole.fromString(role);
    }

    public void setUserRole(UserRole userRole) {
        this.role = userRole.getValue();
    }

    public UserStatus getUserStatus() {
        return UserStatus.fromString(status);
    }

    public void setUserStatus(UserStatus userStatus) {
        this.status = userStatus.getValue();
    }
}