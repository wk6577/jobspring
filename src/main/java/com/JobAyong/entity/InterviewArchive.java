package com.JobAyong.entity;

import com.JobAyong.constant.InterviewQuestionType;
import com.JobAyong.constant.InterviewStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "interview_archive")
@Getter
@Setter
public class InterviewArchive {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interview_archive_id")
    private Integer interviewArchiveId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(name = "archive_mode")
    private InterviewQuestionType mode;

    @Column(name = "archive_name")
    private String archive_name;

    @Column(name = "position", length = 100)
    private String position;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private InterviewStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "interviewArchive", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<InterviewQuestion> interviewQuestions = new ArrayList<>();

    @OneToOne(mappedBy = "interviewArchive", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private InterviewEval interviewEval;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 