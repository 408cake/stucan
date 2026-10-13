package com.example.chart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandleDto {
    private long time;
    private double open;
    private double high;
    private double low;
    private double close;
}