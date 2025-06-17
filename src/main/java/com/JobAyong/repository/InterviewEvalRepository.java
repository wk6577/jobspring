package com.JobAyong.repository;

import com.JobAyong.entity.InterviewArchive;
import com.JobAyong.entity.InterviewEval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InterviewEvalRepository extends JpaRepository<InterviewEval, Integer> {
    /**
     * 인터뷰 아카이브 ID로 평가 정보를 조회합니다.
     * @param interviewArchiveId 인터뷰 아카이브 ID
     * @return 평가 정보
     */
    InterviewEval findByInterviewArchiveInterviewArchiveId(Integer interviewArchiveId);
    
    /**
     * 인터뷰 아카이브 ID로 평가 정보를 삭제합니다.
     * @param interviewArchiveId 인터뷰 아카이브 ID
     */
    void deleteByInterviewArchiveInterviewArchiveId(Integer interviewArchiveId);

    Optional<InterviewEval> findByInterviewArchive(InterviewArchive interviewArchive);
}
