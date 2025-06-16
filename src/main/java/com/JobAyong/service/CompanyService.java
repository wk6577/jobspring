package com.JobAyong.service;

import com.JobAyong.entity.Company;
import com.JobAyong.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
}
