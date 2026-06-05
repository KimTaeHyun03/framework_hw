package com.example.spring_server.service;

import com.example.spring_server.entity.ExperimentLog;
import com.example.spring_server.repository.ExperimentReadRepository;
import org.springframework.stereotype.Service;

import java.util.List;

// 윤서: Read(조회) 전용 Service
@Service
public class ExperimentReadService {

    private final ExperimentReadRepository repository;

    public ExperimentReadService(ExperimentReadRepository repository) {
        this.repository = repository;
    }

    // 목록 조회: 삭제 안 된 실험 전체를 최신순으로
    public List<ExperimentLog> findAll() {
        return repository.findByDeletedFalseOrderByCreatedAtDesc();
    }

    // 상세 조회: id로 1건. 없거나 이미 삭제됐으면 예외
    public ExperimentLog findOne(Long id) {
        return repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("실험을 찾을 수 없습니다. id=" + id));
    }
}