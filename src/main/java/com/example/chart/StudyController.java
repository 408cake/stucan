package com.example.chart;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequiredArgsConstructor
public class StudyController {

    private final SubjectRepository subjectRepository;
    private final StudyEngineService studyEngineService;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    // 1. 전체 과목 조회
    @GetMapping("/api/subjects")
    public ResponseEntity<List<Subject>> getSubjects() {
        return ResponseEntity.ok(subjectRepository.findAll());
    }

    // 2. 공부 시작[cite: 1]
    @PostMapping("/api/subjects/{code}/start")
    public ResponseEntity<String> startStudy(@PathVariable String code) {
        studyEngineService.startStudy(code);
        return ResponseEntity.ok("공부 시작: " + code);
    }

    // 3. 공부 종료[cite: 1]
    @PostMapping("/api/subjects/{code}/stop")
    public ResponseEntity<String> stopStudy(@PathVariable String code) {
        studyEngineService.stopStudy(code);
        return ResponseEntity.ok("공부 종료: " + code);
    }

    // 4. 실시간 SSE 연결 (/events)[cite: 2]
    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connectEvents() {
        SseEmitter emitter = new SseEmitter(1800000L); // 30분
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));

        try {
            emitter.send(SseEmitter.event()
                    .data(Map.of("type", "init", "tickers", studyEngineService.getInitHistories())));
        } catch (IOException e) {
            emitter.complete();
            emitters.remove(emitter);
        }

        return emitter;
    }
    // 과목 등록[cite: 1]
    @PostMapping("/api/subjects")
    public ResponseEntity<?> createSubject(@RequestBody SubjectDto.CreateRequest request) {
        try {
            return ResponseEntity.ok(studyEngineService.createSubject(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 과목 수정[cite: 1]
    @PutMapping("/api/subjects/{code}")
    public ResponseEntity<String> updateSubject(
            @PathVariable String code,
            @RequestBody SubjectDto.UpdateRequest request) {
        studyEngineService.updateSubject(code, request);
        return ResponseEntity.ok("과목 수정 완료");
    }

    // 과목 삭제[cite: 2]
    @DeleteMapping("/api/subjects/{code}")
    public ResponseEntity<String> deleteSubject(@PathVariable String code) {
        studyEngineService.deleteSubject(code);
        return ResponseEntity.ok("과목 삭제 완료");
    }
    // 5. 1초마다 모든 브라우저에 최신 캔들과 수익률 푸시[cite: 2, 5]
    @Scheduled(fixedRate = 1000)
    public void broadcastUpdates() {
        if (emitters.isEmpty()) return;

        Map<String, Object> payload = Map.of(
                "type", "update",
                "tickers", studyEngineService.getCurrentSnapshot()
        );

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().data(payload));
            } catch (Exception e) {
                emitter.complete();
                emitters.remove(emitter);
            }
        }
    }
}