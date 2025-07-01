package com.JobAyong.controller;

import com.JobAyong.dto.VoiceArchiveResponse;
import com.JobAyong.service.VoiceArchiveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<Void> deleteVoiceArchive(@PathVariable int voiceId) {
        log.info("음성 평가 삭제 API 호출. 음성 ID: {}", voiceId);
        try {
            voiceArchiveService.deleteVoiceArchive(voiceId);
            log.info("음성 평가 삭제 성공. 음성 ID: {}", voiceId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("음성 평가 삭제 실패. 음성 ID: {}", voiceId, e);
            throw e;
        }
    }
} 