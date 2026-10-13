package com.example.chart;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TickerRepository extends JpaRepository<Ticker, Long> {
    Optional<Ticker> findBySymbol(String symbol);

    // 화면에 보여줄 활성화된 과목들만 불러오기 위한 용도
    List<Ticker> findByActiveTrue();
}