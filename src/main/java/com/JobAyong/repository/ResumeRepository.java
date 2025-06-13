package com.JobAyong.repository;

import com.JobAyong.entity.Resume;
import com.JobAyong.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findByUser(User user);
    List<Resume> findByUserEmail(String email);
} 