package com.JobAyong.controller;

import com.JobAyong.dto.CreateVoiceEvalRequest;
import com.JobAyong.dto.CreateVoiceRequest;
import com.JobAyong.service.VoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    /**
     * @apiNote 음성 파일 가져오기
     * @author AI
     *
     * @param voiceId 음성 ID
     * @return 음성 파일
     */
    @GetMapping("/{voiceId}/audio")
    public ResponseEntity<?> getAudioFile(@PathVariable int voiceId) {
        log.info("음성 파일 요청 받음 - voiceId: {}", voiceId);
        
        try {
            Resource audioFile = voiceService.getAudioFile(voiceId);
            
            if (!audioFile.exists()) {
                log.error("음성 파일이 존재하지 않음 - voiceId: {}", voiceId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("음성 파일을 찾을 수 없습니다.");
            }
            
            log.info("음성 파일 반환 성공 - voiceId: {}, fileName: {}", voiceId, audioFile.getFilename());
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("audio/wav"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .body(audioFile);
                    
        } catch (IllegalArgumentException e) {
            log.error("잘못된 요청 - voiceId: {}, 오류: {}", voiceId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
            
        } catch (IllegalStateException e) {
            log.error("파일 접근 오류 - voiceId: {}, 오류: {}", voiceId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            
        } catch (Exception e) {
            log.error("음성 파일 처리 중 예상치 못한 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("음성 파일을 처리하는 중 오류가 발생했습니다.");
        }
    }
}
