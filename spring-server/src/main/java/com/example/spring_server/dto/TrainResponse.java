package com.example.spring_server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor          // Jackson이 JSON→객체 만들 때 빈 생성자 필요
public class TrainResponse {
    private String algorithm;
    private Hyperparameters hyperparameters;
    private Double accuracy;
    private Double loss;

    @Getter
    @NoArgsConstructor
    public static class Hyperparameters {
        private String optimizer;
        private Integer epochs;
        @JsonProperty("batch_size")
        private Integer batchSize;
        @JsonProperty("learning_rate")
        private Double learningRate;
    }
}
