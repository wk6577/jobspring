package com.JobAyong.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.JobAyong.entity.ResumeEval;

@Repository
public interface ResumeEvalRepository extends JpaRepository<ResumeEval, Integer> {

    // 특정 resume_id의 최대 버전 조회
    @Query("SELECT COALESCE(MAX(re.resumeEvalVersion), 0) FROM ResumeEval re WHERE re.resume.resumeId = :resumeId AND re.deletedAt IS NULL")
    Integer findMaxVersionByResumeId(@Param("resumeId") Integer resumeId);

}
