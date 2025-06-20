package com.JobAyong.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResumeResponse {
    private Integer resumeId;         // 이력서 PK
    private String userEmail;         // 작성자 이메일
    private String resumeTitle;       // 제목
    private String resumeText;        // 본문
    private String resumeType;        // 'text'만 사용
    private LocalDateTime createdAt;  // 생성일
    private LocalDateTime updatedAt;  // 수정일
    private LocalDateTime deletedAt;  // 삭제일(soft delete)
}
