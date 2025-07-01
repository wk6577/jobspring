package com.JobAyong.repository;

import com.JobAyong.entity.Company;
import com.JobAyong.entity.VoiceEval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoiceEvalRepository extends JpaRepository<VoiceEval, Integer> {
    Optional<VoiceEval> findByEvalId(int evalId);
}
