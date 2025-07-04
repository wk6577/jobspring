package com.JobAyong.service;

import com.JobAyong.dto.CreateVoiceEvalRequest;
import com.JobAyong.dto.CreateVoiceRequest;
import com.JobAyong.entity.User;
import com.JobAyong.entity.Voice;
import com.JobAyong.entity.VoiceEval;
import com.JobAyong.repository.UserRepository;
import com.JobAyong.repository.VoiceEvalRepository;
import com.JobAyong.repository.VoiceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VoiceService {

    private final VoiceRepository voiceRepository;
    private final VoiceEvalRepository voiceEvalRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.dir:${user.dir}/jobAPI/api/data}")
    private String uploadDir;

    @Transactional
    public int addVoice(CreateVoiceRequest request){
        try {
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 이메일: " + request.getEmail()));

            // base64 데이터를 바이너리로 디코딩
            byte[] wavData = null;
            if (request.getWavBinaryBase64() != null && !request.getWavBinaryBase64().isEmpty()) {
                try {
                    // base64 문자열에서 데이터 URL 접두사 제거 (예: "data:audio/wav;base64,")
                    String base64Data = request.getWavBinaryBase64();
                    if (base64Data.contains(",")) {
                        base64Data = base64Data.split(",")[1];
                        log.info("데이터 URL 접두사 제거됨, 순수 base64 길이: {}", base64Data.length());
                    }
                    
                    wavData = Base64.getDecoder().decode(base64Data);
                    log.info("base64 WAV 데이터 디코딩 성공 - 원본: {} chars → 바이너리: {} bytes", 
                             request.getWavBinaryBase64().length(), wavData.length);
                } catch (IllegalArgumentException e) {
                    log.error("base64 디코딩 실패: {}, 원본 데이터 길이: {}", e.getMessage(), request.getWavBinaryBase64().length());
                    log.error("base64 데이터 샘플 (처음 100자): {}", 
                             request.getWavBinaryBase64().substring(0, Math.min(100, request.getWavBinaryBase64().length())));
                } catch (Exception e) {
                    log.error("예상치 못한 디코딩 오류: {}", e.getMessage(), e);
                }
            } else if (request.getConvertedFilePath() != null) {
                // 기존 파일 경로 방식 호환성 유지
                try {
                    Path wavPath = Paths.get(request.getConvertedFilePath());
                    if (Files.exists(wavPath)) {
                        wavData = Files.readAllBytes(wavPath);
                        log.info("WAV 파일을 바이너리로 읽어옴 - 파일 경로: {}, 크기: {} bytes", 
                                request.getConvertedFilePath(), wavData.length);
                    } else {
                        log.warn("WAV 파일이 존재하지 않음: {}", request.getConvertedFilePath());
                    }
                } catch (IOException e) {
                    log.error("WAV 파일 읽기 실패: {}, 파일 경로: {}", e.getMessage(), request.getConvertedFilePath());
                }
            } else {
                log.warn("WAV 바이너리 데이터와 파일 경로 모두 제공되지 않음");
            }

            Voice voice = Voice.builder()
                    .user(user)
                    .fileName(request.getFileName())
                    .fileType(request.getFileType())
                    .fileSize(request.getFileSize())
                    .filePath(request.getFilePath())
                    .convertedFilePath(request.getConvertedFilePath())
                    .wavData(wavData)
                    .transcriptText(request.getTranscript())
                    .build();

            Voice savedVoice = voiceRepository.saveAndFlush(voice);
            log.info("음성 데이터 DB 저장 완료 - voiceId: {}, 입력 wavData size: {} bytes", 
                     savedVoice.getVoiceId(), wavData != null ? wavData.length : 0);

            // DB에서 다시 조회하여 실제 저장된 데이터 검증
            Voice verifyVoice = voiceRepository.findById(savedVoice.getVoiceId()).orElse(null);
            if (verifyVoice != null) {
                if (verifyVoice.getWavData() != null && verifyVoice.getWavData().length > 0) {
                    log.info("DB에서 음성 바이너리 데이터 저장 확인됨 - voiceId: {}, 실제 저장된 size: {} bytes", 
                             verifyVoice.getVoiceId(), verifyVoice.getWavData().length);
                } else {
                    log.error("음성 바이너리 데이터가 DB에 저장되지 않음 - voiceId: {}", verifyVoice.getVoiceId());
                    log.error("원본 요청 정보 - wavBinaryBase64 null 여부: {}, 파일 경로: {}", 
                             request.getWavBinaryBase64() == null, request.getConvertedFilePath());
                }
            } else {
                log.error("DB에서 저장된 Voice 엔티티 조회 실패 - voiceId: {}", savedVoice.getVoiceId());
            }

            return savedVoice.getVoiceId();
        } catch (Exception e) {
            throw new RuntimeException("음성 파일 저장 중 오류 발생", e);
        }
    }

    @Transactional
    public Boolean addVoiceEval(CreateVoiceEvalRequest request){
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            // voice 조회
            Voice voice = voiceRepository.findById(request.getVoiceId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 voiceId 없음: " + request.getVoiceId()));

            log.info("DEBUG voice: id={}, user={}", voice.getVoiceId(), 
                     voice.getUser() == null ? "null" : voice.getUser().getEmail());

            VoiceEval voiceEval = VoiceEval.builder()
                    .voice(voice)
                    .transcript(request.getTranscript())
                    .overallScore(request.getOverallScore())
                    .clarityScore(request.getClarityScore())
                    .speedScore(request.getSpeedScore())
                    .volumeScore(request.getVolumeScore())
                    .confidenceScore(request.getConfidenceScore())
                    .wordsPerMinute(request.getWordsPerMinute())
                    .pauseDuration(request.getPauseDuration())
                    .intonation(request.getIntonation())
                    .pronunciation(request.getPronunciation())
                    .fillersCount(request.getFillersCount())
                    .metricGradesJson(objectMapper.writeValueAsString(request.getMetricGradesJson()))
                    .voicePatternsJson(objectMapper.writeValueAsString(request.getVoicePatternsJson()))
                    .strengthsJson(objectMapper.writeValueAsString(request.getStrengthsJson()))
                    .improvementsJson(objectMapper.writeValueAsString(request.getImprovementsJson()))
                    .strategiesJson(objectMapper.writeValueAsString(request.getStrategiesJson()))
                    .interviewerComment(request.getInterviewerComment())
                    .build();

            voiceEvalRepository.save(voiceEval);

            return true;

        } catch (Exception e) {
            throw new RuntimeException("음성 평가 저장 중 오류 발생", e);
        }
    }

    /**
     * 음성 파일 가져오기 (DB에서 바이너리 데이터 조회)
     * @param voiceId 음성 ID
     * @return 음성 파일 Resource
     */
    public Resource getAudioFile(int voiceId) {
        try {
            log.info("🎧 음성 파일 요청 - voiceId: {}", voiceId);

            Voice voice = voiceRepository.findById(voiceId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 음성 ID: " + voiceId));

            if (voice.getWavData() == null || voice.getWavData().length == 0) {
                log.error("음성 바이너리 데이터가 없음 - voiceId: {}", voiceId);
                throw new IllegalStateException("음성 바이너리 데이터를 찾을 수 없습니다");
            }

            log.info("음성 바이너리 데이터 조회 성공 - voiceId: {}, size: {} bytes", 
                     voiceId, voice.getWavData().length);

            return new ByteArrayResource(voice.getWavData());
        } catch (Exception e) {
            log.error("음성 파일 가져오기 실패", e);
            throw e;
        }
    }
}
