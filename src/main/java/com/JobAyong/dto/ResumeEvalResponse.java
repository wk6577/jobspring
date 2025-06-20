package com.JobAyong.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResumeEvalResponse {
    private Integer resumeEvalId;        // 평가 PK
    private Integer resumeId;            // 이력서 ID
    private String userEmail;            // 평가자 이메일
    private String resumeEvalComment;    // 평가 코멘트
    private String resumeOrg;            // 평가 받는 텍스트
    private String resumeLog;            // AI가 수정해준 부분에서 사용자가 커밋한 텍스트
    private Integer resumeEvalVersion;   // 평가 버전
    private LocalDateTime createdAt;     // 생성일
    private LocalDateTime deletedAt;     // 삭제일(soft delete)
}
