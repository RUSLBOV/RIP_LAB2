package com.example.newsservice.controller;

import com.example.newsservice.model.ScoringRequest;
import com.example.newsservice.model.ScoringResponse;
import com.example.newsservice.service.NewsScoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/news")
public class NewsController {
    
    private static final Logger log = LoggerFactory.getLogger(NewsController.class);
    
    @Autowired
    private NewsScoringService scoringService;
    
    @PostMapping("/score")
    public Mono<ScoringResponse> scoreNews(@RequestBody ScoringRequest request) {
        log.info("Received scoring request for topic: {}", request.getTopic());
        return scoringService.scoreArticles(request.getTopic());
    }
    
    @GetMapping("/test")
    public Mono<String> test() {
        return Mono.just("News Service is running!");
    }
}