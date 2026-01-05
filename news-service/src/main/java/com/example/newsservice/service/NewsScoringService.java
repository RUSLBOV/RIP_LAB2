package com.example.newsservice.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.newsservice.model.NewsArticle;
import com.example.newsservice.model.ScoringResponse;

import reactor.core.publisher.Mono;

@Service
public class NewsScoringService {

    private static final Logger log = LoggerFactory.getLogger(NewsScoringService.class);
//fwfws Fs
    // Больше шаблонов для разнообразия (20 шт)
    private static final List<String> TEMPLATES = Arrays.asList(
        "Breaking: %s festival announced in Berlin with over 50 performers",
        "How %s changed my life — a personal story of transformation",
        "Top 10 %s albums of 2026 according to music critics worldwide",
        "Why %s is overrated — controversial opinion sparks online debate",
        "New startup uses AI to generate %s in real-time with stunning results",
        "Scientists discover link between %s and improved cognitive function",
        "Global %s market expected to exceed $10 billion by 2030",
        "Underground %s scene thrives in post-pandemic urban centers",
        "%s legend returns after 15-year hiatus with new tour",
        "Controversial %s documentary banned in three countries",
        "Streaming killed the %s star — industry insiders speak out",
        "The rise and fall of %s in Eastern Europe during the 90s",
        "Military uses %s for psychological operations — leaked documents confirm",
        "Children exposed to %s show significant improvement in memory tests",
        "Illegal %s trade busted in international Interpol operation",
        "Religious group claims %s is divine communication from higher beings",
        "NASA releases first-ever %s recorded by Mars rover microphone",
        "TikTok trends resurrect forgotten %s genre among Gen Z users",
        "Ethical concerns grow around AI-generated %s content",
        "Olympic committee debates inclusion of %s as demonstration sport"
    );

    private static final List<String> CONTENT_FRAGMENTS = Arrays.asList(
        "The event will take place this summer. Tickets go on sale next week.",
        "Organizers promise full sustainability and zero single-use plastic.",
        "Over 50 artists from 15 countries are expected to perform.",
        "Experts say this could revolutionize the entire industry.",
        "Many fans disagree, but streaming data shows clear decline since 2023.",
        "The model was trained on 10 terabytes of raw unlabeled data.",
        "Participants showed increased activity in prefrontal cortex regions.",
        "Investment surged after regulatory approval in EU and APAC regions.",
        "Underground venues report record attendance despite economic downturn.",
        "Critics praise the emotional depth and technical innovation.",
        "Some listeners couldn't distinguish AI output from human in blind tests.",
        "Researchers used fMRI scans to monitor real-time brain responses.",
        "Venture capital funding reached $2.3B in Q1 alone.",
        "The movement started in small clubs and spread via social media.",
        "Ethical review boards raised concerns about consent and bias.",
        "Documentary features interviews with whistleblowers and insiders.",
        "Festival attendance has halved in major cities over 3 years.",
        "The sound design pushes boundaries of traditional expectations."
    );

    public Mono<ScoringResponse> scoreArticles(String topic) {
        List<NewsArticle> articles = generateFakeArticles(50_000);

        long startTime = System.currentTimeMillis();

        List<NewsArticle> scored = new ArrayList<>();
        for (NewsArticle article : articles) {
            double score = computeNaiveScore(topic, article); // <-- ОСНОВНАЯ НАГРУЗКА
            if (score > 0.001) { // отсекаем совсем нерелевантные
                NewsArticle copy = new NewsArticle();
                copy.setId(article.getId());
                copy.setTitle(article.getTitle());
                copy.setContent(article.getContent());
                copy.setCategory(article.getCategory());
                copy.setPublishedAt(article.getPublishedAt());
                copy.setAuthor(article.getAuthor());
                copy.setScore(score);
                copy.setScoredAt(LocalDateTime.now());
                scored.add(copy);
            }
        }

        //  Сортировка — ещё O(n log n)
        scored.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        // Агрегация метрик
        double topScore = scored.isEmpty() ? 0.0 : scored.get(0).getScore();
        double avgScore = scored.stream().mapToDouble(NewsArticle::getScore).average().orElse(0.0);
        List<NewsArticle> top10 = scored.stream().limit(10).collect(Collectors.toList());

        long duration = System.currentTimeMillis() - startTime;

        log.warn("⚠️ Naive scoring completed: {} articles → {} relevant in {} ms", 
                articles.size(), scored.size(), duration);

        return Mono.just(new ScoringResponse(
                topic,
                scored.size(),
                top10,
                avgScore,
                topScore,
                duration
        ));
    }

