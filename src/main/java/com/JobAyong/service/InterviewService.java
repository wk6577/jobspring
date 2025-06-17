package com.JobAyong.service;

import com.JobAyong.constant.InterviewQuestionType;
import com.JobAyong.constant.InterviewStatus;
import com.JobAyong.dto.createNewInterviewArchiveRequest;
import com.JobAyong.entity.Company;
import com.JobAyong.entity.InterviewArchive;
import com.JobAyong.entity.InterviewQuestion;
import com.JobAyong.entity.InterviewAnswer;
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
import java.util.stream.Collectors;
import java.util.Comparator;

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
    public Integer createArchiveAndReturnId(createNewInterviewArchiveRequest request){
        User user = userService.whoareyou(request.getEmail()); // 예외 발생 구간01
        Company company = companyService.findById(request.getCompanyId()); // 예외 발생 구간02

        InterviewArchive new_interviewArchive = new InterviewArchive();
        new_interviewArchive.setUser(user);
        new_interviewArchive.setCompany(company);
        new_interviewArchive.setPosition(request.getPosition());
        new_interviewArchive.setStatus(InterviewStatus.PENDING);
        new_interviewArchive.setArchive_name(request.getArchiveName());

        InterviewArchive savedArchive = interviewArchiveRepository.save(new_interviewArchive);

        List<InterviewQuestion> questionList = new ArrayList<>();

        for (int i = 0; i < request.getQuestions().size(); i++) {

            InterviewQuestion new_interviewQuestion = new InterviewQuestion();
            new_interviewQuestion.setInterviewArchive(savedArchive);
            new_interviewQuestion.setInterview_question(request.getQuestions().get(i));
            if(request.getModes() == null || request.getModes().isEmpty()){
                new_interviewQuestion.setInterview_question_type(setQuestionType(request.getAlternativeMode()));
            }else {
                new_interviewQuestion.setInterview_question_type(setQuestionType(request.getModes().get(i)));
            }
            questionList.add(new_interviewQuestion);
        }
        interviewQuestionRepository.saveAll(questionList);

        return savedArchive.getInterviewArchiveId();
    }

    /*@apiNote 면접 답변을 저장하는 함수
    *@author 나세호
    * */
    @Transactional
    public void saveInterviewAnswers(String archiveId, List<String> answers) {
        try {
            // 아카이브 ID로 아카이브 조회
            InterviewArchive archive = interviewArchiveRepository.findById(Integer.parseInt(archiveId))
                    .orElseThrow(() -> new IllegalArgumentException("해당 ID의 면접 아카이브를 찾을 수 없습니다: " + archiveId));
            
            // 해당 아카이브의 질문 목록 조회 (생성 시간 순으로 정렬)
            List<InterviewQuestion> questions = interviewQuestionRepository.findAll().stream()
                    .filter(q -> q.getInterviewArchive().getInterviewArchiveId().equals(archive.getInterviewArchiveId()))
                    .sorted(Comparator.comparing(InterviewQuestion::getCreatedAt))
                    .collect(Collectors.toList());
            
            if (questions.isEmpty()) {
                throw new IllegalArgumentException("해당 아카이브에 질문이 없습니다.");
            }
            
            if (questions.size() != answers.size()) {
                log.warn("질문과 답변의 개수가 일치하지 않습니다. 질문: {}, 답변: {}", questions.size(), answers.size());
            }
            
            // 기존 답변이 있다면 모두 삭제 (중복 방지)
            List<InterviewAnswer> existingAnswers = interviewAnswerRepository.findAll().stream()
                    .filter(a -> a.getInterviewArchive().getInterviewArchiveId().equals(archive.getInterviewArchiveId()))
                    .collect(Collectors.toList());
            
            if (!existingAnswers.isEmpty()) {
                interviewAnswerRepository.deleteAll(existingAnswers);
                log.info("기존 답변 {} 개 삭제 완료", existingAnswers.size());
            }
            
            // 답변 저장을 위한 리스트
            List<InterviewAnswer> answerEntities = new ArrayList<>();
            
            // 질문 수와 답변 수 중 작은 것만큼 반복
            int minSize = Math.min(questions.size(), answers.size());
            for (int i = 0; i < minSize; i++) {
                InterviewAnswer answer = new InterviewAnswer();
                answer.setInterviewArchive(archive);
                answer.setInterviewQuestion(questions.get(i));  // 질문과 명시적으로 연결
                answer.setInterview_answer(answers.get(i));
                answerEntities.add(answer);
            }
            
            // DB에 저장
            interviewAnswerRepository.saveAll(answerEntities);
            
            log.info("면접 답변 저장 완료. 아카이브 ID: {}, 저장된 답변 수: {}", archiveId, answerEntities.size());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("아카이브 ID가 유효하지 않습니다: " + archiveId);
        }
    }

}
