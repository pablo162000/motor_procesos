package com.tesis.motor_procesos.service;
import java.util.Map;

public class ProcessMetrics {

    private int totalInstances;
    private double averageProcessDurationMs;
    private Map<String, Double> averageActivityDurationsMs;
    private Map<String, Double> averageUserDurationsMs;

    public ProcessMetrics() {}

    public ProcessMetrics(int totalInstances, double averageProcessDurationMs,
                          Map<String, Double> averageActivityDurationsMs,
                          Map<String, Double> averageUserDurationsMs) {
        this.totalInstances = totalInstances;
        this.averageProcessDurationMs = averageProcessDurationMs;
        this.averageActivityDurationsMs = averageActivityDurationsMs;
        this.averageUserDurationsMs = averageUserDurationsMs;
    }

    public int getTotalInstances() {
        return totalInstances;
    }

    public double getAverageProcessDurationMs() {
        return averageProcessDurationMs;
    }

    public Map<String, Double> getAverageActivityDurationsMs() {
        return averageActivityDurationsMs;
    }

    public Map<String, Double> getAverageUserDurationsMs() {
        return averageUserDurationsMs;
    }
}