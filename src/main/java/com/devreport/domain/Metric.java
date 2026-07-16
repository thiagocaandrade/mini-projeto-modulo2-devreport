package com.devreport.domain;

public class Metric {

    private final int total;
    private final int features;
    private final int bugs;
    private final int tasks;
    private final double throughputPerWeek;
    private final double avgResolutionDays;
    private final double bugDensityPercent;

    public Metric(int total, int features, int bugs, int tasks) {
        this(total, features, bugs, tasks, 0.0, 0.0, 0.0);
    }

    public Metric(int total, int features, int bugs, int tasks,
                  double throughputPerWeek, double avgResolutionDays, double bugDensityPercent) {
        this.total = total;
        this.features = features;
        this.bugs = bugs;
        this.tasks = tasks;
        this.throughputPerWeek = throughputPerWeek;
        this.avgResolutionDays = avgResolutionDays;
        this.bugDensityPercent = bugDensityPercent;
    }

    public int getTotal() { return total; }
    public int getFeatures() { return features; }
    public int getBugs() { return bugs; }
    public int getTasks() { return tasks; }
    public double getThroughputPerWeek() { return throughputPerWeek; }
    public double getAvgResolutionDays() { return avgResolutionDays; }
    public double getBugDensityPercent() { return bugDensityPercent; }
}
