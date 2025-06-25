package com.JobAyong.dto;

import com.JobAyong.entity.Ticket;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TicketResponse {
    private Long ticketId;
    private String email;
    private String ticketComment;
    private String status;
    private String answer;
    private LocalDateTime createdAt;

    public static TicketResponse fromEntity(Ticket ticket) {
        return TicketResponse.builder()
                .ticketId(ticket.getTicketId())
                .email(ticket.getEmail())
                .ticketComment(ticket.getTicketComment())
                .status(ticket.getStatus())
                .answer(ticket.getAnswer())
                .createdAt(ticket.getCreatedAt())
                .build();
    }
}
