package com.JobAyong.controller;

import com.JobAyong.entity.Company;
import com.JobAyong.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public ResponseEntity<?> getCompanyList(@RequestParam(defaultValue = "0") int pn,
                                            @RequestParam(defaultValue = "10") int ps) {

        // ****************************************************************************************
        // 전체 리스트 반환
        // ****************************************************************************************
        if (pn < 0 || ps < 0) {
            // 전체 리스트 반환
            List<Company> allCompanies = companyService.findAllOrderByMainBusinessExists(); // 전체 리스트
            return ResponseEntity.ok(allCompanies);
        }
        // ****************************************************************************************


        // ****************************************************************************************
        // 페이지 네이션용 => 제한된 값 만큼 리스트 반환
        // ****************************************************************************************
        Pageable pageable = PageRequest.of(pn, ps); // pn페이지에 ps개
        return ResponseEntity.ok(companyService.findAllByPage(pageable));
        // ****************************************************************************************
    }


    /*@apiNote 회사 비활성화/재활성화
     * @author 최선아
     * */
    @PatchMapping("/status/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable("id") int companyId, @RequestParam("deactivate") boolean deactivate) {
        if (!companyService.findByCompanyId(companyId)) {
            return ResponseEntity.notFound().build();
        }

        Boolean res = companyService.status(companyId, deactivate);
        return ResponseEntity.ok().build();
    }
}
