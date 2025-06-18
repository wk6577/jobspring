package com.JobAyong.repository;

import com.JobAyong.entity.InterviewAnswer;
import com.JobAyong.entity.InterviewArchive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewAnswerRepository extends JpaRepository<InterviewAnswer, Integer> {
    /**
     * 인터뷰 아카이브에 속한 모든 답변을 조회합니다.
     * @param interviewArchive 인터뷰 아카이브
     * @return 답변 목록
     */
    List<InterviewAnswer> findAllByInterviewArchive(InterviewArchive interviewArchive);
    
    /**
     * 인터뷰 아카이브에 속한 모든 답변을 삭제합니다.
     * @param interviewArchive 인터뷰 아카이브
     */
    void deleteAllByInterviewArchive(InterviewArchive interviewArchive);
}
