package com.devreport.domain;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class PullRequest {

    private final Long id;
    private final int number;
    private final String title;
    private final LocalDateTime createdAt;
    private final LocalDateTime mergedAt;
    private final int additions;
    private final int deletions;
    private final int changedFiles;
    private final List<String> reviewers;
    private final int reviewComments;
    private final List<String> labels;
    private final String repository;

    public PullRequest(Long id, int number, String title, LocalDateTime createdAt,
                       LocalDateTime mergedAt, int additions, int deletions,
                       int changedFiles, List<String> reviewers, int reviewComments,
                       List<String> labels, String repository) {
        this.id = id;
        this.number = number;
        this.title = title;
        this.createdAt = createdAt;
        this.mergedAt = mergedAt;
        this.additions = additions;
        this.deletions = deletions;
        this.changedFiles = changedFiles;
        this.reviewers = reviewers != null ? Collections.unmodifiableList(reviewers) : Collections.emptyList();
        this.reviewComments = reviewComments;
        this.labels = labels != null ? Collections.unmodifiableList(labels) : Collections.emptyList();
        this.repository = repository;
    }

    public Long getId() { return id; }
    public int getNumber() { return number; }
    public String getTitle() { return title; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getMergedAt() { return mergedAt; }
    public int getAdditions() { return additions; }
    public int getDeletions() { return deletions; }
    public int getChangedFiles() { return changedFiles; }
    public List<String> getReviewers() { return reviewers; }
    public int getReviewComments() { return reviewComments; }
    public List<String> getLabels() { return labels; }
    public String getRepository() { return repository; }
}
