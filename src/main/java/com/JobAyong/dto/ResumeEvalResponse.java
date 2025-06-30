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
    private String resumeEvalTitle;      // 평가 제목
    private String resumeOrg;            // 평가 받는 원본 텍스트
    private String resumeImp;            // AI가 개선해준 자소서
    private String reason;               // 개선 이유
    private String missingAreas;         // 부족한 영역 JSON 데이터
    private String resumeFin;            // 수정 저장할 최종 자소서
    private Integer resumeEvalVersion;   // 평가 버전
    private LocalDateTime createdAt;     // 생성일
    private LocalDateTime deletedAt;     // 삭제일(soft delete)
}
