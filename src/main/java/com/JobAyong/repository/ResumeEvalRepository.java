package com.JobAyong.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.JobAyong.entity.ResumeEval;

@Repository
public interface ResumeEvalRepository extends JpaRepository<ResumeEval, Integer> {


}
