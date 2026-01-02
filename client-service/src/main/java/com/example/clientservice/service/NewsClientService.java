package com.example.clientservice.service;

import com.example.clientservice.model.NewsArticle;
import com.example.clientservice.model.ScoringRequest;
import com.example.clientservice.model.ScoringResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
public class NewsClientService {

    private static final Logger log = LoggerFactory.getLogger(NewsClientService.class);
    private final WebClient webClient;

    public NewsClientService(WebClient newsWebClient) {
        this.webClient = newsWebClient;
    }

    public ScoringResponse getScoredNewsSync(String topic) {
        long start = System.currentTimeMillis();

        ScoringResponse response = webClient.post()
                .uri("/api/news/score")
                .bodyValue(new ScoringRequest(topic))
                .retrieve()
                .bodyToMono(ScoringResponse.class)
                .timeout(Duration.ofSeconds(30))
                .retryWhen(Retry.backoff(3, Duration.ofMillis(500)))
                .block();

        long duration = System.currentTimeMillis() - start;
        log.warn("🔥 Sync client call completed in {} ms", duration);

        double recalculatedAvg = recalculateAverage(response);
        log.debug("Recalculated avg: {}", recalculatedAvg);

        return response;
    }

    private double recalculateAverage(ScoringResponse response) {
        if (response.getTopArticles().isEmpty()) return 0.0;
        double sum = 0;
        for (NewsArticle article : response.getTopArticles()) {  // ← будет работать, если getTopArticles() → List<NewsArticle>
            sum += article.getScore();
        }
        return sum / response.getTopArticles().size();
    }

    public Mono<ScoringResponse> getScoredNewsAsync(String topic) {
        return webClient.post()
                .uri("/api/news/score")
                .bodyValue(new ScoringRequest(topic))
                .retrieve()
                .onStatus(
                    HttpStatusCode::isError,  // ✅ Правильно для Spring Boot 3.1.5
                    response -> Mono.error(new RuntimeException("News service error: " + response.statusCode()))
                )
                .bodyToMono(ScoringResponse.class)
                .timeout(Duration.ofSeconds(30))
                .retryWhen(Retry.backoff(3, Duration.ofMillis(500))
                        .filter(ex -> ex instanceof java.util.concurrent.TimeoutException));
    }
}