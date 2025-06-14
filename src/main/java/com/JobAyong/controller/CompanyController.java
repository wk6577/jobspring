package com.JobAyong.controller;

import com.JobAyong.entity.Company;
import com.JobAyong.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    /*@apiNote DB에 등록된 모든 회사정보에서 주요사업이 있는 회사부터 우선적으로 차출해 List 형식으로 가져오는 API
    * @author 나세호
    * */
    @GetMapping
    public ResponseEntity<List<Company>> getCompanyList(){
        return ResponseEntity.ok(companyService.findAllOrderByMainBusinessExists());
    }
}
