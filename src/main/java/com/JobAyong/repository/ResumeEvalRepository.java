package com.JobAyong.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

}
