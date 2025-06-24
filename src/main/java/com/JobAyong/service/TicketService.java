package com.JobAyong.service;

import com.JobAyong.entity.Ticket;
import com.JobAyong.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;

    @Transactional
    public void answerTicket(Long ticketId, String answer) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("티켓이 존재하지 않음"));
        ticket.setAnswer(answer);
        ticket.setStatus("done");
        ticketRepository.save(ticket);
    }

    @Transactional
    public void updateTicket(Long ticketId, String newComment) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("티켓이 존재하지 않음"));
        ticket.setTicketComment(newComment);
        ticketRepository.save(ticket);
    }

    @Transactional
    public void deleteTicket(Long ticketId) {
        ticketRepository.deleteById(ticketId);
    }
}
