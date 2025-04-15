package com.tesis.motor_procesos.service;

import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional

public class MyService {
    private static final Logger logger = LogManager.getLogger(MyService.class);


    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;


    public void startProcess(Integer propuestaId, Integer idEstudiante1,  Integer idDireccion) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("propuestaId", propuestaId);
        variables.put("idEstudiante1", idEstudiante1);
        variables.put("idDireccion", idDireccion);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("procesoTesis", variables);
        logger.info("✅ Proceso iniciado con ID: {}", processInstance.getId());
        logger.info("📄 Variables asociadas: propuestaId={}, idEstudiante1={}, idDireccion={}",
                propuestaId, idEstudiante1, idDireccion);
    }

    public List<Task> getTasks(String assignee) {
        return taskService.createTaskQuery().taskAssignee(assignee).list();
    }

    public Map<String, Object> getTaskVariables(String taskId) {
        return taskService.getVariables(taskId); // Obtiene todas las variables de la tarea
    }

    public void completeTask(String taskId, Map<String, Object> variables) {
        if (taskId == null || taskId.isBlank()) {
            throw new IllegalArgumentException("El ID de la tarea no puede ser nulo o vacío.");
        }
        // Verifica si la tarea existe antes de intentar completarla
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new IllegalArgumentException("❌ No se encontró una tarea con ID: " + taskId);
        }
        // Log de seguimiento (puedes usar Logger si ya lo tienes configurado)
        System.out.println("🔄 Completando tarea: " + taskId);
        System.out.println("📦 Variables recibidas: " + variables);

        // Completa la tarea con o sin variables
        if (variables == null || variables.isEmpty()) {
            taskService.complete(taskId);
        } else {
            taskService.complete(taskId, variables);
        }
        System.out.println("✅ Tarea completada con éxito.");
    }

}
