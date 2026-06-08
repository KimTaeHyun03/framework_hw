package com.example.spring_server.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateExperimentRequest {
    private String memo;
    private String tag;
}

