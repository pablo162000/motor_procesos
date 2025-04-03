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


    public void startProcess(Integer propuestaId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("propuestaId", propuestaId);  // Guardamos el ID como variable del proceso

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("procesoTesis", variables);

        logger.info("✅ Proceso iniciado con ID: {}", processInstance.getId());
        logger.info("📄 Propuesta asociada: {}", propuestaId);
    }

    public List<Task> getTasks(String assignee) {
        return taskService.createTaskQuery().taskAssignee(assignee).list();
    }

}
