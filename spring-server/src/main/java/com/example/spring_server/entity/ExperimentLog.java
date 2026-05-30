package com.example.spring_server.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExperimentLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // === ML 결과 필드 (ML 서버가 준 값 그대로 저장) ===
    private String algorithm;
    private String optimizer;
    private Integer epochs;
    private Integer batchSize;
    private Double learningRate;
    private Double accuracy;
    private Double loss;

    // === 메타 필드 (Spring이 채움) ===
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    private String memo;
    private String tag;

    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;
}
