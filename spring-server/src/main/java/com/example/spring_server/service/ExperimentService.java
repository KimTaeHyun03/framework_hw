package com.example.spring_server.service;

import com.example.spring_server.dto.CreateExperimentRequest;
import com.example.spring_server.dto.TrainRequest;
import com.example.spring_server.dto.TrainResponse;
import com.example.spring_server.dto.UpdateExperimentRequest;
import com.example.spring_server.entity.ExperimentLog;
import com.example.spring_server.repository.ExperimentLogRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    // === 찬영: Delete (soft delete) ===

    // 물리 삭제가 아니라 deleted 플래그만 true 로 바꾼다.
    @Transactional  // 메서드가 끝날 때 변경 내용을 DB에 자동 커밋
    public void softDelete(Long id) {
        // deleted = false 인 레코드만 가져옴 → 이미 삭제된 것은 예외 발생
        ExperimentLog log = repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않거나 이미 삭제된 실험입니다. id=" + id));

        // DB를 직접 UPDATE 하지 않고 객체 필드만 바꿈
        // → @Transactional 덕분에 JPA가 변경을 감지(Dirty Checking)해서 자동 저장
        log.setDeleted(true);
    }
}