    //НЕОПТИМАЛЬНЫЙ TOKENIZER: каждый вызов — новый regex + split
    private List<String> tokenizeNaive(String text) {
        if (text == null || text.trim().isEmpty()) return Collections.emptyList();

        //Каждый раз создаётся Pattern+Matcher! Нет кэширования.
        String cleaned = text.toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\s]", " ") // квадратные скобки — каждый раз!
                .replaceAll("\\s+", " ")           // ещё один regex!
                .trim();

        // split() без проверки на пустоту
        return Arrays.stream(cleaned.split(" "))
                .filter(word -> word.length() >= 2) // нет stop-words!
                .collect(Collectors.toList());
    }

    //  вложенные циклы!
    private double computeNaiveScore(String topic, NewsArticle article) {
        List<String> topicWords = tokenizeNaive(topic);
        List<String> titleWords = tokenizeNaive(article.getTitle());
        List<String> contentWords = tokenizeNaive(article.getContent());

        int titleMatches = 0;
        int contentMatches = 0;

        //  ВЛОЖЕННЫЕ ЦИКЛЫ — главный источник неоптимальности
        for (String tWord : topicWords) {
            for (String tw : titleWords) {
                if (tWord.equals(tw)) {
                    titleMatches++;
                }
            }
            for (String cw : contentWords) {
                if (tWord.equals(cw)) {
                    contentMatches++;
                }
            }
        }

        //  "Сложная" формула с дублирующими вычислениями
        double rawScore = titleMatches * 3.0 + contentMatches * 1.0;

        // Повторная токенизация — для penalty! (намеренно)
        int totalTokens = tokenizeNaive(article.getTitle() + " " + article.getContent()).size();
        double lengthPenalty = Math.log1p(totalTokens); // нелинейный штраф

        // Bonus за уникальность — но вычисляем каждый раз!
        long uniqueTokens = tokenizeNaive(article.getContent()).stream().distinct().count();
        double diversityBonus = totalTokens > 0 ? (double) uniqueTokens / totalTokens : 1.0;

        return rawScore * diversityBonus / (1.0 + 0.05 * lengthPenalty);
    }

    // Генерация 50k фейковых статей — без кэширования
    private List<NewsArticle> generateFakeArticles(int count) {
        Random rnd = new Random();
        String[] categories = {"music", "technology", "sports", "science", "business", "politics", "arts"};
        List<NewsArticle> list = new ArrayList<>(count);
        
        for (int i = 0; i < count; i++) {
            // Выбираем случайную тему-заполнитель
            String placeholder = "topic" + (i % 20);
            String titleTemplate = TEMPLATES.get(i % TEMPLATES.size());
            String title = String.format(titleTemplate, placeholder);

            // Собираем контент из 3–5 случайных фрагментов
            StringBuilder content = new StringBuilder();
            int fragCount = 3 + rnd.nextInt(3); // 3–5
            for (int j = 0; j < fragCount; j++) {
                content.append(CONTENT_FRAGMENTS.get(rnd.nextInt(CONTENT_FRAGMENTS.size())));
                content.append(" ");
            }

            NewsArticle article = new NewsArticle();
            article.setId("news-" + UUID.randomUUID());
            article.setTitle(title);
            article.setContent(content.toString().trim());
            article.setCategory(categories[i % categories.length]);
            article.setPublishedAt(LocalDateTime.now().minusDays(rnd.nextInt(90)));
            article.setAuthor("Reporter " + (i % 25 + 1));
            article.setScore(0.0);
            article.setScoredAt(null);

            list.add(article);
        }
        return list;
    }
}