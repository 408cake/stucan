package com.example.chart;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TickerUpdateRequest {
    private String name;         // 수정할 이름
    private double targetPrice;  // 수정할 목표가격
}
