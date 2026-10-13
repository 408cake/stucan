package com.example.chart;

import lombok.Getter;
import lombok.NoArgsConstructor;

public class SubjectDto {
    @Getter
    @NoArgsConstructor
    public static class CreateRequest {
        private String code;         // 과목 코드 (예: MATH, TOEIC)
        private String name;         // 과목명 (예: 미적분)
        private double initialPrice; // 초기 지식 가치
        private double targetPrice;  // 목표 지식 가치
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateRequest {
        private String name;         // 변경할 과목명
        private double targetPrice;  // 변경할 목표 지식 가치
    }
}
