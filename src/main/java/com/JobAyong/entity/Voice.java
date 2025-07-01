package com.JobAyong.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "voice")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Voice {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Voice)) return false;
        Voice v = (Voice) o;
        return Objects.equals(this.voiceId, v.voiceId);
    }
    @Override
    public int hashCode() {
        return Objects.hash(voiceId);
    }

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

    @Column(name = "converted_file_path", length = 500)
    private String convertedFilePath;

    @Column(name = "transcript_text", length = 500)
    private String transcriptText;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 