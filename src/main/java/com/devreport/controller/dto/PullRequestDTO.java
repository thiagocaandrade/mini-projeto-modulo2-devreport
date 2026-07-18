package com.devreport.controller.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PullRequestDTO {

    private Long id;
    private int number;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime mergedAt;
    private int additions;
    private int deletions;
    private int changedFiles;
    private List<String> reviewers;
    private int reviewComments;
    private List<String> labels;
    private String repository;

    public PullRequestDTO() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getMergedAt() { return mergedAt; }
    public void setMergedAt(LocalDateTime mergedAt) { this.mergedAt = mergedAt; }

    public int getAdditions() { return additions; }
    public void setAdditions(int additions) { this.additions = additions; }

    public int getDeletions() { return deletions; }
    public void setDeletions(int deletions) { this.deletions = deletions; }

    public int getChangedFiles() { return changedFiles; }
    public void setChangedFiles(int changedFiles) { this.changedFiles = changedFiles; }

    public List<String> getReviewers() { return reviewers; }
    public void setReviewers(List<String> reviewers) { this.reviewers = reviewers; }

    public int getReviewComments() { return reviewComments; }
    public void setReviewComments(int reviewComments) { this.reviewComments = reviewComments; }

    public List<String> getLabels() { return labels; }
    public void setLabels(List<String> labels) { this.labels = labels; }

    public String getRepository() { return repository; }
    public void setRepository(String repository) { this.repository = repository; }
}
