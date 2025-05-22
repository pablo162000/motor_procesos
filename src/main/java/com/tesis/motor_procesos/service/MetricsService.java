package com.tesis.motor_procesos.service;


import org.flowable.engine.HistoryService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MetricsService {

    private final HistoryService historyService;

    public MetricsService(HistoryService historyService) {
        this.historyService = historyService;
    }

    public ProcessMetrics getProcessMetrics(String processDefinitionKey) {
        List<HistoricProcessInstance> instances = historyService.createHistoricProcessInstanceQuery()
                .processDefinitionKey(processDefinitionKey)
                .finished()
                .list();

        if (instances.isEmpty()) {
            return new ProcessMetrics(); // Puedes definir qué hacer en caso de vacío
        }

        long totalDuration = 0;
        Map<String, List<Long>> activityDurations = new HashMap<>();
        Map<String, Integer> activityCounts = new HashMap<>();
        Map<String, Long> userDurations = new HashMap<>();
        Map<String, Integer> userCounts = new HashMap<>();

        for (HistoricProcessInstance instance : instances) {
            totalDuration += instance.getDurationInMillis();

            List<HistoricActivityInstance> activities = historyService
                    .createHistoricActivityInstanceQuery()
                    .processInstanceId(instance.getId())
                    .finished()
                    .list();

            for (HistoricActivityInstance activity : activities) {
                if (activity.getDurationInMillis() != null) {
                    activityDurations
                            .computeIfAbsent(activity.getActivityId(), k -> new ArrayList<>())
                            .add(activity.getDurationInMillis());

                    activityCounts.put(activity.getActivityId(),
                            activityCounts.getOrDefault(activity.getActivityId(), 0) + 1);

                    if (activity.getAssignee() != null) {
                        userDurations.put(activity.getAssignee(),
                                userDurations.getOrDefault(activity.getAssignee(), 0L) + activity.getDurationInMillis());

                        userCounts.put(activity.getAssignee(),
                                userCounts.getOrDefault(activity.getAssignee(), 0) + 1);
                    }
                }
            }
        }

        double averageProcessDuration = totalDuration / (double) instances.size();

        Map<String, Double> averageActivityDurations = new HashMap<>();
        for (String actId : activityDurations.keySet()) {
            List<Long> durations = activityDurations.get(actId);
            double avg = durations.stream().mapToLong(Long::longValue).average().orElse(0);
            averageActivityDurations.put(actId, avg);
        }

        Map<String, Double> averageUserDurations = new HashMap<>();
        for (String user : userDurations.keySet()) {
            double avg = userDurations.get(user) / (double) userCounts.get(user);
            averageUserDurations.put(user, avg);
        }

        return new ProcessMetrics(
                instances.size(),
                averageProcessDuration,
                averageActivityDurations,
                averageUserDurations
        );
    }
}