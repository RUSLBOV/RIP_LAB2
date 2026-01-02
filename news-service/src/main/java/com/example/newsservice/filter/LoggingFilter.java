package com.example.newsservice.filter;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

@Component
public class LoggingFilter implements WebFilter {

    // ✅ Явное создание логгера — без Lombok
    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();
        Instant startTime = Instant.now();

        log.info("Incoming request: {} {}", method, path);

        return chain.filter(exchange)
            .doOnSuccess(v -> {
                Duration duration = Duration.between(startTime, Instant.now());
                log.info("Request completed: {} {} - Duration: {}ms",
                    method, path, duration.toMillis());
            })
            .doOnError(throwable -> {
                Duration duration = Duration.between(startTime, Instant.now());
                log.error("Request failed: {} {} - Error: {} - Duration: {}ms",
                    method, path, throwable.getMessage(), duration.toMillis());
            });
    }
}