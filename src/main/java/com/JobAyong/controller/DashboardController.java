package com.JobAyong.controller;

import com.JobAyong.dto.DashboardResponse;
import com.JobAyong.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, List<DashboardResponse>>> getDashboardStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }
}