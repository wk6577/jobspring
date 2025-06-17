package com.JobAyong.repository;

import com.JobAyong.entity.InterviewEval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InterviewEvalRepository extends JpaRepository<InterviewEval, Integer> {
}
