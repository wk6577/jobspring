package com.JobAyong.dto;

import lombok.Data;
import java.util.Map;
import java.util.List;

@Data
public class CreateVoiceEvalRequest {
    private int voiceId;
    private String transcript;

    private int overallScore;
    private int clarityScore;
    private int speedScore;
    private int volumeScore;
    private int confidenceScore;

    private int wordsPerMinute;
    private float pauseDuration;
    private int intonation;
    private int pronunciation;
    private int fillersCount;

    private Map<String, Object> metricGradesJson;
    private Map<String, Object> voicePatternsJson;

    private List<Map<String, Object>> strengthsJson;
    private List<Map<String, Object>> improvementsJson;
    private List<Map<String, Object>> strategiesJson;

    private String interviewerComment;
}
