package com.JobAyong.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResumeRequest {
    private String userEmail;         // 작성자 이메일
    private String resumeTitle;       // 이력서 제목
    private String resumeText;        // 이력서 본문(텍스트)
    private String resumeType;        // 'text' 또는 'file'
    // 필요한 필드가 더 있다면 여기에 추가
}