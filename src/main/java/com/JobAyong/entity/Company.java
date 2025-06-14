package com.JobAyong.entity;

import com.JobAyong.constant.CompanySize;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "company")
@Getter
@Setter
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id")
    private Integer companyId;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "size")
    private CompanySize size;

    @Column(name = "industry", length = 100)
    private String industry;

    @Column(name = "employees")
    private Integer employees;

    @Column(name = "establishment")
    private LocalDate establishment;

    @Column(name = "CEO", length = 50)
    private String ceo;

    @Column(name = "revenue", length = 50)
    private String revenue;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "homepage", length = 200)
    private String homepage;

    @Column(columnDefinition = "TEXT")
    private String history;

    @Column(name = "main_business", length = 255)
    private String main_business;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 