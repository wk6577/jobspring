package com.JobAyong.repository;

import com.JobAyong.entity.InterviewArchive;
import com.JobAyong.entity.User;
import com.JobAyong.entity.Company;
import com.JobAyong.constant.InterviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewArchiveRepository extends JpaRepository<InterviewArchive, Integer> {
    List<InterviewArchive> findByUser(User user);
    List<InterviewArchive> findByUserEmail(String email);
    List<InterviewArchive> findByCompany(Company company);
    List<InterviewArchive> findByStatus(InterviewStatus status);
} 