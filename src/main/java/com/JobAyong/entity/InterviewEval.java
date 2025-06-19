package com.JobAyong.entity;

import com.JobAyong.constant.InterviewQuestionType;
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
    @JoinColumn(name = "interview_archive_id", nullable = false, unique = true)
    private InterviewArchive interviewArchive;

    @Enumerated(EnumType.STRING)
    @Column(name = "eval_mode")
    private InterviewQuestionType mode;

    @Column(name = "eval_score")
    private int eval_score;

    @Column(columnDefinition = " TEXT")
    private String eval_reason;

    @Column(columnDefinition = "TEXT")
    private String eval_bad_summary;

    @Column(columnDefinition = "TEXT")
    private String eval_bad_description;

    @Column(columnDefinition = "TEXT")
    private String eval_good_summary;

    @Column(columnDefinition = "TEXT")
    private String eval_good_description;

    @Column(columnDefinition = "TEXT")
    private String eval_state;

    @Column(columnDefinition = "TEXT")
    private String eval_cause;

    @Column(columnDefinition = "TEXT")
    private String eval_solution;

    @Column(columnDefinition = "TEXT")
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

