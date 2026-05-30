package com.example.spring_server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor          // Service에서 new TrainRequest(e, b, lr) 로 만들려고
public class TrainRequest {
    private Integer epochs;
    @JsonProperty("batch_size")     // 자바=batchSize, JSON=batch_size
    private Integer batchSize;
    @JsonProperty("learning_rate")
    private Double learningRate;
}