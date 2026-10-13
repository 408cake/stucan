package com.example.chart;

public enum StudyStatus {
    STUDYING, // 공부 중 (우상향 Drift 적용)[cite: 1]
    RESTING   // 공부 종료/휴식 중 (우하향 감가상각 적용)[cite: 1]
}