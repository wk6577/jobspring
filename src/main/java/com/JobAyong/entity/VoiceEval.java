package com.JobAyong.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "voice_eval")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoiceEval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int evalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voice_id", nullable = false)
    private Voice voice;


    @Lob
    private String transcript;

    private int overallScore;
    private int clarityScore;
    private int speedScore;
    private int volumeScore;
    private int confidenceScore;

    private int wordsPerMinute;
    private float pauseDuration;
    private int intonation;
    private int pronunciation;
    private int fillersCount;

    @Column(columnDefinition = "json")
    private String metricGradesJson;

    @Column(columnDefinition = "json")
    private String voicePatternsJson;

    @Column(columnDefinition = "json")
    private String strengthsJson;

    @Column(columnDefinition = "json")
    private String improvementsJson;

    @Column(columnDefinition = "json")
    private String strategiesJson;

    @Column(columnDefinition = "text")
    private String interviewerComment;

    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
