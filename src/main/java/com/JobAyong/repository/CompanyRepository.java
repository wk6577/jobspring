package com.JobAyong.repository;

import com.JobAyong.entity.Company;
import com.JobAyong.constant.CompanySize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    List<Company> findByNameContaining(String name);
    List<Company> findByIndustry(String industry);
    List<Company> findBySize(CompanySize size);
} 