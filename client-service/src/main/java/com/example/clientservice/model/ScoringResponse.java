package com.example.clientservice.model;

import java.util.List;


public class ScoringResponse {
    private String topic;
    private int totalArticles;
    private List<NewsArticle> topArticles;  
    private double averageScore;
    private double topScore;
    private long processingTimeMs;



    public List<NewsArticle> getTopArticles() {  
        return topArticles;
    }

    public void setTopArticles(List<NewsArticle> topArticles) {
        this.topArticles = topArticles;
    }


    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public int getTotalArticles() { return totalArticles; }
    public void setTotalArticles(int totalArticles) { this.totalArticles = totalArticles; }

    public double getAverageScore() { return averageScore; }
    public void setAverageScore(double averageScore) { this.averageScore = averageScore; }

    public double getTopScore() { return topScore; }
    public void setTopScore(double topScore) { this.topScore = topScore; }

    public long getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(long processingTimeMs) { this.processingTimeMs = processingTimeMs; }
}