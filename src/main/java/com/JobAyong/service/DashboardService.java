package com.JobAyong.service;

import com.JobAyong.dto.DashboardResponse;
import com.JobAyong.repository.UserRepository;
import com.JobAyong.repository.InterviewArchiveRepository;
import com.JobAyong.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final InterviewArchiveRepository interviewArchiveRepository;
    private final TicketRepository ticketRepository;

    public Map<String, List<DashboardResponse>> getStats() {
        Map<String, List<DashboardResponse>> result = new LinkedHashMap<>();

        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);

        result.put("user", List.of(
                new DashboardResponse("총 사용자 수", userRepository.count(), "👥"),
                new DashboardResponse("이번 주 신규 가입자", userRepository.countByCreatedAtAfterAndDeletedAtIsNull(weekAgo), "✨")
        ));

        result.put("interview", List.of(
                new DashboardResponse("이번 주 면접 수", interviewArchiveRepository.countByCreatedAtAfterAndDeletedAtIsNull(weekAgo), "🎤")
        ));

        result.put("ticket", List.of(
                new DashboardResponse("1:1 문의 수", ticketRepository.count(), "📩"),
                new DashboardResponse("답변 완료 수", ticketRepository.countByStatus("done"), "✅")
        ));

        result.put("user_delete", List.of(
                new DashboardResponse("탈퇴 요청 수", userRepository.countByStatus("DELETE_WAITING"), "❌")
        ));

        return result;
    }
}