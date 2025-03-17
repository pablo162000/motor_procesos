package com.tesis.motor_procesos.runner;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.ProcessEngines;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
@Component
 class FlowableConsoleTest implements CommandLineRunner {
    private final RuntimeService runtimeService;
    private final TaskService taskService;

    public FlowableConsoleTest(RuntimeService runtimeService, TaskService taskService) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
    }

    @Override
    public void run(String... args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Who are you?");
        String employee = scanner.nextLine();

        System.out.println("How many holidays do you want to request?");
        Integer nrOfHolidays = Integer.valueOf(scanner.nextLine());

        System.out.println("Why do you need them?");
        String description = scanner.nextLine();

        scanner.close();  // Cierra el Scanner

        // Pasar los valores como variables del proceso
        Map<String, Object> variables = new HashMap<>();
        variables.put("employee", employee);
        variables.put("nrOfHolidays", nrOfHolidays);
        variables.put("description", description);

        // Iniciar la instancia del proceso
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("holidayRequest", variables);

        //System.out.println("Process instance started with ID: " + processInstance.getId());

        // Consultar tareas pendientes del grupo "managers"
        List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup("managers").list();

        if (tasks.isEmpty()) {
            System.out.println("No hay tareas pendientes para el grupo 'managers'.");
            return;
        }

        System.out.println("=== TAREAS PENDIENTES PARA 'managers' ===");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println((i + 1) + ") " + tasks.get(i).getName() + " (ID: " + tasks.get(i).getId() + ")");
        }

        System.out.println("Seleccione el número de la tarea a completar:");
        int taskIndex = Integer.parseInt(scanner.nextLine());

        if (taskIndex < 1 || taskIndex > tasks.size()) {
            System.out.println("Selección inválida.");
            return;
        }

        Task task = tasks.get(taskIndex - 1);
        String taskId = task.getId();

        // Obtener variables de proceso
        Map<String, Object> processVariables = taskService.getVariables(taskId);
        System.out.println(processVariables.get("employee") + " solicita " +
                processVariables.get("nrOfHolidays") + " días de vacaciones. ¿Aprobar? (yes/no)");

        /*
        boolean approved = "yes".equalsIgnoreCase(scanner.nextLine());

        // Completar la tarea con la decisión
        taskService.complete(taskId, Map.of("approved", approved));

        System.out.println("Tarea " + taskId + " completada con decisión: " + approved);
        */



    }
}
