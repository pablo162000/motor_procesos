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


    public void startProcess(Integer propuestaId, Integer idEstudiante1,  Integer idDireccion, Integer idSecretaria) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("propuestaId", propuestaId);
        variables.put("idEstudiante1", idEstudiante1);
        variables.put("idDireccion", idDireccion);
        variables.put("idSecretaria", idSecretaria);


        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("procesoTesis", variables);

        logger.info("✅ Proceso iniciado con ID: {}", processInstance.getId());
        logger.info("📄 Variables asociadas: propuestaId={}, idEstudiante1={}, idDireccion={}, idSecretaria={}",
                propuestaId, idEstudiante1, idDireccion, idSecretaria);
    }

    public List<Task> getTasks(String assignee) {
        return taskService.createTaskQuery().taskAssignee(assignee).list();
    }

    public Map<String, Object> getTaskVariables(String taskId) {
        return taskService.getVariables(taskId); // Obtiene todas las variables de la tarea
    }

    public void completeTask(String taskId, Map<String, Object> variables) {
        if (variables == null || variables.isEmpty()) {
            variables = new HashMap<>();
        }

        // Asegurar que siempre se pase la variable de decisión
        if (!variables.containsKey("validacionAprobada")) {
            throw new IllegalArgumentException("❌ La variable 'validacionAprobada' es requerida para continuar el proceso.");
        }

        // Completar la tarea con las variables proporcionadas
        taskService.complete(taskId, variables);

        // Agregar logging para seguimiento
        System.out.println("✅ Tarea completada: " + taskId + " | Validación: " + variables.get("validacionAprobada"));
    }





}
