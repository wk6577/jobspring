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

    private String callGPTAPI(String taskName, String prompt, double temperature) {
        log.info("=== " + taskName + " GPT API 호출 시작 ===");
        log.info("Prompt 내용: " + prompt.substring(0, Math.min(200, prompt.length())) + "...");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = Map.of(
                    "model", "gpt-4o",
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "max_tokens", 4000,
                "temperature", temperature
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            log.info("GPT API 요청 시작...");
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);
            log.info("GPT API 응답 상태: " + response.getStatusCode());

            Map<String, Object> responseBody = response.getBody();
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = (String) message.get("content");

            log.info("GPT 응답 길이: " + content.length() + " 글자");
            log.info("GPT 응답 앞 200자: " + content.substring(0, Math.min(200, content.length())) + "...");
            log.info("=== " + taskName + " GPT API 호출 완료 ===");

            return content;
            
        } catch (HttpClientErrorException e) {
            log.error("GPT API 호출 실패 - HTTP 오류: " + e.getStatusCode());
            log.error("응답 내용: " + e.getResponseBodyAsString());
            throw new RuntimeException("GPT API 호출 실패: " + e.getMessage());
        } catch (Exception e) {
            log.error("GPT API 호출 중 예외 발생: " + e.getMessage());
            throw new RuntimeException("GPT API 호출 실패: " + e.getMessage());
        }
    }

    private String createResumeExtractionPrompt(String rawText) {
        return """
자기소개서 추출 AI입니다.

📋 **작업 목표**: 파일에서 추출된 텍스트에서 자기소개서 내용만 깔끔하게 추출

📄 **추출 원본 텍스트**:
""" + rawText + """

📌 **추출 규칙**:
1. ✅ **자기소개서 내용만 추출**
   - 자기소개, 지원동기, 입사 후 포부, 성장과정, 성격 장단점 등
   - 개인의 경험, 역량, 목표가 서술된 문단들

2. ✅ **제거해야 할 내용**
   - 이력서 정보 (이름, 주소, 전화번호, 이메일, 학력, 경력 테이블 등)
   - 지원회사명, 지원직종, 날짜, 페이지 번호
   - 헤더, 푸터, 양식 텍스트
   - "자기소개서", "지원동기" 같은 제목/라벨만 있는 줄
   - 불완전한 문장이나 의미없는 텍스트 조각

3. ✅ **텍스트 정리**
   - 문단 구분을 명확히 유지
   - 온전한 문장들만 포함
   - 자연스러운 흐름으로 연결

4. ✅ **출력 형식**
   - 추출된 자기소개서 내용만 반환
   - 추가 설명이나 주석 없이 깔끔한 텍스트만
   - 문단 간 줄바꿈 유지

**결과물**: 자기소개서 내용만 포함된 깔끔한 텍스트
""";
    }

    @Data
    public static class GPTRequest {
        private String model = "gpt-4o";
        private List<Message> messages;
        private int max_tokens = 4000;
        private double temperature = 0.7;
    }

    @Data
    public static class Message {
        private String role;
        private String content;
    }
}

