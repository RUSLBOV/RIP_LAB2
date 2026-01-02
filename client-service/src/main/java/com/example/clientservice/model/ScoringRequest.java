package com.example.clientservice.model;

public class ScoringRequest {
    private String topic;
    
    public ScoringRequest() {}
    
    public ScoringRequest(String topic) {
        this.topic = topic;
    }
    
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
}