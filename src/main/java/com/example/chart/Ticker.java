package com.example.chart;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ticker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String symbol; // 예: "TICKER_1", "BTC"

    @Column(nullable = false)
    private String name;   // 과목 이름

    @Column(nullable = false)
    private double initialPrice; // 처음 가격

    @Column(nullable = false)
    private double targetPrice;  // 목표 가격

    private boolean active; // 화면 노출 여부

    public Ticker(String symbol, String name, double initialPrice, double targetPrice, boolean active) {
        this.symbol = symbol;
        this.name = name;
        this.initialPrice = initialPrice;
        this.targetPrice = targetPrice;
        this.active = active;
    }

    // 수정 로직: 과목의 이름과 목표가격만 수정 가능하도록 강제
    public void update(String name, double targetPrice) {
        this.name = name;
        this.targetPrice = targetPrice;
    }
}