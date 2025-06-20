package com.JobAyong.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import jakarta.annotation.PostConstruct;

import java.util.List;
import java.util.Map;
@Slf4j
@Service
public class GPTService {

    @Value("${openai.api}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Autowired
    private Environment environment;

    private final RestTemplate restTemplate = new RestTemplate();

    // 생성자에서 API 키 상태 확인
    public GPTService() {
        System.out.println("=== GPTService 초기화 ===");
    }

    // API 키 상태를 확인하는 메서드 추가
    @PostConstruct
    public void init() {
        System.out.println("=== GPTService 초기화 완료 ===");
        System.out.println("활성 프로파일: " + String.join(", ", environment.getActiveProfiles()));
        System.out.println("API URL: " + apiUrl);
        System.out.println("API Key 존재 여부: " + (apiKey != null));
        System.out.println("API Key 길이: " + (apiKey != null ? apiKey.length() : 0));
        System.out.println("API Key 앞 20자리: " + (apiKey != null && apiKey.length() > 20 ? apiKey.substring(0, 20) + "..." : apiKey));
        System.out.println("API Key가 sk-로 시작하는가: " + (apiKey != null && apiKey.startsWith("sk-")));
        System.out.println("=== =================== ===");
    }

    public String askCustomInterViewGPT(String prompt) {
        return callGPTAPI("면접", prompt, 0.7);
    }

    /**
     * 추출된 생 텍스트에서 자소서로 추정되는 텍스트만 추출하는 메서드
     * @param rawText 파일에서 추출된 생 텍스트
     * @return 자소서로 추정되는 정제된 텍스트
     */
    public String extractResumeContentFromRawText(String rawText) {
        log.info(rawText);
        String prompt = createResumeExtractionPrompt(rawText);
        return callGPTAPI("자소서 추출", prompt, 0.3);
    }

    /**
     * GPT API 호출 공통 메서드
     */
    private String callGPTAPI(String taskName, String prompt, double temperature) {
        try {
            // API 키 검증
            if (apiKey == null || apiKey.trim().isEmpty() || apiKey.equals("your-openai-api-key-here")) {
                return taskName + " 처리 실패: OpenAI API 키가 설정되지 않았습니다. ENVIRONMENT_SETUP.md를 참조하여 API 키를 설정해주세요.";
            }

            // API 키 형식 검증 (OpenAI API 키는 'sk-'로 시작)
            if (!apiKey.startsWith("sk-")) {
                return taskName + " 처리 실패: 올바르지 않은 API 키 형식입니다. OpenAI API 키는 'sk-'로 시작해야 합니다.";
            }

            System.out.println("=== GPT API 호출 디버그 정보 ===");
            System.out.println("Task: " + taskName);
            System.out.println("API URL: " + apiUrl);
            System.out.println("API Key 앞 10자리: " + apiKey.substring(0, Math.min(10, apiKey.length())) + "...");
            System.out.println("Temperature: " + temperature);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> message = Map.of(
                    "role", "user",
                    "content", prompt
            );

            Map<String, Object> requestBody = Map.of(
                    "model", "gpt-4o",
                    "messages", List.of(message),
                    "temperature", temperature,
                    "max_tokens", 16384
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);
            Map content = (Map) ((Map) ((List) response.getBody().get("choices")).get(0)).get("message");
            
            String result = content.get("content").toString();
            System.out.println("GPT API 호출 성공");
            return result;

        } catch (HttpClientErrorException.Unauthorized e) {
            String errorMsg = taskName + " 처리 실패: API 키 인증 오류 (401 Unauthorized). " +
                    "OpenAI API 키를 확인해주세요. " +
                    "키가 유효한지, 계정에 크레딧이 있는지 확인하시기 바랍니다.";
            System.err.println("=== API 키 인증 오류 ===");
            System.err.println("사용된 API 키 앞 10자리: " + (apiKey != null ? apiKey.substring(0, Math.min(10, apiKey.length())) + "..." : "null"));
            System.err.println("오류 메시지: " + e.getMessage());
            return errorMsg;
            
        } catch (HttpClientErrorException e) {
            String errorMsg = taskName + " 처리 실패: HTTP 오류 (" + e.getStatusCode() + "). " + e.getMessage();
            System.err.println("=== HTTP 오류 ===");
            System.err.println("상태 코드: " + e.getStatusCode());
            System.err.println("응답 본문: " + e.getResponseBodyAsString());
            return errorMsg;
            
        } catch (Exception e) {
            String errorMsg = taskName + " 처리 실패: " + e.getMessage();
            System.err.println("=== 일반 오류 ===");
            e.printStackTrace();
            return errorMsg;
        }
    }

    /**
     * 자소서 추출을 위한 프롬프트 생성
     * @param rawText 원본 텍스트
     * @return GPT에게 전달할 프롬프트
     */
    private String createResumeExtractionPrompt(String rawText) {
        return String.format(
            "다음 문서에서 '자기소개서' 또는 '자기PR'에 해당하는 내용만 정확히 추출해 주세요.\n\n" +
            
            "📌 포함해야 하는 문장 유형:\n" +
            "- 지원동기, 지원 이유\n" +
            "- 성장 과정, 성격, 가치관\n" +
            "- 개인 경험, 에피소드, 느낀 점\n" +
            "- 입사 후 목표나 계획\n" +
            "- 회사 또는 직무에 대한 관심\n" +
            "- 프로젝트 경험 + 본인의 역할과 느낀 점\n" +
            "- 학습, 도전, 실패와 극복 경험\n\n" +
            
            "📌 제외해야 하는 내용:\n" +
            "- 이름, 나이, 연락처, 주소\n" +
            "- 학력, 학교명, 졸업년도\n" +
            "- 자격증, 단순 스펙, 회사명, 근무기간\n" +
            "- 표, 기호, 제목, 페이지번호, 공백\n" +
            "- 단순 프로젝트 후기, 결과 요약, 일정, 기술 나열\n\n" +
            
            "📌 출력 규칙 (반드시 지켜야 함):\n" +
            "1. 자소서/자기PR 관련 문장만 원문 그대로 출력하세요.\n" +
            "2. 내용이 없으면 정확히 다음 문장만 출력하세요: 자소서 내용을 찾을 수 없습니다.\n" +
            "3. '자기소개서 내용:', '다음은', '결과' 같은 말은 출력하지 마세요.\n" +
            "4. 문단 구분과 줄바꿈은 원문 그대로 유지하세요.\n" +
            "5. 요약, 재구성, 해석하지 말고, 원문 그대로 보여 주세요.\n" +
            "6. 자소서/자기PR 내용에 '[]', '()' 같은 기호가 있다면 수정하지 말고 출력하세요.\n" +
            "7. 자소서 내용에 대한 문단이라고 판단되면 문단내의 문장은 제외하지 말고 출력하세요.\n" +
            "8. 자소서/자기PR 내용에 포함된 **모든 괄호 문자(예: [], (), {})는 절대 삭제하거나 수정하지 말고 원문 그대로 출력**하세요. 괄호 안의 텍스트도 중요한 내용입니다.\n\n" +
            
            "예시:\n" +
            "원문: 이름: 김철수 / 학력: OO대학교 / 저는 대학시절 팀 프로젝트를 통해 리더십을 기를 수 있었습니다. 처음에는...\n" +
            "출력: 저는 대학시절 팀 프로젝트를 통해 리더십을 기를 수 있었습니다. 처음에는...\n" +
            "예: '[성실함]'이라는 표현이 있으면, **대괄호 포함하여 '[성실함]' 전체를 출력**해야 합니다.\n\n" +
            
            "분석할 원문:\n" +
            "%s", rawText
        );
    }
}
