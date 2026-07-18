package com.devreport.domain;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class Issue {

    private final String id;
    private final String title;
    private final String description;
    private final List<String> labels;
    private final LocalDateTime createdAt;
    private final LocalDateTime closedAt;
    private final String author;
    private final String project;
    private final String repository;

    public Issue(String id, String title, String description, List<String> labels,
                 LocalDateTime closedAt, String author, String project) {
        this(id, title, description, labels, null, closedAt, author, project, null);
    }

    public Issue(String id, String title, String description, List<String> labels,
                 LocalDateTime closedAt, String author, String project, String repository) {
        this(id, title, description, labels, null, closedAt, author, project, repository);
    }

    public Issue(String id, String title, String description, List<String> labels,
                 LocalDateTime createdAt, LocalDateTime closedAt, String author, String project, String repository) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.labels = labels != null ? Collections.unmodifiableList(labels) : Collections.emptyList();
        this.createdAt = createdAt;
        this.closedAt = closedAt;
        this.author = author;
        this.project = project;
        this.repository = repository;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getLabels() {
        return labels;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public String getAuthor() {
        return author;
    }

    public String getProject() {
        return project;
    }

    public String getRepository() {
        return repository;
    }
}
