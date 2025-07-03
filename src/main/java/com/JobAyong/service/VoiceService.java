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
                    wavData = Base64.getDecoder().decode(request.getWavBinaryBase64());
                    log.info("base64 WAV 데이터를 바이너리로 디코딩 완료 - 크기: {} bytes", wavData.length);
                } catch (IllegalArgumentException e) {
                    log.error("base64 디코딩 실패: {}", e.getMessage());
                }
            } else if (request.getConvertedFilePath() != null) {
                // 기존 파일 경로 방식 호환성 유지
                try {
                    Path wavPath = Paths.get(request.getConvertedFilePath());
                    if (Files.exists(wavPath)) {
                        wavData = Files.readAllBytes(wavPath);
                        log.info("WAV 파일을 바이너리로 읽어옴 - 크기: {} bytes", wavData.length);
                    }
                } catch (IOException e) {
                    log.error("WAV 파일 읽기 실패: {}", e.getMessage());
                }
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

            voiceRepository.saveAndFlush(voice);
            log.info("음성 데이터 DB 저장 완료 - voiceId: {}, wavData size: {} bytes", 
                     voice.getVoiceId(), wavData != null ? wavData.length : 0);

            return voice.getVoiceId();
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
            log.info("음성 파일 요청 - voiceId: {}", voiceId);

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
