package com.JobAyong.service;

import com.JobAyong.dto.InterviewArchiveResponse;
import com.JobAyong.entity.InterviewArchive;
import com.JobAyong.entity.InterviewQuestion;
import com.JobAyong.entity.InterviewAnswer;
import com.JobAyong.entity.User;
import com.JobAyong.repository.InterviewArchiveRepository;
import com.JobAyong.repository.InterviewQuestionRepository;
import com.JobAyong.repository.InterviewAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

import com.JobAyong.constant.InterviewStatus;

@Service
@RequiredArgsConstructor
public class InterviewArchiveService {

    private final InterviewArchiveRepository interviewArchiveRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;
    private final InterviewAnswerRepository interviewAnswerRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<InterviewArchive> getAllInterviewArchives(String email) {
        return interviewArchiveRepository.findByUserEmailAndDeletedAtIsNullAndStatus(email, InterviewStatus.DONE);
    }

    public List<InterviewArchiveResponse> getAllArchives() {
        // 현재 로그인한 사용자의 면접 아카이브만 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userService.whoareyou(email);

        List<InterviewArchive> archives = interviewArchiveRepository.findByUser(user);
        return archives.stream()
                .map(InterviewArchiveResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public void deleteById(int id) {
        interviewArchiveRepository.deleteById(id);
    }

    public List<InterviewArchiveResponse> getAllArchivesForAdmin() {
        List<InterviewArchive> archives = interviewArchiveRepository.findAll();
        return archives.stream()
                .map(InterviewArchiveResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getQuestionsAndAnswers(int archiveId) {
        // 현재 로그인한 사용자 확인
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userService.whoareyou(email);

        InterviewArchive archive = interviewArchiveRepository.findById(archiveId)
                .orElseThrow(() -> new RuntimeException("면접 아카이브를 찾을 수 없습니다."));

        // 해당 아카이브가 현재 사용자의 것인지 확인
        if (!archive.getUser().getEmail().equals(user.getEmail())) {
            throw new RuntimeException("접근 권한이 없습니다.");
        }

        List<InterviewQuestion> questions = interviewQuestionRepository.findAllByInterviewArchive(archive);
        List<Map<String, Object>> result = new ArrayList<>();

        for (InterviewQuestion question : questions) {
            Map<String, Object> questionAnswer = new HashMap<>();
            questionAnswer.put("questionId", question.getInterviewQuestionId());
            questionAnswer.put("question", question.getInterview_question());
            questionAnswer.put("questionType", question.getInterview_question_type().toString());

            // 해당 질문에 대한 답변 찾기
            if (question.getInterviewAnswer() != null) {
                questionAnswer.put("answerId", question.getInterviewAnswer().getInterviewAnswerId());
                questionAnswer.put("answer", question.getInterviewAnswer().getInterview_answer());
            } else {
                questionAnswer.put("answerId", null);
                questionAnswer.put("answer", null);
            }

            result.add(questionAnswer);
        }

        return result;
    }
}