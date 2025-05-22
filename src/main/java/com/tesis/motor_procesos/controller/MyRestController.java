package com.tesis.motor_procesos.controller;

import com.tesis.motor_procesos.service.MyService;
import jakarta.servlet.http.HttpServletResponse;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.Gateway;
import org.flowable.bpmn.model.GraphicInfo;
import org.flowable.bpmn.model.TextAnnotation;
import org.flowable.engine.HistoryService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.image.impl.DefaultProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/process")

public class MyRestController {


    @Autowired
    private HistoryService historyService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private MyService myService;

    @PostMapping("/iniciar")
    public ResponseEntity<String> iniciarProceso(
            @RequestParam Integer propuestaId,
            @RequestParam(required = false) Integer idEstudiante1,
            @RequestParam Integer idDireccion )
    {
        myService.startProcess(propuestaId, idEstudiante1, idDireccion);
        return ResponseEntity.ok("Proceso iniciado con éxito.");
    }


    @GetMapping("/tasks")
    public ResponseEntity<List<TaskRepresentation>> obtenerTareas(@RequestParam String assignee) {
        List<Task> tasks = myService.getTasks(assignee);

        if (tasks.isEmpty()) {
            return ResponseEntity.noContent().build(); // Devuelve 204 si no hay tareas
        }

        List<TaskRepresentation> dtos = tasks.stream()
                .map(task -> {
                    // Obtener variables del proceso para la tarea actual
                    Map<String, Object> variables = myService.getTaskVariables(task.getId());
                    return new TaskRepresentation(task.getId(), task.getName(), variables);
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }


    @GetMapping("/tasks/{assignee}")
    public ResponseEntity<Integer> tareasPendientes(@PathVariable String assignee) {
        List<Task> tasks = myService.getTasks(assignee);

        return ResponseEntity.ok(tasks.size());
    }


    @GetMapping("/tasks/by-assignee-and-key")
    public ResponseEntity<List<TaskRepresentation>> obtenerTareasPorAssigneeYKey(@RequestParam String assignee, @RequestParam String taskKey) {
        List<Task> tasks = myService.getTasksByAssigneeAndTaskKey(assignee, taskKey);

        if (tasks.isEmpty()) {
            return ResponseEntity.noContent().build(); // Devuelve 204 si no hay tareas
        }

        List<TaskRepresentation> dtos = tasks.stream()
                .map(task -> {
                    // Obtener variables del proceso para la tarea actual
                    Map<String, Object> variables = myService.getTaskVariables(task.getId());
                    return new TaskRepresentation(task.getId(), task.getName(), variables);
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }



    @PostMapping("/tasks/complete")
    public ResponseEntity<String> completarTareaConVariables(
            @RequestParam String taskId,
            @RequestBody(required = false) Map<String, Object> variables) {

        myService.completeTask(taskId, variables);

        return ResponseEntity.ok("✅ Tarea completada con ID: " + taskId +
                " | Variables: " + variables.toString());
    }



    // Clase interna para representar las tareas de manera simplificada
    static class TaskRepresentation {
        private final String id;
        private final String name;
        private final Map<String, Object> variables; // Variables del proceso

        public TaskRepresentation(String id, String name, Map<String, Object> variables) {
            this.id = id;
            this.name = name;
            this.variables = variables;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Map<String, Object> getVariables() {
            return variables;
        }
    }

    @GetMapping("/image/{processInstanceId}")
    public void image(@PathVariable String processInstanceId, HttpServletResponse response) throws Exception {
        response.setContentType("image/png");

        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (processInstance == null) {
            throw new RuntimeException("No existe el proceso con ID: " + processInstanceId);
        }

        BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());

        List<String> activeActivities;
        try {
            activeActivities = runtimeService.getActiveActivityIds(processInstanceId);
        } catch (Exception ex) {
            activeActivities = List.of(); // Proceso finalizado
        }

        List<String> completedActivities = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .finished()
                .list()
                .stream()
                .map(h -> h.getActivityId())
                .toList();


        DefaultProcessDiagramGenerator generator = new DefaultProcessDiagramGenerator();

        InputStream is = generator.generateDiagram(
                bpmnModel,
                "png",
                completedActivities,
                activeActivities,
                "Arial", "Arial", "Arial",
                processEngine.getProcessEngineConfiguration().getClassLoader(),
                1.0,
                true
        );

        // Escribe directamente los bytes al response
        is.transferTo(response.getOutputStream());
        response.flushBuffer(); // Asegura que la imagen se envíe
    }

    @GetMapping("/activo/{propuestaId}")
    public String obtenerProcesoActivo(@PathVariable String propuestaId) {
        String idProceso = myService.obtenerIdInstanciaActivaPorPropuestaId(propuestaId);

        if (idProceso != null) {
            return idProceso;
        } else {
            return "No hay proceso activo con propuestaId: " + propuestaId;
        }
    }

}