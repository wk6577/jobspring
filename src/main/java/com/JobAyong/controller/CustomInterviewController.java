//package com.JobAyong.controller;
//
//import com.JobAyong.service.GPTService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/customInterviewController")
//@CrossOrigin(origins = "http://localhost:3000")
//
//public class CustomInterviewController {
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    private final GPTService gptService;
//
//    public CustomInterviewController(GPTService gptService) {
//        this.gptService = gptService;
//    }
//
//    @PostMapping("/evaluation")
//    public ResponseEntity<?> evaluateCustomInterview(@RequestBody Map<String, Object> requestData) {
//        // 테스트용 더미 데이터
//        System.out.println("Received Custom Evaluation Request:");
//        System.out.println(requestData);
//
//        Map<String, Object> resultMap = new HashMap<>();
//
//        resultMap.put("score", 83);
//        resultMap.put("comment", "답변의 논리성과 직무 연관성이 잘 드러났습니다. 하지만 사례 구성에서 일부 반복이 보였습니다.");
//
//        resultMap.put("good_summary01", "명확한 전달력");
//        resultMap.put("good_description01", "질문에 대한 핵심을 빠르게 파악하고, 간결하게 답변하는 능력이 돋보였습니다.");
//        resultMap.put("good_summary02", "직무 이해도");
//        resultMap.put("good_description02", "답변에서 개발 직무에 대한 깊은 이해가 드러났습니다.");
//        resultMap.put("good_summary03", "자기 주도성");
//        resultMap.put("good_description03", "문제 상황에 대해 스스로 해결책을 도출한 경험이 인상적이었습니다.");
//
//        resultMap.put("bad_summary01", "사례 반복");
//        resultMap.put("bad_description01", "두 개의 답변에서 유사한 프로젝트 경험이 반복되어 다양성이 부족해 보였습니다.");
//        resultMap.put("bad_summary02", "감정 표현 부족");
//        resultMap.put("bad_description02", "성공 경험을 말하면서 감정 표현이 부족해 다소 건조한 인상을 줍니다.");
//        resultMap.put("bad_summary03", "디테일 부족");
//        resultMap.put("bad_description03", "상황 설명에 구체성이 떨어져 설득력이 약화되었습니다.");
//
//        resultMap.put("state01", "좋아짐");
//        resultMap.put("cause01", "개선된 논리 흐름");
//        resultMap.put("state02", "유사함");
//        resultMap.put("cause02", "구성은 같으나 전달 방식 개선");
//        resultMap.put("state03", "나빠짐");
//        resultMap.put("cause03", "표현력 약화로 정보 전달 저하");
//
//        resultMap.put("solution01", "경험을 다양하게 분산시켜 제시해 보세요.");
//        resultMap.put("solution02", "프로젝트 외에도 협업 경험 등으로 영역을 확장해보세요.");
//        resultMap.put("solution03", "답변에 감정과 동기를 더해 인간적인 요소를 부각시켜 보세요.");
//        resultMap.put("solution04", "면접관이 궁금할만한 디테일을 미리 짚어주는 방식도 좋습니다.");
//        resultMap.put("solution05", "동일 사례를 언급할 경우 포인트를 명확히 달리하세요.");
//
//        resultMap.put("improvment01", "다양한 시나리오에 대비하여 답변의 폭을 넓히세요.");
//        resultMap.put("improvment02", "답변에 감정 표현을 더해 몰입감을 높이세요.");
//        resultMap.put("improvment03", "사례를 말할 때 핵심 메시지를 명확히 하세요.");
//
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            String jsonString = mapper.writeValueAsString(resultMap);  // 🟡 JSON 문자열로 변환
//            return ResponseEntity.ok()
//                    .header("Content-Type", "application/json")
//                    .body(jsonString); // 실제 JSON string 반환
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body("JSON 변환 에러 발생: " + e.getMessage());
//        }
//
//
//
//
////        System.out.println("🔍 받은 요청: " + requestData);
////
////        // 질문과 답변을 추출해 GPT에 던질 prompt 구성
////        List<String> questions = (List<String>) requestData.get("questions");
////        List<String> answers = (List<String>) requestData.get("answers");
////
////        StringBuilder prompt = new StringBuilder();
////        prompt.append("""
////                        다음은 모의 면접 질문과 응답입니다. 아래 데이터를 참고하여 다음 형식의 JSON으로 평가 결과를 생성해주세요.
////                        반드시 포맷 양식대로 빠짐없이 작성해주세요.
////
////                        ***[요청 포맷 예시]***
////                        {
////                          "score": 83,
////                          "comment": "전체 총평",
////                          "good_summary01": "명확한 전달력",
////                          "good_description01": "질문에 대한 핵심을 빠르게 파악하고, 간결하게 답변하는 능력이 돋보였습니다.",
////                          "good_summary02": "직무 이해도",
////                          "good_description02": "답변에서 개발 직무에 대한 깊은 이해가 드러났습니다.",
////                          "good_summary03": "자기 주도성",
////                          "good_description03": "문제 상황에 대해 스스로 해결책을 도출한 경험이 인상적이었습니다.",
////                          "bad_summary01": "사례 반복",
////                          "bad_description01": "두 개의 답변에서 유사한 프로젝트 경험이 반복되어 다양성이 부족해 보였습니다.",
////                          "bad_summary02": "감정 표현 부족",
////                          "bad_description02": "성공 경험을 말하면서 감정 표현이 부족해 다소 건조한 인상을 줍니다.",
////                          "bad_summary03": "디테일 부족",
////                          "bad_description03": "상황 설명에 구체성이 떨어져 설득력이 약화되었습니다.",
////                          "state01": "좋아짐",
////                          "cause01": "개선된 논리 흐름",
////                          "state02": "유사함",
////                          "cause02": "구성은 같으나 전달 방식 개선",
////                          "state03": "나빠짐",
////                          "cause03": "표현력 약화로 정보 전달 저하",
////                          "solution01": "경험을 다양하게 분산시켜 제시해 보세요.",
////                          "solution02": "프로젝트 외에도 협업 경험 등으로 영역을 확장해보세요.",
////                          "solution03": "답변에 감정과 동기를 더해 인간적인 요소를 부각시켜 보세요.",
////                          "solution04": "면접관이 궁금할만한 디테일을 미리 짚어주는 방식도 좋습니다.",
////                          "solution05": "동일 사례를 언급할 경우 포인트를 명확히 달리하세요.",
////                          "improvment01": "다양한 시나리오에 대비하여 답변의 폭을 넓히세요.",
////                          "improvment02": "답변에 감정 표현을 더해 몰입감을 높이세요.",
////                          "improvment03": "사례를 말할 때 핵심 메시지를 명확히 하세요."
////                        }
////                        """);
////
////        prompt.append("state(최근 답변에 대한 평가) 항목은 반드시 3개 만들어주세요.\n");
////        prompt.append("cause(이번 답변에 대한 분석 결과) 항목은 반드시 3개 만들어주세요.\n");
////        prompt.append("solution(질문별 피드백) 항목은 총 ").append(questions.size()).append("개 만들어주세요.\n");
////        prompt.append("improvment(다음 면접을 위한 제안) 항목은 반드시 3개 만들어주세요.\n");
////        prompt.append("다음은 질문과 응답입니다.");
////        // 질문-답변 붙이기
////        for (int i = 0; i < questions.size(); i++) {
////            prompt.append("Q").append(i + 1).append(": ").append(questions.get(i)).append("\n");
////            if (i < answers.size()) {
////                prompt.append("A").append(i + 1).append(": ").append(answers.get(i)).append("\n");
////            }
////        }
////
////
////        //GPT 호출
////        String gptResponse = gptService.askCustomInterViewGPT(prompt.toString());
////
////        Map<String, Object> result = new HashMap<>();
////        try {
////            // GPT 응답이 JSON 문자열이라고 가정하고 파싱
////            Map<String, Object> parsed = objectMapper.readValue(gptResponse, Map.class);
////
////            // 원하는 구조로 반환
////            return ResponseEntity.ok(parsed);
////        } catch (Exception e) {
////            result.put("score", -1);
////            e.printStackTrace();
////        }
////
////        result.put("reason", gptResponse); // 전체 원문은 그냥 출력
////
////        return ResponseEntity.ok(result);
//
//
//    }
//}
