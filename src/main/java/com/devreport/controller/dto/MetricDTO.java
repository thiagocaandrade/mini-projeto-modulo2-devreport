package com.devreport.controller.dto;

public class MetricDTO {

    private int total;
    private int features;
    private int bugs;
    private int tasks;
    private double throughputPerWeek;
    private double avgResolutionDays;
    private double bugDensityPercent;

    public MetricDTO() {
    }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public int getFeatures() { return features; }
    public void setFeatures(int features) { this.features = features; }
    public int getBugs() { return bugs; }
    public void setBugs(int bugs) { this.bugs = bugs; }
    public int getTasks() { return tasks; }
    public void setTasks(int tasks) { this.tasks = tasks; }
    public double getThroughputPerWeek() { return throughputPerWeek; }
    public void setThroughputPerWeek(double throughputPerWeek) { this.throughputPerWeek = throughputPerWeek; }
    public double getAvgResolutionDays() { return avgResolutionDays; }
    public void setAvgResolutionDays(double avgResolutionDays) { this.avgResolutionDays = avgResolutionDays; }
    public double getBugDensityPercent() { return bugDensityPercent; }
    public void setBugDensityPercent(double bugDensityPercent) { this.bugDensityPercent = bugDensityPercent; }
}
