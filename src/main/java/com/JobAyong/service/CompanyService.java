package com.JobAyong.service;

import com.JobAyong.entity.Company;
import com.JobAyong.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Transactional(readOnly = true)
    public Company findById(Long companyId){ return companyRepository.findById(companyId).orElseThrow(() -> new RuntimeException("유효한 회사 ID가 아닙니다.")); }

    /*@apiNote DB에 등록된 모든 회사정보에서 주요사업이 있는 회사부터 우선적으로 차출해 List 형식으로 가져오는 함수
    * @author 나세호
    * */
    @Transactional(readOnly = true)
    public List<Company> findAllOrderByMainBusinessExists(){ return companyRepository.findAllOrderByMainBusinessExists(); }


    /*@apiNote (페이지네이션용) DB에 등록된 모든 회사정보에서 주요사업이 있는 회사부터 우선적으로 차출해 List 형식으로 가져오는 함수
     * @author 최선아
     * */
    @Transactional(readOnly = true)
    public Page<Company> findAllByPage(Pageable pageable){ return companyRepository.findAllByPage(pageable); }


    /*@apiNote 회사 ID 존재 여부 true/false 반환
     * @author 최선아
     * */
    public Boolean findByCompanyId(int companyId){
        Optional<Company> companyOpt = companyRepository.findByCompanyId(companyId);
        if (companyOpt.isPresent()) {
            return true;
        }
        return false;
    }

    /*@apiNote 회사 비활성화/재활성화 처리
     * @author 최선아
     * */
    @Transactional
    public Boolean status(int companyId, boolean deactivate){
        Optional<Company> companyOpt = companyRepository.findByCompanyId(companyId);
        if (companyOpt.isPresent()) {
            Company company = companyOpt.get();
            System.out.println("삭제 시각: " + LocalDateTime.now());
            company.setDeletedAt(deactivate ? LocalDateTime.now() : null); // ✅ 분기 처리
            System.out.println("엔티티에 설정된 값: " + company.getDeletedAt());
            companyRepository.save(company);
            return true;
        }
        return false;
    }

}
