package com.example.chart;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TickerCreateRequest {
    private String symbol;
    private String name;
    private double initialPrice;
    private double targetPrice;
    private boolean active = true; // 기본값 활성화
}