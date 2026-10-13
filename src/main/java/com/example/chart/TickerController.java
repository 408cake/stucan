package com.example.chart;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tickers")
@RequiredArgsConstructor
public class TickerController {

    private final TickerService tickerService;

    // POST: /api/tickers (새 과목 등록)
    @PostMapping
    public ResponseEntity<Ticker> createTicker(@RequestBody TickerCreateRequest request) {
        Ticker created = tickerService.createTicker(request);
        return ResponseEntity.ok(created);
    }

    // PUT: /api/tickers/{id} (기존 과목 수정)
    @PutMapping("/{id}")
    public ResponseEntity<String> updateTicker(
            @PathVariable Long id,
            @RequestBody TickerUpdateRequest request) {

        tickerService.updateTicker(id, request);
        return ResponseEntity.ok("과목 수정이 완료되었습니다.");
    }

    // GET: /api/tickers (전체 과목 불러오기)
    @GetMapping
    public ResponseEntity<List<Ticker>> getAllTickers() {
         return ResponseEntity.ok(tickerService.getAllTickers());
    }
}