package com.JobAyong.dto;

import com.JobAyong.entity.Voice;
import com.JobAyong.entity.VoiceEval;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class VoiceArchiveResponse {
    private Integer voiceId;
    private String email;
    private String fileName;
    private String transcriptText;
    private LocalDateTime createdAt;
    
    // 평가 관련 필드
    private Integer overallScore;
    private Integer clarityScore;
    private Integer speedScore;
    private Integer volumeScore;
    private Integer confidenceScore;
    private Integer wordsPerMinute;
    private Float pauseDuration;
    private String metricGradesJson;
    private String voicePatternsJson;
    private String strengthsJson;
    private String improvementsJson;
    private String strategiesJson;
    private String interviewerComment;

    public static VoiceArchiveResponse from(Voice voice, VoiceEval eval) {
        return VoiceArchiveResponse.builder()
                .voiceId(voice.getVoiceId())
                .email(voice.getUser().getEmail())
                .fileName(voice.getFileName())
                .transcriptText(voice.getTranscriptText())
                .createdAt(voice.getCreatedAt())
                .overallScore(eval != null ? eval.getOverallScore() : null)
                .clarityScore(eval != null ? eval.getClarityScore() : null)
                .speedScore(eval != null ? eval.getSpeedScore() : null)
                .volumeScore(eval != null ? eval.getVolumeScore() : null)
                .confidenceScore(eval != null ? eval.getConfidenceScore() : null)
                .wordsPerMinute(eval != null ? eval.getWordsPerMinute() : null)
                .pauseDuration(eval != null ? eval.getPauseDuration() : null)
                .metricGradesJson(eval != null ? eval.getMetricGradesJson() : null)
                .voicePatternsJson(eval != null ? eval.getVoicePatternsJson() : null)
                .strengthsJson(eval != null ? eval.getStrengthsJson() : null)
                .improvementsJson(eval != null ? eval.getImprovementsJson() : null)
                .strategiesJson(eval != null ? eval.getStrategiesJson() : null)
                .interviewerComment(eval != null ? eval.getInterviewerComment() : null)
                .build();
    }
} 