package com.example.newsservice.model;

import java.time.LocalDateTime;

public class NewsArticle {
    private String id;
    private String title;
    private String content;
    private String category;
    private LocalDateTime publishedAt;
    private String author;
    private double score;
    private LocalDateTime scoredAt;
    
    // Пустой конструктор
    public NewsArticle() {
    }
    
    // Конструктор со всеми параметрами
    public NewsArticle(String id, String title, String content, String category, 
                      LocalDateTime publishedAt, String author, double score, 
                      LocalDateTime scoredAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.category = category;
        this.publishedAt = publishedAt;
        this.author = author;
        this.score = score;
        this.scoredAt = scoredAt;
    }
    
    // Геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
    
    public LocalDateTime getScoredAt() { return scoredAt; }
    public void setScoredAt(LocalDateTime scoredAt) { this.scoredAt = scoredAt; }
}