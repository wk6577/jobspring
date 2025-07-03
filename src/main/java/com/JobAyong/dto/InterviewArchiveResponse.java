package com.JobAyong.dto;

import com.JobAyong.constant.InterviewQuestionType;
import com.JobAyong.constant.InterviewStatus;
import com.JobAyong.entity.InterviewArchive;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InterviewArchiveResponse {
    private Integer interviewArchiveId;
    private String email;
    private String company;
    private String archiveName;
    private InterviewQuestionType archiveMode;
    private String position;
    private InterviewStatus status;
    private LocalDateTime createdAt;
    private Integer evalScore;

    public static InterviewArchiveResponse fromEntity(InterviewArchive entity) {
        Integer evalScore = null;
        if (entity.getInterviewEval() != null) {
            evalScore = entity.getInterviewEval().getEval_score();
        }

        return InterviewArchiveResponse.builder()
                .interviewArchiveId(entity.getInterviewArchiveId())
                .email(entity.getUser().getEmail())
                .company(entity.getCompany())
                .archiveName(entity.getArchive_name())
                .archiveMode(entity.getMode())
                .position(entity.getPosition())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .evalScore(evalScore)
                .build();
    }
}