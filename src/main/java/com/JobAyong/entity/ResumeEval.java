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

    @Column(name = "resume_eval_title")
    private String resumeEvalTitle;

    @Column(name = "resume_org", nullable = false, columnDefinition = "TEXT")
    private String resumeOrg;

    @Column(name = "resume_imp", nullable = false, columnDefinition = "TEXT")
    private String resumeImp;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "missing_areas", columnDefinition = "TEXT")
    private String missingAreas;

    @Column(name = "resume_fin", nullable = false, columnDefinition = "TEXT")
    private String resumeFin;

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
