package com.JobAyong.repository;

import com.JobAyong.entity.InterviewArchive;
import com.JobAyong.entity.User;
import com.JobAyong.entity.Company;
import com.JobAyong.constant.InterviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewArchiveRepository extends JpaRepository<InterviewArchive, Integer> {
    List<InterviewArchive> findByUser(User user);
    List<InterviewArchive> findByUserEmail(String email);
    List<InterviewArchive> findByCompany(Company company);
    List<InterviewArchive> findByStatus(InterviewStatus status);
    
    // 휴지통 기능을 위한 메서드들
    List<InterviewArchive> findByUserEmailAndDeletedAtIsNull(String email); // 삭제되지 않은 것만
    List<InterviewArchive> findByUserEmailAndDeletedAtIsNotNull(String email); // 삭제된 것만 (휴지통)

    int countByCreatedAtAfterAndDeletedAtIsNull(LocalDateTime dateTime);
} 