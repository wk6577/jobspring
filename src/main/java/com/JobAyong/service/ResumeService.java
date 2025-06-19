package com.JobAyong.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.JobAyong.entity.Resume;
import com.JobAyong.entity.ResumeEval;
import com.JobAyong.repository.ResumeEvalRepository;
import com.JobAyong.repository.ResumeRepository;
import com.JobAyong.dto.ResumeRequest;
import com.JobAyong.dto.ResumeResponse;
import com.JobAyong.dto.ResumeEvalRequest;
import com.JobAyong.dto.ResumeEvalResponse;
import com.JobAyong.entity.User;
import com.JobAyong.constant.ResumeType;

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
        resume.setResumeFile(dto.getResumeFile());
        // String -> Enum 변환
        if (dto.getResumeType() != null) {
            resume.setResumeType(ResumeType.valueOf(dto.getResumeType().toUpperCase()));
        }
        return resume;
    }

    // Resume 엔티티 -> ResumeResponse 변환
    public ResumeResponse toResumeResponse(Resume resume) {
        ResumeResponse dto = new ResumeResponse();
        dto.setResumeId(resume.getResumeId());
        dto.setUserEmail(resume.getUser() != null ? resume.getUser().getEmail() : null);
        dto.setResumeTitle(resume.getResumeTitle());
        dto.setResumeText(resume.getResumeText());
        dto.setResumeFile(resume.getResumeFile());
        // Enum -> String 변환
        dto.setResumeType(resume.getResumeType() != null ? resume.getResumeType().name().toLowerCase() : null);
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
}
