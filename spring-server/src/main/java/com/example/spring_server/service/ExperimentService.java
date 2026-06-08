package com.example.spring_server.service;

import com.example.spring_server.dto.CreateExperimentRequest;
import com.example.spring_server.dto.TrainRequest;
import com.example.spring_server.dto.TrainResponse;
import com.example.spring_server.dto.UpdateExperimentRequest;
import com.example.spring_server.entity.ExperimentLog;
import com.example.spring_server.repository.ExperimentLogRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ExperimentService {

    private final WebClient mlWebClient;
    private final ExperimentLogRepository repository;

    // 생성자 주입 (3단계 빈 + 1단계 repository 를 Spring이 넣어줌)
    public ExperimentService(WebClient mlWebClient, ExperimentLogRepository repository) {
        this.mlWebClient = mlWebClient;
        this.repository = repository;
    }

    public ExperimentLog create(CreateExperimentRequest req) {
        // ① ML 서버에 학습 요청 만들기
        TrainRequest trainRequest =
                new TrainRequest(req.getEpochs(), req.getBatchSize(), req.getLearningRate());

        // ② /train 호출하고 결과 올 때까지 기다림(.block)
        TrainResponse result = mlWebClient.post()
                .uri("/train")
                .bodyValue(trainRequest)
                .retrieve()
                .bodyToMono(TrainResponse.class)
                .block();

        // ③ 중첩 응답 → 평평한 엔티티로 펼치기 + 메모/태그 합치기
        TrainResponse.Hyperparameters hp = result.getHyperparameters();
        ExperimentLog log = ExperimentLog.builder()
                .algorithm(result.getAlgorithm())
                .optimizer(hp.getOptimizer())
                .epochs(hp.getEpochs())
                .batchSize(hp.getBatchSize())
                .learningRate(hp.getLearningRate())
                .accuracy(result.getAccuracy())
                .loss(result.getLoss())
                .memo(req.getMemo())
                .tag(req.getTag())
                .deleted(false)
                .build();   // createdAt 은 안 넣음 → @CreationTimestamp 자동

        // ④ DB 저장 (저장된 객체엔 id, createdAt 채워져서 돌아옴)
        return repository.save(log);
    }

    // === 윤서: Read (조회) ===

    // 목록 조회: 삭제 안 된 실험 전체를 최신순으로
    public List<ExperimentLog> findAll() {
        return repository.findByDeletedFalseOrderByCreatedAtDesc();
    }

    // 상세 조회: id로 1건. 없거나 이미 삭제됐으면 예외
    public ExperimentLog findOne(Long id) {
        return repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("실험을 찾을 수 없습니다. id=" + id));
    }

    // === 기연: Update (수정) ===

    // soft-delete 된 레코드는 안 보이게. 없거나 삭제됐으면 404.
    public ExperimentLog findActive(Long id) {
        return repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    // memo/tag 만 수정한다. ML 값/하이퍼파라미터/메타 필드는 손대지 않는다.
    public ExperimentLog update(Long id, UpdateExperimentRequest req) {
        ExperimentLog log = findActive(id);
        log.setMemo(req.getMemo());
        log.setTag(req.getTag());
        return repository.save(log);
    }
}