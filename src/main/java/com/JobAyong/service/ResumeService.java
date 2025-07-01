package com.JobAyong.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.JobAyong.entity.Resume;
import com.JobAyong.entity.ResumeEval;
import com.JobAyong.repository.ResumeEvalRepository;
import com.JobAyong.repository.ResumeRepository;
import com.JobAyong.dto.ResumeRequest;
import com.JobAyong.dto.ResumeResponse;
import com.JobAyong.dto.ResumeEvalRequest;
import com.JobAyong.dto.ResumeEvalResponse;
import com.JobAyong.entity.User;
import com.JobAyong.repository.UserRepository;
import com.JobAyong.constant.ResumeType;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ResumeService {
    private final ResumeEvalRepository resumeEvalRepository;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final GPTService gptService;
    
    @Autowired
    public ResumeService(ResumeRepository resumeRepository, ResumeEvalRepository resumeEvalRepository, UserRepository userRepository, GPTService gptService) {
        this.resumeEvalRepository = resumeEvalRepository;
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.gptService = gptService;
    }

    public Resume save(Resume resume) {
        return resumeRepository.save(resume);
    }

    public Optional<Resume> findByResumeId(Integer resumeId) {
        return resumeRepository.findById(resumeId);
    }

    public List<Resume> findAllResume() {
        return resumeRepository.findAll();
    }

    public void deleteByResumeId(Integer resumeId) {
        resumeRepository.deleteById(resumeId);
    }



    public ResumeEval save(ResumeEval resumeEval) {
        return resumeEvalRepository.save(resumeEval);
    }

    public Optional<ResumeEval> findByResumeEvalId(Integer ResumeEvalId) {
        return resumeEvalRepository.findById(ResumeEvalId);
    }

    public List<ResumeEval> findAllResumeEval() {
        return resumeEvalRepository.findAll();
    }

    public void deleteById(Integer ResumeEvalId) {
        resumeEvalRepository.deleteById(ResumeEvalId);
    }

    // 특정 resume_id의 최대 버전 조회
    public Integer getMaxVersionByResumeId(Integer resumeId) {
        return resumeEvalRepository.findMaxVersionByResumeId(resumeId);
    }

    // ResumeRequest -> Resume 엔티티 변환 (User 객체를 파라미터로 받음)
    public Resume toResumeEntity(ResumeRequest dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        Resume resume = new Resume();
        resume.setUser(user);
        
        // 제목이 없거나 "제목 없음"인 경우 현재 시간으로 기본 제목 생성
        String title = dto.getResumeTitle();
        if (title == null || title.trim().isEmpty() || "제목 없음".equals(title)) {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            title = "자기소개서 " + now.format(formatter);
        }
        resume.setResumeTitle(title);
        
        resume.setResumeText(dto.getResumeText());
        
        // resumeType 설정 (기본값은 text)
        if (dto.getResumeType() != null) {
            try {
                resume.setResumeType(ResumeType.valueOf(dto.getResumeType()));
            } catch (IllegalArgumentException e) {
                resume.setResumeType(ResumeType.text); // 기본값
            }
        } else {
            resume.setResumeType(ResumeType.text); // 기본값
        }

        return resume;
    }

    // Resume 엔티티 -> ResumeResponse 변환
    public ResumeResponse toResumeResponse(Resume resume) {
        ResumeResponse dto = new ResumeResponse();
        dto.setResumeId(resume.getResumeId());
        dto.setResumeTitle(resume.getResumeTitle());
        dto.setResumeText(resume.getResumeText());
        dto.setResumeType(resume.getResumeType() != null ? resume.getResumeType().name() : "text");
        dto.setCreatedAt(resume.getCreatedAt());
        dto.setUpdatedAt(resume.getUpdatedAt());
        dto.setDeletedAt(resume.getDeletedAt());
        return dto;
    }

    // ResumeEvalRequest -> ResumeEval 엔티티 변환
    public ResumeEval toResumeEvalEntity(ResumeEvalRequest dto, Resume resume, User user) {
        ResumeEval eval = new ResumeEval();
        eval.setResume(resume);
        eval.setUser(user);
        
        // 평가 제목 처리: 프론트엔드에서 온 제목이 비어있거나 " 평가"만 있으면 Resume 제목 사용
        String evalTitle = dto.getResumeEvalTitle();
        if (evalTitle == null || evalTitle.trim().isEmpty() || evalTitle.equals(" 평가") || evalTitle.equals("평가")) {
            evalTitle = resume.getResumeTitle() + " 평가";
            System.out.println("⚠️ 프론트엔드 제목이 비어있음, Resume 제목 사용: \"" + evalTitle + "\"");
        }
        eval.setResumeEvalTitle(evalTitle);
        
        eval.setResumeOrg(dto.getResumeOrg());
        eval.setResumeImp(dto.getResumeImp());
        eval.setReason(dto.getReason());
        eval.setMissingAreas(dto.getMissingAreas());
        eval.setResumeFin(dto.getResumeFin());
        eval.setResumeEvalVersion(dto.getResumeEvalVersion());
        return eval;
    }

    // ResumeEval 엔티티 -> ResumeEvalResponse 변환
    public ResumeEvalResponse toResumeEvalResponse(ResumeEval eval) {
        ResumeEvalResponse dto = new ResumeEvalResponse();
        dto.setResumeEvalId(eval.getResumeEvalId());
        dto.setResumeId(eval.getResume().getResumeId());
        dto.setUserEmail(eval.getUser().getEmail());
        dto.setResumeEvalTitle(eval.getResumeEvalTitle());
        dto.setResumeOrg(eval.getResumeOrg());
        dto.setResumeImp(eval.getResumeImp());
        dto.setReason(eval.getReason());
        dto.setMissingAreas(eval.getMissingAreas());
        dto.setResumeFin(eval.getResumeFin());
        dto.setResumeEvalVersion(eval.getResumeEvalVersion());
        dto.setCreatedAt(eval.getCreatedAt());
        dto.setDeletedAt(eval.getDeletedAt());
        return dto;
    }

    // 파일에서 텍스트 추출
    public String extractTextFromFile(MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();
            if (fileName == null) {
                throw new RuntimeException("파일명을 확인할 수 없습니다.");
            }
            
            String extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
            
            switch (extension) {
                case ".pdf":
                    return extractTextFromPdf(file.getInputStream());
                case ".docx":
                    return extractTextFromDocx(file.getInputStream());
                case ".txt":
                    return new String(file.getBytes(), "UTF-8");
                default:
                    throw new RuntimeException("지원하지 않는 파일 형식입니다. PDF, DOCX, TXT 파일만 지원합니다.");
            }
        } catch (IOException e) {
            throw new RuntimeException("파일 읽기 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 파일에서 텍스트를 추출하고 GPT를 통해 자소서 내용만 정제하여 반환
     * @param file 업로드된 파일 (PDF, DOCX, TXT)
     * @return GPT로 정제된 자소서 텍스트
     */
    public String extractAndProcessResumeFromFile(MultipartFile file) {
        try {
            System.out.println("=== 자소서 추출 시작 ===");
            System.out.println("파일명: " + file.getOriginalFilename());
            System.out.println("파일 크기: " + file.getSize() + " bytes");
            
            // 1단계: 파일에서 생 텍스트 추출
            System.out.println("1단계: 파일에서 텍스트 추출 시작");
            String rawText = extractTextFromFile(file);
            
            if (rawText == null || rawText.trim().isEmpty()) {
                throw new RuntimeException("파일에서 텍스트를 추출할 수 없습니다.");
            }
            
            System.out.println("추출된 텍스트 길이: " + rawText.length() + " 문자");
            System.out.println("추출된 텍스트 미리보기: " + rawText.substring(0, Math.min(100, rawText.length())) + "...");
            
            // 2단계: GPT를 통해 자소서 내용만 추출
            System.out.println("2단계: GPT를 통한 자소서 내용 추출 시작");
            String processedResumeText = gptService.extractResumeContentFromRawText(rawText);
            
            System.out.println("GPT 처리 결과 길이: " + processedResumeText.length() + " 문자");
            System.out.println("GPT 처리 결과 미리보기: " + processedResumeText.substring(0, Math.min(100, processedResumeText.length())) + "...");
            
            // 3단계: 결과 검증
            if (processedResumeText.contains("자소서 내용을 찾을 수 없습니다.")) {
                throw new RuntimeException("업로드된 파일에서 자소서 내용을 찾을 수 없습니다.");
            }
            
            System.out.println("=== 자소서 추출 완료 ===");
            return processedResumeText;
            
        } catch (Exception e) {
            System.err.println("=== 자소서 추출 실패 ===");
            System.err.println("오류 메시지: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("자소서 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 파일에서 자소서를 추출하고 Resume 엔티티로 저장
     * @param file 업로드된 파일
     * @param user 사용자 정보
     * @param resumeTitle 자소서 제목
     * @return 저장된 Resume 엔티티
     */
    public Resume createResumeFromFile(MultipartFile file, User user, String resumeTitle) {
        try {
            System.out.println("=== 자소서 파일 저장 시작 ===");
            System.out.println("사용자: " + user.getEmail());
            System.out.println("제목: " + resumeTitle);
            
            // GPT를 통해 자소서 내용 추출
            System.out.println("자소서 내용 추출 시작...");
            String resumeText = extractAndProcessResumeFromFile(file);
            
            // Resume 엔티티 생성
            System.out.println("Resume 엔티티 생성 시작...");
            Resume resume = new Resume();
            resume.setUser(user);
            
            // 제목 설정 (파일 업로드인 경우 현재 시간 포함)
            String title = resumeTitle;
            if (title == null || title.trim().isEmpty()) {
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                title = "자기소개서 " + now.format(formatter);
            }
            resume.setResumeTitle(title);
            
            resume.setResumeText(resumeText);
            resume.setResumeType(ResumeType.file); // 파일 업로드이므로 file 타입으로 설정
            
            System.out.println("데이터베이스 저장 시작...");
            // 데이터베이스에 저장
            Resume savedResume = save(resume);
            
            System.out.println("저장 완료! Resume ID: " + savedResume.getResumeId());
            System.out.println("=== 자소서 파일 저장 완료 ===");
            
            return savedResume;
            
        } catch (Exception e) {
            System.err.println("=== 자소서 파일 저장 실패 ===");
            System.err.println("오류 메시지: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("파일에서 자소서 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    // PDF에서 텍스트 추출
    private String extractTextFromPdf(InputStream inputStream) throws IOException {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
    
    // DOCX에서 텍스트 추출
    private String extractTextFromDocx(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            return extractor.getText();
        }
    }

    /**
     * OpenAI GPT API 연결 테스트
     * @return 테스트 결과 메시지
     */
    public String testGPTConnection() {
        try {
            String testPrompt = "안녕하세요. 이것은 API 연결 테스트입니다. '연결 성공'이라고 답해주세요.";
            return gptService.askCustomInterViewGPT(testPrompt);
        } catch (Exception e) {
            throw new RuntimeException("GPT API 연결 테스트 실패: " + e.getMessage());
        }
    }


}
