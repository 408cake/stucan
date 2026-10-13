package com.example.chart;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TickerService {

    private final TickerRepository tickerRepository;

    // 1. 과목 동적 등록
    @Transactional
    public Ticker createTicker(TickerCreateRequest request) {
        Ticker ticker = new Ticker(
                request.getSymbol(),
                request.getName(),
                request.getInitialPrice(),
                request.getTargetPrice(),
                request.isActive()
        );
        return tickerRepository.save(ticker); // DB에 Insert
    }

    // 2. 과목 수정 (이름, 목표가격만)
    @Transactional
    public void updateTicker(Long id, TickerUpdateRequest request) {
        // 기존 데이터를 DB에서 찾아옴
        Ticker ticker = tickerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 과목을 찾을 수 없습니다. id=" + id));

        // 엔티티 수정 (트랜잭션 종료 시 자동 Update 쿼리 발생)
        ticker.update(request.getName(), request.getTargetPrice());
    }

    // 3. (참고용) 전체 과목 조회
    @Transactional(readOnly = true)
    public List<Ticker> getAllTickers() {
        return tickerRepository.findAll();
    }
}