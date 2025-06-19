package com.JobAyong.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResumeEvalRequest {
    private Integer resumeId;            // 평가할 이력서 ID
    private String userEmail;            // 평가자 이메일
    private String resumeEvalComment;    // 평가 코멘트 (nullable)
    private String resumeOrg;            // 평가 받는 텍스트
    private String resumeLog;            // AI가 수정해준 부분에서 사용자가 커밋한 텍스트
    private Integer resumeEvalVersion;   // 평가 버전 (default 1)
}
