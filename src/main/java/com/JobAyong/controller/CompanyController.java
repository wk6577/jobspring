package com.JobAyong.controller;

import com.JobAyong.dto.CreateCompanyRequest;
import com.JobAyong.dto.UpdateCompanyRequest;
import com.JobAyong.entity.Company;
import com.JobAyong.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    /**
     * @apiNote DB에 등록된 모든 회사정보에서 주요사업이 있는 회사부터 우선적으로 차출해 List 형식으로 가져오는 API
     * @author 나세호
     **/
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

    /**
     * @apiNote 회사 등록 요청
     * @author 최선아
     *
     * @param request 신규 회사 정보
     * @return 201 Created + true (성공), 500 Internal Server Error + false (실패)
     */
    @PostMapping
    public ResponseEntity<Boolean> addCompany(@RequestBody CreateCompanyRequest request) {
        boolean success = companyService.addCompany(request);

        if (success) {
            return ResponseEntity.status(HttpStatus.CREATED).body(true);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    /**
     * @apiNote 회사 수정 정보 업데이트
     * @author 최선아
     *
     * @param id 업데이트할 회사 ID
     * @param request 회사 업데이트 정보
     * @return 200 OK + true (성공), 500 Internal Server Error + false (실패)
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCompany(@PathVariable Integer id, @RequestBody UpdateCompanyRequest request){
        Boolean exit = companyService.findByCompanyId(id);
        Map<String, Object> response = new HashMap<>();
        if(exit){
            Boolean success = companyService.updateCompany(id, request);
            if(success){
                response.put("success", success);
                response.put("message", success ? "회사 업데이트 성공." : "회사 업데이트에 실패함.");
                return ResponseEntity
                        .status(success ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(response);
            }
        }else{
            response.put("success", false);
            response.put("message", "존재하지 않는 회사 ID입니다.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * @apiNote 회사 비활성화/재활성화
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
