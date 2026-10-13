package com.example.chart;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class StudyEngineService {

    private final SubjectRepository subjectRepository;

    // 과목 코드별 과거 캔들(최대 100개) 보관
    private final Map<String, List<CandleDto>> candleHistories = new ConcurrentHashMap<>();
    private final Map<String, CandleDto> currentCandles = new ConcurrentHashMap<>();

    private final Random random = new Random();

    @Transactional
    public Subject createSubject(SubjectDto.CreateRequest req) {
        String code = req.getCode().toUpperCase().trim();
        if (subjectRepository.findByCode(code).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 과목 코드입니다: " + code);
        }
        Subject subject = new Subject(code, req.getName().trim(), req.getInitialPrice(), req.getTargetPrice());
        Subject saved = subjectRepository.save(subject);

        // [핵심] 등록 즉시 초기 캔들과 히스토리 바구니 생성
        long nowSec = System.currentTimeMillis() / 1000;
        long candleTime = (nowSec / 5) * 5;

        CandleDto initCandle = CandleDto.builder()
                .time(candleTime)
                .open(saved.getInitialPrice())
                .high(saved.getInitialPrice())
                .low(saved.getInitialPrice())
                .close(saved.getInitialPrice())
                .build();

        currentCandles.put(saved.getCode(), initCandle);
        candleHistories.computeIfAbsent(saved.getCode(), k -> new ArrayList<>()).add(initCandle);

        return saved;
    }

    // 2. 과목 수정 (이름, 목표가)[cite: 1]
    @Transactional
    public void updateSubject(String code, SubjectDto.UpdateRequest req) {
        Subject subject = subjectRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 과목입니다: " + code));
        subject.update(req.getName().trim(), req.getTargetPrice());
    }

    // 3. 과목 삭제 (DB 및 실시간 캔들 메모리 정리)[cite: 2]
    @Transactional
    public void deleteSubject(String code) {
        Subject subject = subjectRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 과목입니다: " + code));
        subjectRepository.delete(subject);
        currentCandles.remove(code);
        candleHistories.remove(code);
    }

    @PostConstruct
    public void initDefaultData() {
        if (subjectRepository.count() == 0) {
            subjectRepository.save(new Subject("CAL", "미적분", 1000.0, 600000.0));
        }
    }

    // 1초마다 학습 상태에 따른 Drift + Noise 합성 연산 및 5초봉 생성[cite: 1, 2]
    @Scheduled(fixedRate = 1000)
    @Transactional
    public void tickEngine() {
        List<Subject> subjects = subjectRepository.findAll();
        long nowSec = System.currentTimeMillis() / 1000;
        long candleTime = (nowSec / 5) * 5; // 5초봉[cite: 2]

        for (Subject sub : subjects) {
            double current = sub.getCurrentPrice();
            double noise = (random.nextDouble() * 20.0) - 10.0; // -10 ~ +10 랜덤 노이즈[cite: 1]
            double drift = 0.0;

            if (sub.getStatus() == StudyStatus.STUDYING) {
                // 공부 중: 강력한 우상향 힘 (+15 ~ +25)[cite: 1]
                drift = 15.0 + (random.nextDouble() * 10.0);
            } else {
                // 휴식 중: 완만한 망각 곡선 감가상각 (-2 ~ -5)[cite: 1]
                drift = -3.0 - (random.nextDouble() * 2.0);
            }

            double newPrice = Math.max(10.0, current + drift + noise);
            sub.updateCurrentPrice(newPrice);

            // 5초 캔들 집계[cite: 2]
            String code = sub.getCode();
            CandleDto curCandle = currentCandles.get(code);

            if (curCandle == null || curCandle.getTime() != candleTime) {
                if (curCandle != null) {
                    List<CandleDto> history = candleHistories.computeIfAbsent(code, k -> new ArrayList<>());
                    history.add(curCandle);
                    if (history.size() > 100) history.remove(0);
                }
                curCandle = CandleDto.builder()
                        .time(candleTime)
                        .open(newPrice)
                        .high(newPrice)
                        .low(newPrice)
                        .close(newPrice)
                        .build();
            } else {
                curCandle.setHigh(Math.max(curCandle.getHigh(), newPrice));
                curCandle.setLow(Math.min(curCandle.getLow(), newPrice));
                curCandle.setClose(newPrice);
            }
            currentCandles.put(code, curCandle);
        }
    }

    // 공부 시작 처리[cite: 1]
    @Transactional
    public void startStudy(String code) {
        Subject subject = subjectRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 과목입니다: " + code));
        subject.startStudy();
    }

    // 공부 종료(휴식) 처리[cite: 1]
    @Transactional
    public void stopStudy(String code) {
        Subject subject = subjectRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 과목입니다: " + code));
        subject.stopStudy();
    }

    public Map<String, List<CandleDto>> getInitHistories() {
        return candleHistories;
    }

    public Map<String, Object> getCurrentSnapshot() {
        Map<String, Object> result = new HashMap<>();
        List<Subject> subjects = subjectRepository.findAll();
        for (Subject sub : subjects) {
            CandleDto candle = currentCandles.get(sub.getCode());
            if (candle != null) {
                result.put(sub.getCode(), Map.of(
                        "candle", candle,
                        "price", sub.getCurrentPrice(),
                        "profitRate", sub.getProfitRate(),
                        "status", sub.getStatus().name()
                ));
            }
        }
        return result;
    }
}