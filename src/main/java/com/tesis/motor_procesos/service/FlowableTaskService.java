package com.tesis.motor_procesos.service;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class FlowableTaskService {

    private final TaskService taskService;

    public FlowableTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Obtiene las tareas asignadas al grupo "managers".
     */
    public List<Map<String, Object>> getManagerTasks() {
        List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup("managers").list();
        return tasks.stream().map(task -> Map.of(
                "id", (Object) task.getId(),  // Casting a Object
                "name", (Object) task.getName(),
                "processInstanceId", (Object) task.getProcessInstanceId()
        )).collect(Collectors.toList());
    }
    /**
     * Completa una tarea específica con una decisión de aprobación o rechazo.
     */
    public String completeTask(String taskId, boolean approved) {
        taskService.complete(taskId, Map.of("approved", approved));
        return "Tarea " + taskId + " completada con decisión: " + approved;
    }

}
