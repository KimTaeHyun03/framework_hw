package com.example.spring_server.controller;

import com.example.spring_server.service.ExperimentReadService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// 윤서: Read(조회) 전용 Controller
@Controller
public class ExperimentReadController {

    private final ExperimentReadService service;

    public ExperimentReadController(ExperimentReadService service) {
        this.service = service;
    }

    // 목록: 과거 실험 결과 전체 조회
    @GetMapping("/experiments")
    public String list(Model model) {
        model.addAttribute("experiments", service.findAll());
        return "experimentList";   // → templates/experimentList.mustache
    }

    // 상세: 실험 1건의 설정값 조회 (URL의 {id}를 PathVariable로 받음)
    @GetMapping("/experiments/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("experiment", service.findOne(id));
        return "experimentDetail";   // → templates/experimentDetail.mustache
    }
}