package com.JobAyong.repository;

import com.JobAyong.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.time.LocalDateTime;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByStatus(String status);
    long countByCreatedAtAfterAndDeletedAtIsNull(LocalDateTime date);

    // 탈퇴하지 않은 사용자만 조회
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<User> findByEmailAndNotDeleted(@Param("email") String email);
    
    // 탈퇴한 사용자인지 확인
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NOT NULL")
    Optional<User> findByEmailAndDeleted(@Param("email") String email);
    
    // 탈퇴하지 않은 사용자 존재 여부 확인
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    boolean existsByEmailAndNotDeleted(@Param("email") String email);
} 