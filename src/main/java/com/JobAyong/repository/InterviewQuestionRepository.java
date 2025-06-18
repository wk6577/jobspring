package com.JobAyong.repository;

import com.JobAyong.entity.InterviewQuestion;
import com.JobAyong.entity.InterviewArchive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Integer> {
    /**
     * 인터뷰 아카이브에 속한 모든 질문을 조회합니다.
     * @param interviewArchive 인터뷰 아카이브
     * @return 질문 목록
     */
    List<InterviewQuestion> findAllByInterviewArchive(InterviewArchive interviewArchive);
    
    /**
     * 인터뷰 아카이브에 속한 모든 질문을 삭제합니다.
     * @param interviewArchive 인터뷰 아카이브
     */
    void deleteAllByInterviewArchive(InterviewArchive interviewArchive);
}
