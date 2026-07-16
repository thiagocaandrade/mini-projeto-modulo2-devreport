package com.devreport.domain;

public class RepositorySummary {

    private final String name;
    private final int totalIssues;
    private final int totalPRs;
    private final int totalAdditions;

    public RepositorySummary(String name, int totalIssues, int totalPRs, int totalAdditions) {
        this.name = name;
        this.totalIssues = totalIssues;
        this.totalPRs = totalPRs;
        this.totalAdditions = totalAdditions;
    }

    public String getName() { return name; }
    public int getTotalIssues() { return totalIssues; }
    public int getTotalPRs() { return totalPRs; }
    public int getTotalAdditions() { return totalAdditions; }
}
