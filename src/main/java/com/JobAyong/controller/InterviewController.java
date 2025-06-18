package com.JobAyong.controller;

import com.JobAyong.dto.*;
import com.JobAyong.service.InterviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    /*@apiNote 새로운 모의 면접 기록 생성
    * @author 나세호
    * */
    @PostMapping
    public ResponseEntity<CreateNewInterviewArchiveResponse> createNewInterviewArchive(@RequestBody CreateNewInterviewArchiveRequest request){
        int interviewArchiveID = interviewService.createArchive(request);

        CreateNewInterviewArchiveResponse response = new CreateNewInterviewArchiveResponse();
        response.setInterviewArchiveId(interviewArchiveID);
        return ResponseEntity.ok(response);
    }

    /*@apiNote 이전 평가 가져오는 API
     * @author 나세호
     * */
    @PostMapping("/prevImprovement")
    public ResponseEntity<GetPrevImprovementResponse> getPrevImprovements(@RequestBody GetPrevImprovementRequest request){
        GetPrevImprovementResponse result = interviewService.getPrevImprovements(request);
        return ResponseEntity.ok(result);
    }

    /*@apiNote 모의 면접 평가 및 답변 저장
     * @author 나세호
     * */
    @PostMapping("/answer/eval")
    public ResponseEntity<CreateNewInterviewQuestionAndEvalResponse> createNewInterviewQuestionAndEval(@RequestBody CreateNewInterviewQuestionAndEvalRequest request){
        interviewService.saveAnswerAndEval(request);

        CreateNewInterviewQuestionAndEvalResponse response = new CreateNewInterviewQuestionAndEvalResponse();
        response.setMsg("자동 저장 완료");
        return ResponseEntity.ok(response);
    }
    
    /*@apiNote 면접 평가 타이틀 수정
     * @author AI
     * */
    @PutMapping("/{id}/title")
    public ResponseEntity<Map<String, Object>> updateInterviewTitle(@PathVariable("id") Integer id, @RequestBody Map<String, String> request) {
        String newTitle = request.get("title");
        interviewService.updateArchiveTitle(id, newTitle);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "타이틀이 성공적으로 수정되었습니다.");
        return ResponseEntity.ok(response);
    }
}
