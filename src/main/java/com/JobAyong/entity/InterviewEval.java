package com.JobAyong.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "interview_eval")
@Getter
@Setter
public class InterviewEval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interview_eval_id")
    private Integer interviewEvalId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_archive_id", nullable = false)
    private InterviewArchive interviewArchive;

    @Column(name = "eval_score")
    private int eval_score;

    @Column(columnDefinition = "eval_reason TEXT NULL")
    private String eval_reason;

    @Column(columnDefinition = "eval_bad_summary TEXT NULL")
    private String eval_bad_summary;

    @Column(columnDefinition = "eval_bad_description TEXT NULL")
    private String eval_bad_description;

    @Column(columnDefinition = "eval_good_summary TEXT NULL")
    private String eval_good_summary;

    @Column(columnDefinition = "eval_good_description TEXT NULL")
    private String eval_good_description;

    @Column(columnDefinition = "eval_state TEXT NULL")
    private String eval_state;

    @Column(columnDefinition = "eval_cause TEXT NULL")
    private String eval_cause;

    @Column(columnDefinition = "eval_solution TEXT NULL")
    private String eval_solution;

    @Column(columnDefinition = "eval_improvment TEXT NULL")
    private String eval_improvment;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

