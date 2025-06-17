package com.JobAyong.entity;

import com.JobAyong.constant.InterviewQuestionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "interview_question")
@Getter
@Setter
public class InterviewQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interview_question_id")
    private Integer interviewQuestionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_archive_id", nullable = false)
    private InterviewArchive interviewArchive;

    @Enumerated(EnumType.STRING)
    @Column(name = "interview_question_type", columnDefinition = "ENUM('GENERAL','PRESSURE','PERSONALITY','TECHNICAL','SITUATIONAL')")
    private InterviewQuestionType interview_question_type;

    @Column(name = "interview_question")
    private String interview_question;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

