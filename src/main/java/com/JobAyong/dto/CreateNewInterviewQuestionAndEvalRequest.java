package com.JobAyong.dto;

import com.JobAyong.constant.InterviewQuestionType;
import lombok.Data;

import java.util.List;

@Data
public class CreateNewInterviewQuestionAndEvalRequest {
    private EvaluationDTO evaluation;
    private Integer interviewArchiveId;
    private List<String> answers;
    private String evalMode;

    @Data
    public static class EvaluationDTO {
        private Integer score;
        private String reason;
        private String bad_description;
        private String bad_summary;
        private String good_description;
        private String good_summary;
        private String cause;
        private String state;
        private String improvment;
        private String solution;
    }
}
