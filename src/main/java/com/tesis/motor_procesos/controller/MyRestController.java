package com.tesis.motor_procesos.controller;

import com.tesis.motor_procesos.service.MyService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/process")

public class MyRestController {

    @Autowired
    private MyService myService;

    @PostMapping("/iniciar")
    public ResponseEntity<String> iniciarProceso(
            @RequestParam Integer propuestaId,
            @RequestParam(required = false) Integer idEstudiante1,
            @RequestParam Integer idDireccion ,@RequestParam Integer idSecretaria)
    {
        myService.startProcess(propuestaId, idEstudiante1,  idDireccion, idSecretaria);
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


    @PostMapping("/tasks/complete")
    public ResponseEntity<String> completarTarea(
            @RequestParam String taskId,
            @RequestParam boolean validacionAprobada) {

        Map<String, Object> variables = new HashMap<>();
        variables.put("validacionAprobada", validacionAprobada);

        myService.completeTask(taskId, variables);

        return ResponseEntity.ok("✅ Tarea completada con ID: " + taskId + " | Validación: " + validacionAprobada);
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


}