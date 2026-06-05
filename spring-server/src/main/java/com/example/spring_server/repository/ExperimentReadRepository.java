package com.example.spring_server.repository;

import com.example.spring_server.entity.ExperimentLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// 윤서: Read(조회) 전용 Repository
public interface ExperimentReadRepository extends JpaRepository<ExperimentLog, Long> {

    // 목록: 삭제 안 된 실험만, 최신순(createdAt 내림차순)으로
    // → SELECT * FROM experiment_log WHERE deleted = false ORDER BY created_at DESC
    List<ExperimentLog> findByDeletedFalseOrderByCreatedAtDesc();

    // 상세: 해당 id이면서 삭제 안 된 실험 1건
    // → SELECT * FROM experiment_log WHERE id = ? AND deleted = false
    Optional<ExperimentLog> findByIdAndDeletedFalse(Long id);
}