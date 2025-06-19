package com.JobAyong.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "resume_eval")
@Getter
@Setter
public class ResumeEval {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resume_eval_id")
    private Integer resumeEvalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email", nullable = false)
    private User user;

    @Column(name = "resume_eval_comment", nullable = false)
    private String resumeEvalComment;

    @Column(name = "resume_org", nullable = false, columnDefinition = "TEXT")
    private String resumeOrg;

    @Column(name = "resume_log", nullable = false, columnDefinition = "TEXT")
    private String resumeLog;

    @Column(name = "resume_eval_version", nullable = false, columnDefinition = "int default 1")
    private Integer resumeEvalVersion;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
