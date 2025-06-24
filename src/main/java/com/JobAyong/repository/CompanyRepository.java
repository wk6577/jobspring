package com.JobAyong.repository;

import com.JobAyong.entity.Company;
import com.JobAyong.constant.CompanySize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    List<Company> findByNameContaining(String name);
    List<Company> findByIndustry(String industry);
    List<Company> findBySize(CompanySize size);
    Optional<Company> findByCompanyId(int companyId);

    /*@apiNote DB에서 주요사업을 갖고 있는 회사 부터 오름차순 차출 함수
    * @author 나세호
    * */
    @Query("SELECT c FROM Company c ORDER BY " +
            "CASE WHEN c.mainBusiness IS NULL OR c.mainBusiness = '' THEN 1 ELSE 0 END ASC, " +
            "CASE c.size " +
            "WHEN 'LARGE' THEN 0 " +
            "WHEN 'MEDIUM' THEN 1 " +
            "WHEN 'PUBLIC' THEN 2 " +
            "ELSE 3 END ASC")
    List<Company> findAllOrderByMainBusinessExists();


    /*@apiNote (페이지네이션용) DB에서 주요사업을 갖고 있는 회사 부터 오름차순 차출 함수
     * @author 최선아
     * */
    @Query("SELECT c FROM Company c ORDER BY " +
            "CASE WHEN c.mainBusiness IS NULL OR c.mainBusiness = '' THEN 1 ELSE 0 END ASC, " +
            "CASE c.size " +
            "WHEN 'LARGE' THEN 0 " +
            "WHEN 'MEDIUM' THEN 1 " +
            "WHEN 'PUBLIC' THEN 2 " +
            "ELSE 3 END ASC")
    Page<Company> findAllByPage(Pageable pageable);
}