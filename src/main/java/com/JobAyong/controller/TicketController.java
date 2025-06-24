package com.JobAyong.controller;

import com.JobAyong.entity.Ticket;
import com.JobAyong.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/inquiries")
public class TicketController {

    @Autowired
    private TicketRepository ticketRepository;

    @PostMapping
    public ResponseEntity<?> createInquiry(@RequestBody Ticket ticket) {
        try {
            ticket.setStatus("pending");
            ticket.setCreatedAt(LocalDateTime.now());
            Ticket saved = ticketRepository.save(ticket);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("서버 오류: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteInquiry(@PathVariable Long id) {
        try {
            ticketRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("삭제 실패: " + e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateInquiry(@PathVariable Long id, @RequestBody Ticket ticket) {
        try {
            Ticket existingTicket = ticketRepository.findById(id).orElseThrow(() -> new RuntimeException("Ticket not found"));
            existingTicket.setTicketComment(ticket.getTicketComment());
            existingTicket.setStatus(ticket.getStatus());
            ticketRepository.save(existingTicket);
            return ResponseEntity.ok(existingTicket);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("수정 실패: " + e.getMessage());
        }
    }

    @GetMapping("/{email}")
    public ResponseEntity<List<Ticket>> getInquiriesByEmail(@PathVariable String email) {
        List<Ticket> list = ticketRepository.findByEmailOrderByCreatedAtDesc(email);
        return ResponseEntity.ok(list);
    }

    // ✅ 추가: 전체 조회
    @GetMapping("/all")
    public ResponseEntity<List<Ticket>> getAllTickets() {
        List<Ticket> tickets = ticketRepository.findAll();
        return ResponseEntity.ok(tickets);
    }

    // ✅ 추가: 답변 등록
    @PatchMapping("/{id}/answer")
    public ResponseEntity<?> answerInquiry(@PathVariable Long id, @RequestBody AnswerRequest request) {
        try {
            Ticket ticket = ticketRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("문의가 존재하지 않음"));
            ticket.setAnswer(request.getAnswer());
            ticket.setStatus("done");
            ticketRepository.save(ticket);
            return ResponseEntity.ok("답변 완료");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("답변 실패: " + e.getMessage());
        }
    }

    // 내부 DTO 클래스
    static class AnswerRequest {
        private String answer;
        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
    }
}
