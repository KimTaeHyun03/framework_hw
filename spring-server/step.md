# Create 기능 체크리스트 (담당: 태현)

> 흐름: 학습 요청 → ML 서버 호출 → 결과 그대로 H2 저장
> ML이 준 값(accuracy/loss/hyperparameters)은 **수정 없이 그대로** 저장한다.

## 0. 패키지 구조 (`com.example.spring_server` 아래)
- [ ] `domain` `dto` `repository` `service` `controller` `config` 생성

## 1. 엔티티 — `domain/ExperimentLog`
- [ ] `@Entity` 선언, `id`(자동생성)
- [ ] ML 결과 필드: `algorithm`, `optimizer`, `epochs`, `batchSize`, `learningRate`, `accuracy`, `loss`
- [ ] 메타 필드: `createdAt`, `memo`, `tag`, `deleted`(기본 false)

## 2. DTO — `dto`
- [ ] `TrainRequest` : `epochs`, `batchSize`, `learningRate` (ML로 보낼 값)
- [ ] `TrainResponse` : `algorithm`, `hyperparameters{optimizer,epochs,batchSize,learningRate}`, `accuracy`, `loss`
- [ ] (선택) 사용자 응답용 DTO

## 3. WebClient 설정 — `config`
- [ ] `WebClient` 빈 등록, `baseUrl = http://localhost:8000`
- [ ] base-url 은 `application.properties` 로 분리 (권장)

## 4. Repository — `repository`
- [ ] `ExperimentLogRepository extends JpaRepository<ExperimentLog, Long>`

## 5. Service — `service` (핵심)
- [ ] `webClient.post().uri("/train").bodyValue(req)...bodyToMono(TrainResponse.class)` 로 ML 호출
- [ ] `TrainResponse` → `ExperimentLog` 매핑 (ML 값 그대로, 손대지 않기)
- [ ] Spring이 채우는 건 메타데이터만: `createdAt = now`, `deleted = false`, 사용자 `memo`/`tag`
- [ ] `repository.save(entity)` 저장 후 반환

## 6. Controller — `controller`
- [ ] `@PostMapping("/experiments")` → `TrainRequest` 받아 Service 호출 → 결과 반환

## 7. 설정 — `application.properties`
- [ ] H2 datasource 설정
- [ ] `spring.jpa.hibernate.ddl-auto=update`
- [ ] `spring.h2.console.enabled=true`
- [ ] ML 서버 base-url 값

## 8. 동작 확인
- [ ] `ml-server/run.sh` 로 ML 서버 실행 (8000 포트)
- [ ] Spring 실행 후 `POST /experiments` 에 `{"epochs":50}` 전송
- [ ] `/h2-console` 에서 행 저장됐는지 확인
