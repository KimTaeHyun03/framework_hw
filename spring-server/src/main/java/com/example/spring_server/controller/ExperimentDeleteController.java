package com.example.spring_server.controller;

import com.example.spring_server.service.ExperimentService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller   // ← RestController 아님! 화면 이름(리다이렉트)을 반환
public class ExperimentDeleteController {

    private final ExperimentService service;

    public ExperimentDeleteController(ExperimentService service) {
        this.service = service;
    }

    // POST /experiments/{id}/delete
    // HTML form은 GET/POST만 지원하므로 삭제도 POST로 받음
    // (목록 GET /experiments 매핑은 윤서 Read 컨트롤러가 담당 → 여기선 delete만)
    @PostMapping("/experiments/{id}/delete")
    public String delete(@PathVariable Long id) {
        service.softDelete(id);
        return "redirect:/experiments";   // 삭제 후 목록으로
    }
}
