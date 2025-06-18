package com.JobAyong.controller;

import com.JobAyong.dto.createNewInterviewArchiveRequest;
import com.JobAyong.dto.createNewInterviewArchiveResponse;
import com.JobAyong.dto.createNewInterviewQuestionAndEvalRequest;
import com.JobAyong.dto.createNewInterviewQuestionAndEvalResponse;
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
    public ResponseEntity<createNewInterviewArchiveResponse> createNewInterviewArchive(@RequestBody  createNewInterviewArchiveRequest request){
        int interviewArchiveID = interviewService.createArchive(request);

        createNewInterviewArchiveResponse response = new createNewInterviewArchiveResponse();
        response.setInterviewArchiveId(interviewArchiveID);
        return ResponseEntity.ok(response);
    }

    /*@apiNote 모의 면접 평가 및 답변 저장
     * @author 나세호
     * */
    @PostMapping("/answer/eval")
    public ResponseEntity<createNewInterviewQuestionAndEvalResponse> createNewInterviewQuestionAndEval(@RequestBody createNewInterviewQuestionAndEvalRequest request){
        interviewService.saveAnswerAndEval(request);

        createNewInterviewQuestionAndEvalResponse response = new createNewInterviewQuestionAndEvalResponse();
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
