package com.JobAyong.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "voice")
@Getter
@Setter
public class Voice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "voice_id")
    private Integer voiceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email", nullable = false)
    private User user;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_type", length = 50)
    private String fileType;

    @Column(name = "file_size")
    private Integer fileSize;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 