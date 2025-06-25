package com.JobAyong.service;

import com.JobAyong.dto.InterviewArchiveResponse;
import com.JobAyong.entity.InterviewArchive;
import com.JobAyong.repository.InterviewArchiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InterviewArchiveService {

    private final InterviewArchiveRepository interviewArchiveRepository;

    public List<InterviewArchiveResponse> getAllArchives() {
        List<InterviewArchive> archives = interviewArchiveRepository.findAll();
        return archives.stream()
                .map(InterviewArchiveResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
