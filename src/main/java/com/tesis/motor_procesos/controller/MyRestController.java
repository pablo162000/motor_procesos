package com.tesis.motor_procesos.controller;

import com.tesis.motor_procesos.service.MyService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/process")

public class MyRestController {

    @Autowired
    private MyService myService;

    @PostMapping
    public ResponseEntity<String> startProcess(@RequestBody Integer propuestaId) {
        myService.startProcess(propuestaId);
        return ResponseEntity.ok("Proceso iniciado con propuestaId: " + propuestaId);
    }
    // Endpoint para obtener las tareas asignadas a un usuario específico
    @RequestMapping(value = "/tasks", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TaskRepresentation> getTasks(@RequestParam String assignee) {
        List<Task> tasks = myService.getTasks(assignee);
        List<TaskRepresentation> dtos = new ArrayList<>();
        for (Task task : tasks) {
            dtos.add(new TaskRepresentation(task.getId(), task.getName()));
        }
        return dtos;
    }

    // Clase interna para representar las tareas de manera simplificada
    static class TaskRepresentation {
        private String id;
        private String name;

        public TaskRepresentation(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }


}