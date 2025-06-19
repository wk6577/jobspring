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

@Service
public class ResumeService {
    private final ResumeEvalRepository resumeEvalRepository;
    private final ResumeRepository resumeRepository;
    
    @Autowired
    public ResumeService(ResumeRepository resumeRepository,ResumeEvalRepository resumeEvalRepository) {
        this.resumeEvalRepository = resumeEvalRepository;
        this.resumeRepository = resumeRepository;
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

    // ResumeRequest -> Resume 엔티티 변환 (User 객체를 파라미터로 받음)
    public Resume toResumeEntity(ResumeRequest dto, User user) {
        Resume resume = new Resume();
        resume.setUser(user);
        resume.setResumeTitle(dto.getResumeTitle());
        resume.setResumeText(dto.getResumeText());
        // String -> Enum 변환

        return resume;
    }

    // Resume 엔티티 -> ResumeResponse 변환
    public ResumeResponse toResumeResponse(Resume resume) {
        ResumeResponse dto = new ResumeResponse();
        dto.setResumeId(resume.getResumeId());
        dto.setUserEmail(resume.getUser() != null ? resume.getUser().getEmail() : null);
        dto.setResumeTitle(resume.getResumeTitle());
        dto.setResumeText(resume.getResumeText());
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
        eval.setResumeEvalComment(dto.getResumeEvalComment());
        eval.setResumeOrg(dto.getResumeOrg());
        eval.setResumeLog(dto.getResumeLog());
        eval.setResumeEvalVersion(dto.getResumeEvalVersion());
        return eval;
    }

    // ResumeEval 엔티티 -> ResumeEvalResponse 변환
    public ResumeEvalResponse toResumeEvalResponse(ResumeEval eval) {
        ResumeEvalResponse dto = new ResumeEvalResponse();
        dto.setResumeEvalId(eval.getResumeEvalId());
        dto.setResumeId(eval.getResume().getResumeId());
        dto.setUserEmail(eval.getUser().getEmail());
        dto.setResumeEvalComment(eval.getResumeEvalComment());
        dto.setResumeOrg(eval.getResumeOrg());
        dto.setResumeLog(eval.getResumeLog());
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
}
