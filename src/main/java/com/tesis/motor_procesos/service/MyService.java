package com.tesis.motor_procesos.service;

import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.flowable.engine.IdentityService;

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
    private HistoryService historyService;
    @Autowired
    private TaskService taskService;

    @Autowired
    private IdentityService identityService;

    public void startProcess(Map<String, Object> variables) {
        try {

            identityService.setAuthenticatedUserId(String.valueOf(variables.get("idEstudiante1")));
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("procesoTesis", variables);
            logger.info("✅ Proceso iniciado con ID: {}", processInstance.getId());
            logger.info("📄 Variables asociadas: propuestaId={}, idEstudiante1={}, idDireccion={}",
                    variables.get("propuestaId"), variables.get("idEstudiante1"), variables.get("idDireccion"));
        } finally {
            // Limpiar el usuario autenticado después del inicio del proceso
            identityService.setAuthenticatedUserId(null);
        }
    }

    public List<Task> getTasks(String assignee) {
        return taskService.createTaskQuery().taskAssignee(assignee).list();
    }

    public List<Task> getTasksByAssigneeAndTaskKey(String assignee, String taskDefinitionKey) {
        return taskService.createTaskQuery()
                .taskAssignee(assignee)
                .taskDefinitionKey(taskDefinitionKey)
                .list();
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


    public String obtenerIdInstanciaActivaPorPropuestaId(String propuestaId) {
/*
        Integer propuestaIdNum = Integer.parseInt(propuestaId);

        ProcessInstance instancia = runtimeService.createProcessInstanceQuery()
                .variableValueEquals("propuestaId", propuestaIdNum)
                .singleResult();

        return instancia != null ? instancia.getId() : null;
*/
        Integer propuestaIdNum = Integer.parseInt(propuestaId);

        HistoricProcessInstance instancia = historyService.createHistoricProcessInstanceQuery()
                .variableValueEquals("propuestaId", propuestaIdNum)
                .orderByProcessInstanceStartTime().desc() // opcional: para obtener la más reciente
                .singleResult();

        return instancia != null ? instancia.getId() : null;
    }



}
