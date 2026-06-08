package com.example.spring_server.controller;

import com.example.spring_server.dto.CreateExperimentRequest;
import com.example.spring_server.dto.UpdateExperimentRequest;
import com.example.spring_server.entity.ExperimentLog;
import com.example.spring_server.service.ExperimentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller   // ← RestController 아님! 화면 이름을 반환
public class ExperimentViewController {

    private final ExperimentService service;

    public ExperimentViewController(ExperimentService service) {
        this.service = service;
    }

    // 입력 폼 보여주기 (기본값 미리 채워서 → 사용자가 수정)
    @GetMapping("/experiments/new")
    public String newForm(Model model) {
        CreateExperimentRequest form = new CreateExperimentRequest();
        form.setEpochs(50);
        form.setBatchSize(16);
        form.setLearningRate(0.01);
        model.addAttribute("form", form);
        return "experimentForm";   // → templates/experiment-form.mustache
    }

    // 폼 제출 → 학습 + 저장 → 목록으로 리다이렉트
    @PostMapping("/experiments")
    public String submit(@ModelAttribute CreateExperimentRequest form) {
        service.create(form);
        return "redirect:/experiments";   // 저장 후 목록으로 (윤서 Read 페이지)
    }

    // === 윤서: Read (조회) ===

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

    // === 기연: Update (수정) ===

    // 수정 폼 — ML 결과는 read-only로 보여주고 memo/tag만 입력받는다
    @GetMapping("/experiments/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        ExperimentLog experiment = service.findActive(id);
        model.addAttribute("experiment", experiment);
        return "experimentEditForm";
    }

    // 수정 제출 → 단일 조회로 리다이렉트
    @PostMapping("/experiments/{id}")
    public String updateSubmit(@PathVariable Long id, @ModelAttribute UpdateExperimentRequest form) {
        service.update(id, form);
        return "redirect:/experiments/" + id;
    }
}
