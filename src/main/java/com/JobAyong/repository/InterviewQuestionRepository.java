package com.JobAyong.repository;

import com.JobAyong.entity.InterviewArchive;
import com.JobAyong.entity.InterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Integer> {
    List<InterviewQuestion> findAllByInterviewArchive(InterviewArchive interviewArchive);
}
