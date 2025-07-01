package com.JobAyong.controller;

import com.JobAyong.dto.CreateVoiceEvalRequest;
import com.JobAyong.dto.CreateVoiceRequest;
import com.JobAyong.service.VoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/voices")
@RequiredArgsConstructor
public class VoiceController {
    private final VoiceService voiceService;

    /**
     * @apiNote 새 음성 파일 저장
     * @author 최선아
     *
     * @param request 음성 파일 데이터
     * @return 201 OK + true (성공), 500 Internal Server Error + false (실패)
     */
    @PostMapping("/voiceFile")
    public ResponseEntity<?> addVoice(@RequestBody CreateVoiceRequest request) {
        System.out.println("/voice진입");
        int voiceId = voiceService.addVoice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(voiceId);
    }

    /**
     * @apiNote 새 음성 분석 저장
     * @author 최선아
     *
     * @param request 음성 데이터
     * @return 201 OK + true (성공), 500 Internal Server Error + false (실패)
     */
    @PostMapping
    public ResponseEntity<?> addVoiceEval(@RequestBody CreateVoiceEvalRequest request) {
        boolean success = voiceService.addVoiceEval(request);

        if (success) {
            return ResponseEntity.status(HttpStatus.CREATED).body(true);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }
}
