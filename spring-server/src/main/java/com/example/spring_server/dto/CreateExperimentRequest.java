package com.example.spring_server.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateExperimentRequest {
    private Integer epochs;
    private Integer batchSize;
    private Double learningRate;
    private String memo;
    private String tag;
}

