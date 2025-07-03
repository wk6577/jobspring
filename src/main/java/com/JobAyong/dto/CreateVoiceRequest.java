package com.JobAyong.dto;

import lombok.Data;
import java.util.Map;
import java.util.List;

@Data
public class CreateVoiceRequest {

    // voice테이블 .webm .wav 파일 저장용
    private String email;
    private String fileName;
    private String fileType;
    private int fileSize;
    private String filePath;
    private String convertedFilePath;
    private String wavBinaryBase64;  // base64로 인코딩된 WAV 바이너리 데이터

    private String transcript;
}

