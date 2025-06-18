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

    @GetMapping("/{email}")
    public ResponseEntity<List<Ticket>> getInquiriesByEmail(@PathVariable String email) {
        List<Ticket> list = ticketRepository.findByEmailOrderByCreatedAtDesc(email);
        return ResponseEntity.ok(list);
    }
}
