package com.JobAyong.controller;

import com.JobAyong.dto.VoiceArchiveResponse;
import com.JobAyong.service.VoiceArchiveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/voice-archives")
@Slf4j
public class VoiceArchiveController {
    private final VoiceArchiveService voiceArchiveService;

    @GetMapping
    public ResponseEntity<List<VoiceArchiveResponse>> getAllVoiceArchives() {
        log.info("음성 평가 목록 조회 API 호출");
        try {
            List<VoiceArchiveResponse> archives = voiceArchiveService.getAllVoiceArchives();
            log.info("음성 평가 목록 조회 성공. 개수: {}", archives.size());
            return ResponseEntity.ok(archives);
        } catch (Exception e) {
            log.error("음성 평가 목록 조회 실패", e);
            throw e;
        }
    }

    @GetMapping("/{voiceId}")
    public ResponseEntity<VoiceArchiveResponse> getVoiceArchiveDetail(@PathVariable int voiceId) {
        log.info("음성 평가 상세 조회 API 호출. 음성 ID: {}", voiceId);
        try {
            VoiceArchiveResponse archive = voiceArchiveService.getVoiceArchiveDetail(voiceId);
            log.info("음성 평가 상세 조회 성공. 음성 ID: {}", voiceId);
            return ResponseEntity.ok(archive);
        } catch (Exception e) {
            log.error("음성 평가 상세 조회 실패. 음성 ID: {}", voiceId, e);
            throw e;
        }
    }

    @DeleteMapping("/{voiceId}")
    public ResponseEntity<Map<String, Object>> deleteVoiceArchive(@PathVariable int voiceId) {
        log.info("음성 평가 삭제 API 호출. 음성 ID: {}", voiceId);
        try {
            voiceArchiveService.deleteVoiceArchive(voiceId);
            log.info("음성 평가 삭제 성공. 음성 ID: {}", voiceId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "음성 평가가 휴지통으로 이동되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("음성 평가 삭제 실패. 음성 ID: {}", voiceId, e);
            throw e;
        }
    }

    @GetMapping("/trash")
    public ResponseEntity<List<VoiceArchiveResponse>> getDeletedVoiceArchives() {
        log.info("음성 평가 휴지통 목록 조회 API 호출");
        try {
            List<VoiceArchiveResponse> archives = voiceArchiveService.getDeletedVoiceArchives();
            log.info("음성 평가 휴지통 목록 조회 성공. 개수: {}", archives.size());
            return ResponseEntity.ok(archives);
        } catch (Exception e) {
            log.error("음성 평가 휴지통 목록 조회 실패", e);
            throw e;
        }
    }

    @PutMapping("/{voiceId}/restore")
    public ResponseEntity<Map<String, Object>> restoreVoiceArchive(@PathVariable int voiceId) {
        log.info("음성 평가 복구 API 호출. 음성 ID: {}", voiceId);
        try {
            voiceArchiveService.restoreVoiceArchive(voiceId);
            log.info("음성 평가 복구 성공. 음성 ID: {}", voiceId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "음성 평가가 복구되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("음성 평가 복구 실패. 음성 ID: {}", voiceId, e);
            throw e;
        }
    }

    @DeleteMapping("/{voiceId}/permanent")
    public ResponseEntity<Map<String, Object>> permanentlyDeleteVoiceArchive(@PathVariable int voiceId) {
        log.info("음성 평가 완전 삭제 API 호출. 음성 ID: {}", voiceId);
        try {
            voiceArchiveService.permanentlyDeleteVoiceArchive(voiceId);
            log.info("음성 평가 완전 삭제 성공. 음성 ID: {}", voiceId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "음성 평가가 완전히 삭제되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("음성 평가 완전 삭제 실패. 음성 ID: {}", voiceId, e);
            throw e;
        }
    }
} 