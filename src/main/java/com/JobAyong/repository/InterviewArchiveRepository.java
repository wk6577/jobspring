package com.JobAyong.repository;

import com.JobAyong.entity.InterviewArchive;
import com.JobAyong.entity.User;
import com.JobAyong.entity.Company;
import com.JobAyong.constant.InterviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InterviewArchiveRepository extends JpaRepository<InterviewArchive, Integer> {
    List<InterviewArchive> findByUser(User user);
    List<InterviewArchive> findByUserEmail(String email);
    List<InterviewArchive> findByCompany(Company company);
    List<InterviewArchive> findByStatus(InterviewStatus status);
    
    // 휴지통 기능을 위한 메서드들
    List<InterviewArchive> findByUserEmailAndDeletedAtIsNull(String email); // 삭제되지 않은 것만
    List<InterviewArchive> findByUserEmailAndDeletedAtIsNotNull(String email); // 삭제된 것만 (휴지통)
    List<InterviewArchive> findByUserEmailAndDeletedAtIsNullAndStatus(String email, InterviewStatus status);

    // InterviewEval을 함께 fetch하는 메서드 추가
    @Query("SELECT ia FROM InterviewArchive ia LEFT JOIN FETCH ia.interviewEval WHERE ia.user.email = :email AND ia.deletedAt IS NULL AND ia.status = :status")
    List<InterviewArchive> findByUserEmailAndDeletedAtIsNullAndStatusWithEval(@Param("email") String email, @Param("status") InterviewStatus status);

    int countByCreatedAtAfterAndDeletedAtIsNull(LocalDateTime dateTime);
} 