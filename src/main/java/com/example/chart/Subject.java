package com.example.chart;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;         // 과목 코드 (예: MATH, ENG)

    @Column(nullable = false)
    private String name;         // 과목명 (예: 미적분, 토익)

    @Column(nullable = false)
    private double initialPrice; // 초기 지식 가치 (예: 1,000원)[cite: 1]

    @Column(nullable = false)
    private double currentPrice; // 현재 지식 가치[cite: 1]

    @Column(nullable = false)
    private double targetPrice;  // 목표 가치 (예: 600,000원)[cite: 1]

    @Enumerated(EnumType.STRING)
    private StudyStatus status;  // STUDYING(공부중), RESTING(휴식중)[cite: 1]

    public Subject(String code, String name, double initialPrice, double targetPrice) {
        this.code = code;
        this.name = name;
        this.initialPrice = initialPrice;
        this.currentPrice = initialPrice;
        this.targetPrice = targetPrice;
        this.status = StudyStatus.RESTING; // 기본은 휴식 상태[cite: 1]
    }

    public void update(String name, double targetPrice) {
        this.name = name;
        this.targetPrice = targetPrice;
    }

    public void startStudy() {
        this.status = StudyStatus.STUDYING;
    }

    public void stopStudy() {
        this.status = StudyStatus.RESTING;
    }

    public void updateCurrentPrice(double newPrice) {
        this.currentPrice = Math.max(10.0, newPrice); // 최소 10원 방어
    }

    // 수익률(목표 달성률) 계산: (현재가 - 초기값) / 초기값 * 100[cite: 5]
    public double getProfitRate() {
        if (this.initialPrice <= 0) return 0.0;
        return ((this.currentPrice - this.initialPrice) / this.initialPrice) * 100.0;
    }
}