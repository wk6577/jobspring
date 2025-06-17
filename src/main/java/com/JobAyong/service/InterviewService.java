package com.JobAyong.service;

import com.JobAyong.constant.InterviewQuestionType;
import com.JobAyong.constant.InterviewStatus;
import com.JobAyong.dto.createNewInterviewArchiveRequest;
import com.JobAyong.entity.Company;
import com.JobAyong.entity.InterviewArchive;
import com.JobAyong.entity.InterviewQuestion;
import com.JobAyong.entity.User;
import com.JobAyong.repository.InterviewAnswerRepository;
import com.JobAyong.repository.InterviewArchiveRepository;
import com.JobAyong.repository.InterviewEvalRepository;
import com.JobAyong.repository.InterviewQuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterviewService {

    private final InterviewArchiveRepository interviewArchiveRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;
    private final InterviewAnswerRepository interviewAnswerRepository;
    private final InterviewEvalRepository interviewEvalRepository;
    private final UserService userService;
    private final CompanyService companyService ;

    /*@apiNote 질문 모드 ENUM 세팅해주는 함수
    * @author 나세호
    * */
    public InterviewQuestionType setQuestionType(String type){
        String mode = type.trim();
        return switch (mode) {
            case "general" -> InterviewQuestionType.GENERAL;
            case "pressure" -> InterviewQuestionType.PRESSURE;
            case "personality" -> InterviewQuestionType.PERSONALITY;
            case "technical" -> InterviewQuestionType.TECHNICAL;
            case "situational" -> InterviewQuestionType.SITUATIONAL;
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
    }

    /*@apiNote 새로운 아카이브를 생성하고 그안에 질문을 저장해주는 함수
     *@author 나세호
     * */
    @Transactional
    public void createArchive(createNewInterviewArchiveRequest request){
        User user = userService.whoareyou(request.getEmail()); // 예외 발생 구간01
        Company company = companyService.findById(request.getCompanyId()); // 예외 발생 구간02

        InterviewArchive new_interviewArchive = new InterviewArchive();
        new_interviewArchive.setUser(user);
        new_interviewArchive.setCompany(company);
        new_interviewArchive.setPosition(request.getPosition());
        new_interviewArchive.setStatus(InterviewStatus.PENDING);
        new_interviewArchive.setArchive_name(request.getArchiveName());

        interviewArchiveRepository.save(new_interviewArchive);

        List<InterviewQuestion> questionList = new ArrayList<>();

        for (int i = 0; i < request.getQuestions().size(); i++) {

            InterviewQuestion new_interviewQuestion = new InterviewQuestion();
            new_interviewQuestion.setInterviewArchive(new_interviewArchive);
            new_interviewQuestion.setInterview_question(request.getQuestions().get(i));
            if(request.getModes().isEmpty()){
                new_interviewQuestion.setInterview_question_type(setQuestionType(request.getAlternativeMode()));
            }else {
                new_interviewQuestion.setInterview_question_type(setQuestionType(request.getModes().get(i)));
            }
            questionList.add(new_interviewQuestion);
        }
        interviewQuestionRepository.saveAll(questionList);
    }

}
