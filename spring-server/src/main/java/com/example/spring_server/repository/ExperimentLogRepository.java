package com.example.spring_server.repository;

import com.example.spring_server.entity.ExperimentLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExperimentLogRepository extends JpaRepository<ExperimentLog, Long> {
}
