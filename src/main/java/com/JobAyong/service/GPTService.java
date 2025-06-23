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
     * 자소서 내용을 분석하고 개선안을 제공하는 메서드
     * @param resumeText 분석할 자소서 원본 텍스트
     * @return GPT가 분석한 결과 (JSON 형태의 문자열)
     */
    public String analyzeResumeContent(String resumeText) {
        log.info("자소서 분석 시작: " + resumeText.substring(0, Math.min(100, resumeText.length())) + "...");
        String prompt = createResumeAnalysisPrompt(resumeText);
        String rawResponse = callGPTAPI("자소서 분석", prompt, 0.7);
        
        // GPT 응답에서 마크다운 코드 블록 제거
        return cleanGPTResponse(rawResponse);
    }

    /**
     * GPT 응답에서 마크다운 코드 블록 제거
     * @param rawResponse GPT 원본 응답
     * @return 정리된 JSON 문자열
     */
    private String cleanGPTResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            return rawResponse;
        }
        
        String cleaned = rawResponse.trim();
        
        // ```json으로 시작하고 ```로 끝나는 마크다운 코드 블록 제거
        if (cleaned.startsWith("```json") && cleaned.endsWith("```")) {
            cleaned = cleaned.substring(7); // "```json" 제거
            cleaned = cleaned.substring(0, cleaned.length() - 3); // "```" 제거
            cleaned = cleaned.trim();
        }
        // ```로 시작하고 끝나는 일반 코드 블록도 처리
        else if (cleaned.startsWith("```") && cleaned.endsWith("```")) {
            // 첫 번째 줄바꿈까지 제거 (```json\n 또는 ```\n)
            int firstNewline = cleaned.indexOf('\n');
            if (firstNewline > 0) {
                cleaned = cleaned.substring(firstNewline + 1);
            } else {
                cleaned = cleaned.substring(3); // "```" 제거
            }
            cleaned = cleaned.substring(0, cleaned.length() - 3); // 마지막 "```" 제거
            cleaned = cleaned.trim();
        }
        
        return cleaned;
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
                    "model", "gpt-4.1-nano",
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

    /**
     * 자소서 분석을 위한 프롬프트 생성 (확장된 버전)
     * @param resumeText 분석할 자소서 텍스트
     * @return GPT에게 전달할 프롬프트
     */
    private String createResumeAnalysisPrompt(String resumeText) {
        return String.format(
            "당신은 전문적인 자기소개서 컨설턴트입니다. 다음 자기소개서를 분석하고 개선해주세요.\n\n" +
            
            "📌 분석 및 개선 기준:\n" +
            "1. ✅ 논리성과 구조\n" +
            "- 문단은 도입 → 전개 → 결론 구조로 자연스럽게 구성되어야 합니다.\n" +
            "- 문장 간, 문단 간 흐름이 자연스럽고 논리적으로 연결되어야 합니다.\n" +
            "- 불필요한 반복 문장을 제거하고, 핵심 메시지가 명확히 드러나야 합니다.\n\n" +

            "2. ✅ 표현력과 어휘 사용\n" +
            "- 문장이 자연스럽고 세련된 문어체로 구성되어야 합니다.\n" +
            "- 직무와 관련된 적절한 어휘를 사용하며, 비속어나 축약어는 제거해 주세요.\n" +
            "- 쉬운 문장 구조와 적절한 길이로 가독성을 높여 주세요.\n\n" +

            "3. ✅ 맞춤법 및 문법\n" +
            "- 철자, 띄어쓰기, 맞춤법 오류를 수정해 주세요.\n" +
            "- 주어와 서술어의 호응, 어순, 시제의 일관성 등을 점검하고 수정해 주세요.\n\n" +

            "4. ✅ 직무 역량 표현\n" +
            "- 문제 상황을 인식하고 해결한 사례를 명확히 서술해 주세요.\n" +
            "- 팀워크, 협업, 커뮤니케이션 능력이 드러나는 경험을 포함해 주세요.\n" +
            "- 해당 직무와 연관된 기술력, 전공, 자격증, 실무 경험이 자연스럽게 드러나야 합니다.\n\n" +

            "5. ✅ 성실성과 태도\n" +
            "- 꾸준한 노력, 장기간 지속된 활동, 자기계발 노력이 드러나도록 표현해 주세요.\n" +
            "- 외부 교육, 학습, 자격증 취득 등 성실하게 준비한 태도가 보이게 다듬어 주세요.\n" +
            "- 맡은 일을 책임감 있게 끝까지 수행한 사례를 강조해 주세요.\n\n" +

            "6. ✅ 리더십과 도전정신\n" +
            "- 조직을 이끌거나 조율한 리더 경험이 있다면 강조해 주세요.\n" +
            "- 실패 극복이나 새로운 시도를 한 도전 경험을 잘 드러내 주세요.\n" +
            "- 갈등 해결 또는 구성원 간 문제 조정 사례가 있다면 포함해 주세요.\n\n" +

            "7. ✅ 결과 및 성과 중심 표현\n" +
            "- 경험의 결과를 수치나 지표(예: 퍼센트, 증가율, 수량 등)로 표현해 주세요.\n" +
            "- 타인의 피드백을 수용하고 개선한 사례를 보여 주세요.\n" +
            "- 활동이나 프로젝트의 결과에 대한 회고 또는 반성도 포함해 주세요.\n\n" +
            
            "📌 출력 형식 (반드시 JSON 형태로 응답):\n" +
            "{\n" +
            "  \"analysis\": {\n" +
            "    \"original_sentences\": [\"원본 문장1\", \"원본 문장2\", ...],\n" +
            "    \"improved_sentences\": [\"개선 문장1\", \"개선 문장2\", ...],\n" +
            "    \"changes\": [\n" +
            "      {\n" +
            "        \"index\": 0,\n" +
            "        \"type\": \"modified\",\n" +
            "        \"original\": \"원본 문장\",\n" +
            "        \"improved\": \"개선된 문장\",\n" +
            "        \"reason\": \"개선 이유 설명\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"missing_areas\": [\n" +
            "      {\n" +
            "        \"category\": \"리더십과 도전정신\",\n" +
            "        \"description\": \"부족한 영역에 대한 설명\",\n" +
            "        \"suggestions\": [\n" +
            "          {\n" +
            "            \"title\": \"제안 제목\",\n" +
            "            \"content\": \"구체적인 제안 내용\",\n" +
            "            \"example\": \"작성 예시 문장\",\n" +
            "            \"insertion_point\": {\n" +
            "              \"after_sentence\": 2,\n" +
            "              \"reason\": \"삽입 위치 선택 이유\"\n" +
            "            }\n" +
            "          }\n" +
            "        ]\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}\n\n" +
            
            "📌 주의사항:\n" +
            "- 1단계: 기존 문장의 맞춤법, 문법, 표현력 개선 (changes 배열)\n" +
            "- 2단계: 7가지 기준에서 부족한 영역 식별 (missing_areas 배열)\n" +
            "- missing_areas에는 현재 자소서에서 부족하거나 없는 영역만 포함\n" +
            "- 각 부족한 영역마다 2-3개의 구체적인 개선 제안과 예시 제공\n" +
            "- 예시는 실제 자소서에 바로 사용할 수 있는 완성된 문장으로 작성\n" +
            "- category는 정확히 다음 중 하나: '논리성과 구조', '표현력과 어휘 사용', '맞춤법 및 문법', '직무 역량 표현', '성실성과 태도', '리더십과 도전정신', '결과 및 성과 중심 표현'\n\n" +
            
            "📌 삽입 위치 분석 (insertion_point):\n" +
            "- 각 예시 문장이 현재 자소서의 어느 위치에 삽입되면 가장 자연스러운지 분석\n" +
            "- after_sentence는 0부터 시작하는 문장 인덱스 (0번째 문장 뒤, 1번째 문장 뒤 등)\n" +
            "- 문맥의 흐름, 논리적 연결성, 주제의 연관성을 고려하여 최적 위치 선택\n" +
            "- reason에는 해당 위치를 선택한 구체적인 이유 설명\n" +
            "- 예: 프로젝트 경험 설명 후 → 리더십 역할 추가, 성장 과정 언급 후 → 구체적 성과 추가\n\n" +
            
            "분석할 자기소개서:\n" +
            "%s", resumeText
        );
    }
}
