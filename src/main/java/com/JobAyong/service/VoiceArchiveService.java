package com.JobAyong.service;

import com.JobAyong.dto.VoiceArchiveResponse;
import com.JobAyong.entity.Voice;
import com.JobAyong.entity.VoiceEval;
import com.JobAyong.repository.VoiceEvalRepository;
import com.JobAyong.repository.VoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoiceArchiveService {
    private final VoiceRepository voiceRepository;
    private final VoiceEvalRepository voiceEvalRepository;

    @Transactional(readOnly = true)
    public List<VoiceArchiveResponse> getAllVoiceArchives() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        log.info("음성 평가 목록 조회 시작. 사용자: {}", email);
        
        // 삭제되지 않은 음성 데이터만 가져오고 최신순으로 정렬
        List<Voice> voices = voiceRepository.findByUserEmailAndDeletedAtIsNullOrderByCreatedAtDesc(email);
        
        log.info("조회된 음성 데이터 개수: {}", voices.size());
        
        return voices.stream()
                .map(voice -> {
                    log.debug("음성 ID: {}, 파일명: {}", voice.getVoiceId(), voice.getFileName());
                    
                    VoiceEval eval = voiceEvalRepository.findByVoiceId(voice.getVoiceId())
                            .orElse(null);
                    
                    if (eval != null) {
                        log.debug("음성 ID {}에 대한 평가 데이터 발견. 점수: {}", voice.getVoiceId(), eval.getOverallScore());
                    } else {
                        log.debug("음성 ID {}에 대한 평가 데이터 없음", voice.getVoiceId());
                    }
                    
                    return VoiceArchiveResponse.from(voice, eval);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VoiceArchiveResponse getVoiceArchiveDetail(int voiceId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        log.info("음성 평가 상세 조회. 음성 ID: {}, 사용자: {}", voiceId, email);
        
        Voice voice = voiceRepository.findById(voiceId)
                .orElseThrow(() -> new RuntimeException("음성 평가를 찾을 수 없습니다."));
                
        if (!voice.getUser().getEmail().equals(email)) {
            throw new RuntimeException("접근 권한이 없습니다.");
        }
        
        VoiceEval eval = voiceEvalRepository.findByVoiceId(voiceId)
                .orElse(null);
                
        log.info("음성 평가 상세 조회 완료. 평가 데이터 존재: {}", eval != null);
                
        return VoiceArchiveResponse.from(voice, eval);
    }

    @Transactional
    public void deleteVoiceArchive(int voiceId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        log.info("음성 평가 삭제 시작. 음성 ID: {}, 사용자: {}", voiceId, email);
        
        Voice voice = voiceRepository.findById(voiceId)
                .orElseThrow(() -> new RuntimeException("음성 평가를 찾을 수 없습니다."));
                
        if (!voice.getUser().getEmail().equals(email)) {
            throw new RuntimeException("접근 권한이 없습니다.");
        }
        
        // 소프트 삭제 처리
        voice.setDeletedAt(LocalDateTime.now());
        voiceRepository.save(voice);
        
        log.info("음성 평가 삭제 완료. 음성 ID: {}", voiceId);
    }

    @Transactional(readOnly = true)
    public List<VoiceArchiveResponse> getDeletedVoiceArchives() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        log.info("음성 평가 휴지통 목록 조회 시작. 사용자: {}", email);
        
        // 삭제된 음성 데이터만 가져오고 삭제일 기준 최신순으로 정렬
        List<Voice> voices = voiceRepository.findByUserEmailAndDeletedAtIsNotNullOrderByDeletedAtDesc(email);
        
        log.info("조회된 휴지통 음성 데이터 개수: {}", voices.size());
        
        return voices.stream()
                .map(voice -> {
                    log.debug("휴지통 음성 ID: {}, 파일명: {}", voice.getVoiceId(), voice.getFileName());
                    
                    VoiceEval eval = voiceEvalRepository.findByVoiceId(voice.getVoiceId())
                            .orElse(null);
                    
                    return VoiceArchiveResponse.from(voice, eval);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void restoreVoiceArchive(int voiceId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        log.info("음성 평가 복구 시작. 음성 ID: {}, 사용자: {}", voiceId, email);
        
        Voice voice = voiceRepository.findById(voiceId)
                .orElseThrow(() -> new RuntimeException("음성 평가를 찾을 수 없습니다."));
                
        if (!voice.getUser().getEmail().equals(email)) {
            throw new RuntimeException("접근 권한이 없습니다.");
        }
        
        // 복구 처리 (deleted_at을 null로 설정)
        voice.setDeletedAt(null);
        voiceRepository.save(voice);
        
        log.info("음성 평가 복구 완료. 음성 ID: {}", voiceId);
    }

    @Transactional
    public void permanentlyDeleteVoiceArchive(int voiceId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        log.info("음성 평가 완전 삭제 시작. 음성 ID: {}, 사용자: {}", voiceId, email);
        
        Voice voice = voiceRepository.findById(voiceId)
                .orElseThrow(() -> new RuntimeException("음성 파일을 찾을 수 없습니다."));

        VoiceEval voiceEval = voiceEvalRepository.findByVoiceId(voice.getVoiceId())
                .orElseThrow(() -> new RuntimeException("음성 평가를 찾을 수 없습니다."));
                
        if (!voice.getUser().getEmail().equals(email)) {
            throw new RuntimeException("접근 권한이 없습니다.");
        }
        
        // 완전 삭제 처리
        voiceEvalRepository.delete(voiceEval);
        voiceRepository.delete(voice);
        
        log.info("음성 평가 완전 삭제 완료. 음성 ID: {}", voiceId);
    }
} 