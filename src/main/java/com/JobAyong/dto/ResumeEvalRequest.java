package com.JobAyong.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResumeEvalRequest {
    private Integer resumeId;            // 평가할 이력서 ID
    private String resumeOrg;            // 평가 받는 원본 텍스트
    private String resumeImp;            // AI가 개선해준 자소서
    private String reason;               // 개선 이유
    private String missingAreas;         // 부족한 영역 JSON 데이터
    private String resumeFin;            // 수정 저장할 최종 자소서
    private Integer resumeEvalVersion;   // 평가 버전 (default 1)
    private String resumeEvalTitle;      // 평가 제목
}
