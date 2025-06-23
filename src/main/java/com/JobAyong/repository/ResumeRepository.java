package com.JobAyong.repository;

import com.JobAyong.entity.Resume;
import com.JobAyong.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Integer> {
    List<Resume> findByUser(User user);
    List<Resume> findByUserEmail(String email);
    
    // 삭제되지 않은 자기소개서 목록 조회 (deleted_at이 null인 것만)
    @Query("SELECT r FROM Resume r WHERE r.user.email = :email AND r.deletedAt IS NULL ORDER BY r.createdAt DESC")
    List<Resume> findActiveResumesByUserEmail(@Param("email") String email);
    
    // 휴지통의 자기소개서 목록 조회 (deleted_at이 null이 아닌 것만)
    @Query("SELECT r FROM Resume r WHERE r.user.email = :email AND r.deletedAt IS NOT NULL ORDER BY r.deletedAt DESC")
    List<Resume> findDeletedResumesByUserEmail(@Param("email") String email);
    
    // 삭제되지 않은 특정 자기소개서 조회
    @Query("SELECT r FROM Resume r WHERE r.resumeId = :id AND r.deletedAt IS NULL")
    Optional<Resume> findActiveResumeById(@Param("id") Integer id);
    
    // 휴지통의 특정 자기소개서 조회
    @Query("SELECT r FROM Resume r WHERE r.resumeId = :id AND r.deletedAt IS NOT NULL")
    Optional<Resume> findDeletedResumeById(@Param("id") Integer id);
} 