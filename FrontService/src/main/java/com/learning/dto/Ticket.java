package com.learning.dto;

public class Ticket {
    private Long id;
    private String summary;
    private String description;
    private String status; // Not Started, InProgress, Done

    public Ticket() {}

    public Ticket(Long id, String summary, String description, String status) {
        this.id = id;
        this.summary = summary;
        this.description = description;
        this.status = status;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getSummary() {
        return summary;
    }
    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
