package com.devreport.controller.dto;

public class RepositorySummaryDTO {

    private String name;
    private int totalIssues;
    private int totalPRs;
    private int totalAdditions;

    public RepositorySummaryDTO() {
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getTotalIssues() { return totalIssues; }
    public void setTotalIssues(int totalIssues) { this.totalIssues = totalIssues; }

    public int getTotalPRs() { return totalPRs; }
    public void setTotalPRs(int totalPRs) { this.totalPRs = totalPRs; }

    public int getTotalAdditions() { return totalAdditions; }
    public void setTotalAdditions(int totalAdditions) { this.totalAdditions = totalAdditions; }
}
