package com.JobAyong.service;

import com.JobAyong.constant.InterviewQuestionType;
import com.JobAyong.constant.InterviewStatus;
import com.JobAyong.dto.CreateNewInterviewArchiveRequest;
import com.JobAyong.dto.CreateNewInterviewQuestionAndEvalRequest;
import com.JobAyong.dto.GetPrevImprovementRequest;
import com.JobAyong.dto.GetPrevImprovementResponse;
import com.JobAyong.entity.*;
import com.JobAyong.repository.InterviewAnswerRepository;
import com.JobAyong.repository.InterviewArchiveRepository;
import com.JobAyong.repository.InterviewEvalRepository;
import com.JobAyong.repository.InterviewQuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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
            case "custom" -> InterviewQuestionType.CUSTOM;
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
    }

    /*@apiNote 새로운 아카이브를 생성하고 그안에 질문을 저장해주는 함수
     *@author 나세호
     * */
    @Transactional
    public Integer createArchive(CreateNewInterviewArchiveRequest request){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userService.whoareyou(email);

        Company company;
        if(request.getCompanyId() == null){
            company = companyService.findById(-9999L);
        }else {
            company = companyService.findById(request.getCompanyId());
        }// 예외 발생 구간02

        InterviewArchive new_interviewArchive = new InterviewArchive();
        new_interviewArchive.setUser(user);
        new_interviewArchive.setCompany(company);
        new_interviewArchive.setPosition(request.getPosition());
        new_interviewArchive.setStatus(InterviewStatus.PENDING);
        new_interviewArchive.setMode(setQuestionType(request.getArchiveMode()));
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

        return new_interviewArchive.getInterviewArchiveId();
    }

    /*@apiNote 생성된 아카이브에 답변과 평가 저장해주는 함수
     *@author 나세호
     * */
    @Transactional
    public void saveAnswerAndEval(CreateNewInterviewQuestionAndEvalRequest request){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userService.whoareyou(email); // 예외 발생 가능(나중에 토큰 인증기능으로 대체)

        CreateNewInterviewQuestionAndEvalRequest.EvaluationDTO result_of_eval = request.getEvaluation();
        Integer interviewArchiveId = request.getInterviewArchiveId();
        List<String> answerList = request.getAnswers();

        InterviewArchive interviewArchive = interviewArchiveRepository.findById(interviewArchiveId).orElseThrow(() -> new RuntimeException("유효하지 않은 아키이브 아이디 입니다.")); // 예외 발생 가능
        List<InterviewQuestion> interviewQuestions = interviewQuestionRepository.findAllByInterviewArchive(interviewArchive);

        List<InterviewAnswer> interviewAnswerList = new ArrayList<>();

        for (int i = 0; i < interviewQuestions.size() ; i++) {

            InterviewAnswer dbAnswer = new InterviewAnswer();
            dbAnswer.setInterviewQuestion(interviewQuestions.get(i));
            dbAnswer.setInterviewArchive(interviewArchive);
            dbAnswer.setInterview_answer(answerList.get(i));
            interviewAnswerList.add(dbAnswer);
        }

        interviewAnswerRepository.saveAll(interviewAnswerList);

        Optional<InterviewEval> existingEval = interviewEvalRepository.findByInterviewArchive(interviewArchive);

        if (existingEval.isPresent()) {
            interviewArchive.setStatus(InterviewStatus.DONE);
            interviewArchiveRepository.save(interviewArchive);
            throw new RuntimeException("이미 해당 아카이브에는 평가가 존재합니다."); // 먼저 평가존재하는지 파악한뒤 생성
        }

        InterviewEval new_eval = new InterviewEval();

        new_eval.setEval_score(result_of_eval.getScore());
        new_eval.setEval_reason(result_of_eval.getReason());
        new_eval.setEval_bad_summary(result_of_eval.getBad_summary());
        new_eval.setEval_bad_description(result_of_eval.getBad_description());
        new_eval.setEval_good_summary(result_of_eval.getGood_summary());
        new_eval.setEval_good_description(result_of_eval.getGood_description());
        new_eval.setEval_state(result_of_eval.getState());
        new_eval.setEval_cause(result_of_eval.getCause());
        new_eval.setEval_solution(result_of_eval.getSolution());
        new_eval.setEval_improvment(result_of_eval.getImprovment());
        new_eval.setInterviewArchive(interviewArchive);
        new_eval.setMode(setQuestionType(request.getEvalMode()));

        interviewEvalRepository.save(new_eval);

        interviewArchive.setStatus(InterviewStatus.DONE);

        interviewArchiveRepository.save(interviewArchive);
    }

    @Transactional
    public void deleteEval(int id, String email){
        // 누군지 확인
        User user = userService.whoareyou(email);
        // 접근 권한 확인
        InterviewArchive archive = interviewArchiveRepository.findById(id).orElseThrow(() -> new RuntimeException("유효하지 않은 아키이브 아이디 입니다."));

        if(archive.getUser() != user){
            throw new RuntimeException("니꺼 아니잖아 나가");
        }

        interviewArchiveRepository.delete(archive);
    }

    @Transactional(readOnly = true)
    public GetPrevImprovementResponse getPrevImprovements(GetPrevImprovementRequest request){
        InterviewQuestionType target = setQuestionType(request.getMode());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.whoareyou(authentication.getName());

        List<InterviewArchive> archiveList = interviewArchiveRepository.findByUser(user);
        if(archiveList.isEmpty()){
            throw new RuntimeException("생성된 아카이브 없음 생성 필요");
        }

        InterviewArchive secondLatestMatchingArchive = archiveList.stream()
                .filter(archive -> target.equals(archive.getMode()))
                .sorted(Comparator.comparing(InterviewArchive::getCreatedAt).reversed())
                .skip(1)
                .findFirst()
                .orElse(null);

        if (secondLatestMatchingArchive == null) {
            return null;
        }

        InterviewEval lastEval = interviewEvalRepository.findByInterviewArchive(secondLatestMatchingArchive).orElse(null);

        if (lastEval == null) {
            return null;
        }

        GetPrevImprovementResponse response = new GetPrevImprovementResponse();
        response.setPrev_badpoints(lastEval.getEval_bad_summary());
        response.setPrev_baddescription(lastEval.getEval_bad_description());

        return response;
    }
    
    /*@apiNote 면접 평가 타이틀 수정
     * @author AI
     * */
    @Transactional
    public void updateArchiveTitle(Integer id, String newTitle) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userService.whoareyou(email);
        
        InterviewArchive archive = interviewArchiveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 아카이브 아이디입니다."));
        
        // 권한 확인
        if(archive.getUser() != user) {
            throw new RuntimeException("해당 아카이브에 대한 권한이 없습니다.");
        }
        
        archive.setArchive_name(newTitle);
        interviewArchiveRepository.save(archive);
    }
}
