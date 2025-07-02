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
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

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


            Voice voice = Voice.builder()
                    .user(user)
                    .fileName(request.getFileName())
                    .fileType(request.getFileType())
                    .fileSize(request.getFileSize())
                    .filePath(request.getFilePath())
                    .convertedFilePath(request.getConvertedFilePath())
                    .transcriptText(request.getTranscript())
                    .build();


            voiceRepository.saveAndFlush(voice);
            System.out.println("🎧 저장된 voiceId: " + voice.getVoiceId());

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

            System.out.println("DEBUG voice: id=" + voice.getVoiceId() +
                    ", user=" + (voice.getUser() == null ? "null" : voice.getUser().getEmail()));

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
     * 음성 파일 가져오기
     * @param voiceId 음성 ID
     * @return 음성 파일 Resource
     */
    public Resource getAudioFile(int voiceId) {
        try {
            log.info("음성 파일 요청 - voiceId: {}", voiceId);

            Voice voice = voiceRepository.findById(voiceId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 음성 ID: " + voiceId));

            String rawPath = voice.getConvertedFilePath().replace("\\", "/");
            log.info("음성 파일 정보 조회 성공 - filePath: {}", rawPath);

            Path absolutePath;

            // 절대 경로인지 확인
            if (Paths.get(rawPath).isAbsolute()) {
                absolutePath = Paths.get(rawPath);
            } else {
                if (rawPath.startsWith("/")) {
                    rawPath = rawPath.substring(1);
                }
                absolutePath = Paths.get(uploadDir, rawPath);
            }

            File file = absolutePath.toFile();

            log.info("변환된 절대 경로: {}", absolutePath);
            log.info("파일 존재 여부: {}", file.exists());

            if (!file.exists()) {
                log.error("음성 파일을 찾을 수 없음 - 경로: {}", absolutePath);
                throw new IllegalStateException("음성 파일을 찾을 수 없습니다: " + absolutePath);
            }

            if (!file.canRead()) {
                log.error("음성 파일 읽기 권한 없음 - 경로: {}", absolutePath);
                throw new IllegalStateException("음성 파일에 대한 읽기 권한이 없습니다: " + absolutePath);
            }

            return new FileSystemResource(file);
        } catch (Exception e) {
            log.error("음성 파일 가져오기 실패", e);
            throw e;
        }
    }
}
