package com.JobAyong.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.JobAyong.entity.ResumeEval;
import java.util.List;

@Repository
public interface ResumeEvalRepository extends JpaRepository<ResumeEval, Integer> {

    // 특정 resume_id의 최대 버전 조회
    @Query("SELECT COALESCE(MAX(re.resumeEvalVersion), 0) FROM ResumeEval re WHERE re.resume.resumeId = :resumeId AND re.deletedAt IS NULL")
    Integer findMaxVersionByResumeId(@Param("resumeId") Integer resumeId);

    // 사용자 이메일로 자소서 평가 목록 조회 (삭제되지 않은 것만)
    @Query("SELECT re FROM ResumeEval re WHERE re.user.email = :email AND re.deletedAt IS NULL ORDER BY re.createdAt DESC")
    List<ResumeEval> findByUserEmailAndDeletedAtIsNull(@Param("email") String email);

    // 사용자 이메일로 삭제된 자소서 평가 목록 조회 (휴지통용)
    @Query("SELECT re FROM ResumeEval re WHERE re.user.email = :email AND re.deletedAt IS NOT NULL ORDER BY re.deletedAt DESC")
    List<ResumeEval> findByUserEmailAndDeletedAtIsNotNull(@Param("email") String email);

    // 사용자 이메일로 각 자소서별 최신 버전의 평가만 조회 (삭제되지 않은 것만)
    @Query("SELECT re FROM ResumeEval re WHERE re.user.email = :email AND re.deletedAt IS NULL " +
           "AND re.resumeEvalVersion = (SELECT MAX(re2.resumeEvalVersion) FROM ResumeEval re2 " +
           "WHERE re2.resume.resumeId = re.resume.resumeId AND re2.deletedAt IS NULL) " +
           "ORDER BY re.createdAt DESC")
    List<ResumeEval> findLatestVersionByUserEmailAndDeletedAtIsNull(@Param("email") String email);

    // 사용자 이메일로 각 자소서별 최신 버전의 삭제된 평가만 조회 (휴지통용)
    @Query("SELECT re FROM ResumeEval re WHERE re.user.email = :email AND re.deletedAt IS NOT NULL " +
           "AND re.resumeEvalVersion = (SELECT MAX(re2.resumeEvalVersion) FROM ResumeEval re2 " +
           "WHERE re2.resume.resumeId = re.resume.resumeId AND re2.deletedAt IS NOT NULL) " +
           "ORDER BY re.deletedAt DESC")
    List<ResumeEval> findLatestVersionByUserEmailAndDeletedAtIsNotNull(@Param("email") String email);

    // 특정 자소서의 모든 버전을 소프트 삭제 (같은 resume_id의 모든 버전)
    @Modifying
    @Transactional
    @Query("UPDATE ResumeEval re SET re.deletedAt = :deletedAt WHERE re.resume.resumeId = :resumeId AND re.deletedAt IS NULL")
    void softDeleteAllVersionsByResumeId(@Param("resumeId") Integer resumeId, @Param("deletedAt") java.time.LocalDateTime deletedAt);

    // 특정 자소서의 모든 버전을 복구 (같은 resume_id의 모든 버전)
    @Modifying
    @Transactional
    @Query("UPDATE ResumeEval re SET re.deletedAt = NULL WHERE re.resume.resumeId = :resumeId AND re.deletedAt IS NOT NULL")
    void restoreAllVersionsByResumeId(@Param("resumeId") Integer resumeId);

    // 특정 자소서의 모든 버전을 완전 삭제 (같은 resume_id의 모든 버전)
    @Modifying
    @Transactional
    @Query("DELETE FROM ResumeEval re WHERE re.resume.resumeId = :resumeId")
    void deleteAllVersionsByResumeId(@Param("resumeId") Integer resumeId);

    // 특정 자소서의 모든 버전 조회 (삭제되지 않은 것만, 버전 순으로 정렬)
    @Query("SELECT re FROM ResumeEval re WHERE re.resume.resumeId = :resumeId AND re.deletedAt IS NULL ORDER BY re.resumeEvalVersion DESC")
    List<ResumeEval> findAllVersionsByResumeId(@Param("resumeId") Integer resumeId);

}
