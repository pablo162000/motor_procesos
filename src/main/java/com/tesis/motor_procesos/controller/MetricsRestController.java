package com.tesis.motor_procesos.controller;

import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/metrics")
public class MetricsRestController {


    @Autowired
    private HistoryService historyService;


    @Autowired
    private RuntimeService runtimeService;



    @GetMapping("/proceso/{nombreproceso}/estado/activos")
    public long getActivos(@PathVariable String nombreproceso,
                           @RequestParam("idDireccion") Integer idDireccion,
                           @RequestParam(value = "desde") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                           @RequestParam(value = "hasta") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {


        ProcessInstanceQuery query =runtimeService.
                createProcessInstanceQuery()
                .processDefinitionKey(nombreproceso)
                .active();

        if (!Objects.equals(idDireccion, 0)) {
            query = query.variableValueEquals("idDireccion", idDireccion);
        }

        if(!Objects.equals(hasta, 0) && !Objects.equals(desde, 0)){
            Instant desdeInstant = desde.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant hastaInstant = hasta.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant(); // incluir el día 'hasta'

            query = query.startedAfter(Date.from(desdeInstant))
                    .startedBefore(Date.from(hastaInstant));
        }

        return query.count();
    }


    @GetMapping("/proceso/{nombreproceso}/estado/activos/fechacreacion")
    public Map<String, Long> getActivosFechaCreacion(@PathVariable String nombreproceso,
                                                     @RequestParam("idDireccion") Integer idDireccion,
                                                     @RequestParam(value = "desde") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                                     @RequestParam(value = "hasta") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        ProcessInstanceQuery query =runtimeService.
                createProcessInstanceQuery()
                .processDefinitionKey(nombreproceso)
                .active();

        if (!Objects.equals(idDireccion, 0)) {
            query = query.variableValueEquals("idDireccion", idDireccion);
        }


        if(!Objects.equals(hasta, 0) && !Objects.equals(desde, 0)){
            Instant desdeInstant = desde.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant hastaInstant = hasta.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant(); // incluir el día 'hasta'

            query = query.startedAfter(Date.from(desdeInstant))
                    .startedBefore(Date.from(hastaInstant));
        }


        List<ProcessInstance> instances = query.list();

        return instances.stream()
                .collect(Collectors.groupingBy(
                        h -> h.getStartTime().toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                                .toString(),
                        Collectors.counting()
                ));
    }


    @GetMapping("/proceso/{nombreproceso}/estado/completado")
    public long getCompletados(@PathVariable String nombreproceso,
                               @RequestParam("idDireccion") Integer idDireccion,
                               @RequestParam(value = "desde") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                               @RequestParam(value = "hasta") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {


        HistoricProcessInstanceQuery query = historyService
                .createHistoricProcessInstanceQuery()
                .processDefinitionKey(nombreproceso)
                .finished();

        if (!Objects.equals(idDireccion, 0)) {
            query = query.variableValueEquals("idDireccion", idDireccion);
        }

        if(!Objects.equals(hasta, 0) && !Objects.equals(desde, 0)){
            Instant desdeInstant = desde.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant hastaInstant = hasta.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant(); // incluir el día 'hasta'

            query = query.startedAfter(Date.from(desdeInstant))
                    .startedBefore(Date.from(hastaInstant));
        }

        List<HistoricProcessInstance> instances = query.list();


        return instances.stream().count();
    }


    @GetMapping("/proceso/{nombreproceso}/tareas/completadas")
    public Map<String, Long> tareasCompletadasPorFecha(
            @RequestParam("desde") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam("hasta") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam("idDireccion") Integer idDireccion,
            @PathVariable String nombreproceso) {
        // Convertir a instantes
        Instant desdeInstant = desde.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant hastaInstant = hasta.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant(); // incluir el día 'hasta'

        HistoricTaskInstanceQuery query= historyService.
                createHistoricTaskInstanceQuery()
                .processDefinitionKey(nombreproceso)
                .taskCompletedAfter(Date.from(desdeInstant))
                .taskCompletedBefore(Date.from(hastaInstant))
                .finished();

        if (!Objects.equals(idDireccion, 0)) {
            query = query.processVariableValueEquals("idDireccion", idDireccion);
        }

        List<HistoricTaskInstance> tareas =query.list();

        // Agrupar por fecha de finalización (formato: yyyy-MM-dd)
        return tareas.stream()
                .filter(t -> t.getEndTime() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getEndTime()
                                .toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                                .toString(),
                        Collectors.counting()
                ));
    }




    @GetMapping("/proceso/{nombreproceso}/duracionPromedio")
    public double calcularDuracionPromedio(@RequestParam("idDireccion") Integer idDireccion,
                                           @PathVariable String nombreproceso) {

        HistoricProcessInstanceQuery query = historyService
                .createHistoricProcessInstanceQuery()
                .processDefinitionKey(nombreproceso)
                .finished();

        if (!Objects.equals(idDireccion, 0)) {
            query = query.variableValueEquals("idDireccion", idDireccion);
        }

        List<HistoricProcessInstance> instancias = query.list();

        return instancias.stream()
                .mapToLong(inst -> inst.getEndTime().getTime() - inst.getStartTime().getTime())
                .average()
                .orElse(0.0);
    }

    /*
    @GetMapping("/usuarios/assignees")
    public List<String> getAssignees() {
        return historyService.createHistoricTaskInstanceQuery()
                .list()
                .stream()
                .map(HistoricTaskInstance::getAssignee)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

     */
/*
    @GetMapping("/proceso/{nombreproceso}/completados-por-dia")
    public Map<String, Long> getCompletadosPorDia(@PathVariable String nombreproceso,
                                                  @RequestParam("idDireccion") Integer idDireccion) {
        HistoricProcessInstanceQuery query = historyService
                .createHistoricProcessInstanceQuery()
                .processDefinitionKey(nombreproceso)
                .finished();

        if (!Objects.equals(idDireccion, 0)) {
            query = query.variableValueEquals("idDireccion", idDireccion);
        }

        List<HistoricProcessInstance> instances = query.list();

        return instances.stream()
                .collect(Collectors.groupingBy(
                        h -> h.getEndTime().toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                                .toString(),
                        Collectors.counting()
                ));
    }

 */

    @GetMapping("/proceso/{nombreproceso}/completados-por-dia")
    public Map<String, Long> getCompletadosPorDiaRangoFechas(@PathVariable String nombreproceso,
                                                             @RequestParam("desde") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                                             @RequestParam("hasta") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                                             @RequestParam("idDireccion") Integer idDireccion) {

        //Instant desdeInstant = desde.atStartOfDay(ZoneId.systemDefault()).toInstant();
        //Instant hastaInstant = hasta.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant(); // incluir el día 'hasta'


        HistoricProcessInstanceQuery query = historyService
                .createHistoricProcessInstanceQuery()
                .processDefinitionKey(nombreproceso)
                .finished();

        if (!Objects.equals(idDireccion, 0)) {
            query = query.variableValueEquals("idDireccion", idDireccion);
        }

        if(!Objects.equals(hasta, 0) && !Objects.equals(desde, 0)){
            Instant desdeInstant = desde.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant hastaInstant = hasta.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant(); // incluir el día 'hasta'

            query = query.startedAfter(Date.from(desdeInstant))
                    .startedBefore(Date.from(hastaInstant));
        }


        List<HistoricProcessInstance> instances = query.list();

        return instances.stream()
                .collect(Collectors.groupingBy(
                        h -> h.getEndTime().toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                                .toString(),
                        Collectors.counting()
                ));
    }




    @GetMapping("/instancia/{processInstanceId}")
    public ResponseEntity<Map<String, Object>> obtenerMetricas(@PathVariable String processInstanceId) {
        Map<String, Object> metrics = new HashMap<>();

        // Instancia histórica
        HistoricProcessInstance instance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (instance == null) {
            return ResponseEntity.notFound().build();
        }

        // Tiempo total del proceso
        if (instance.getEndTime() != null) {
            long durationMillis = instance.getEndTime().getTime() - instance.getStartTime().getTime();
            metrics.put("duracionTotalProcesoMillis", durationMillis);
        }

        List<String> tiposExcluidos = List.of(
                "boundaryEvent",
                "exclusiveGateway",
                "startEvent",
                "endEvent",
                "sequenceFlow",
                "parallelGateway"
        );

        // Actividades históricas
        List<org.flowable.engine.history.HistoricActivityInstance> actividades =
                historyService.createHistoricActivityInstanceQuery()
                        .processInstanceId(processInstanceId)
                        .orderByHistoricActivityInstanceStartTime()
                        .asc()
                        .list()
                        .stream()
                        .filter(a -> !tiposExcluidos.contains(a.getActivityType()))
                        .collect(Collectors.toList());

        List<Map<String, Object>> actividadMetrics = new ArrayList<>();
        Map<String, List<Long>> duracionesPorTipo = new HashMap<>();

        long mayorDuracion = 0;
        String cuelloBotella = "";

        for (var act : actividades) {
            if (act.getEndTime() == null) continue; // ignorar actividades en curso

            long duracion = act.getEndTime().getTime() - act.getStartTime().getTime();

            Map<String, Object> info = new HashMap<>();
            info.put("actividadId", act.getActivityId());
            info.put("nombre", act.getActivityName());
            info.put("tipo", act.getActivityType());
            info.put("asignadoA", act.getAssignee());
            info.put("inicio", act.getStartTime());
            info.put("fin", act.getEndTime());
            info.put("duracionMillis", duracion);

            actividadMetrics.add(info);

            // Agrupación por tipo
            duracionesPorTipo.computeIfAbsent(act.getActivityType(), k -> new ArrayList<>()).add(duracion);

            // Determinar cuello de botella
            if (duracion > mayorDuracion) {
                mayorDuracion = duracion;
                cuelloBotella = act.getActivityName() + " (" + act.getActivityId() + ")";
            }
        }

        // Métricas por tipo
        List<Map<String, Object>> resumenPorTipo = duracionesPorTipo.entrySet().stream()
                .map(entry -> {
                    String tipo = entry.getKey();
                    List<Long> duraciones = entry.getValue();
                    long total = duraciones.stream().mapToLong(Long::longValue).sum();
                    long promedio = total / duraciones.size();
                    Map<String, Object> resumen = new HashMap<>();
                    resumen.put("tipo", tipo);
                    resumen.put("cantidad", duraciones.size());
                    resumen.put("duracionTotalMillis", total);
                    resumen.put("duracionPromedioMillis", promedio);
                    return resumen;
                }).toList();

        metrics.put("actividades", actividadMetrics);
        metrics.put("cuelloBotella", cuelloBotella);
        metrics.put("tareasCompletadas", actividadMetrics.size());
        metrics.put("resumenPorTipo", resumenPorTipo);

        return ResponseEntity.ok(metrics);
    }


    @GetMapping("/procesos/{nombreproceso}")
    public ResponseEntity<?> getMetrics(@PathVariable String nombreproceso,
                                        @RequestParam("idDireccion") Integer idDireccion,
                                        @RequestParam(value = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                        @RequestParam(value = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        HistoricProcessInstanceQuery query = historyService
                .createHistoricProcessInstanceQuery()
                .processDefinitionKey(nombreproceso)
                .finished();

        if (!Objects.equals(idDireccion, 0)) {
            query = query.variableValueEquals("idDireccion", idDireccion);
        }

        if(!Objects.equals(hasta, 0) && !Objects.equals(desde, 0)){
            Instant desdeInstant = desde.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant hastaInstant = hasta.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant(); // incluir el día 'hasta'

            query = query.startedAfter(Date.from(desdeInstant))
                    .startedBefore(Date.from(hastaInstant));
        }

        List<HistoricProcessInstance> instances = query.list();


        if (instances.isEmpty()) {
            return ResponseEntity.ok("No hay instancias finalizadas para ese proceso.");
        }

        long totalDuration = 0;
        Map<String, List<Long>> activityDurations = new HashMap<>();
        Map<String, Integer> activityCounts = new HashMap<>();
        Map<String, Long> userDurations = new HashMap<>();
        Map<String, Integer> userCounts = new HashMap<>();



        for (HistoricProcessInstance instance : instances) {
            totalDuration += instance.getDurationInMillis();

            List<String> tiposExcluidos = List.of(
                    "boundaryEvent",
                    "exclusiveGateway",
                    "startEvent",
                    "endEvent",
                    "sequenceFlow",
                    "parallelGateway"
            );


            List<HistoricActivityInstance> activities = historyService
                    .createHistoricActivityInstanceQuery()
                    .processInstanceId(instance.getId())
                    .finished()
                    .list()
                    .stream()
                    .filter(a -> !tiposExcluidos.contains(a.getActivityType()))
                    .collect(Collectors.toList());


            for (HistoricActivityInstance activity : activities) {
                if (activity.getDurationInMillis() != null) {
                    // Duración por actividad
                    activityDurations
                            .computeIfAbsent(activity.getActivityId(), k -> new ArrayList<>())
                            .add(activity.getDurationInMillis());

                    activityCounts.put(activity.getActivityId(),
                            activityCounts.getOrDefault(activity.getActivityId(), 0) + 1);

                    // Duración por usuario
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

        averageActivityDurations.put("CalifiacaionRevisores", averageActivityDurations.get("revisarR1")+averageActivityDurations.get("revisarR2"));
        averageActivityDurations.remove("revisarR1");
        averageActivityDurations.remove("revisarR2");

        averageActivityDurations.put("RespuestaFinal", averageActivityDurations.get("generarActa")+averageActivityDurations.get("notificarRechazo"));
        averageActivityDurations.remove("notificarRechazo");
        averageActivityDurations.remove("generarActa");

        Map<String, Double> averageUserDurations = new HashMap<>();
        for (String user : userDurations.keySet()) {
            double avg = userDurations.get(user) / (double) userCounts.get(user);
            averageUserDurations.put(user, avg);
        }

        Map<String, String> fastAndLow = new HashMap<>();

       fastAndLow.put("Tarea mas rapida", averageActivityDurations.entrySet()
               .stream()
               .min(Map.Entry.comparingByValue())
               .orElse(null).getKey()
       );

        fastAndLow.put("Tarea mas lenta", averageActivityDurations.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null).getKey()
        );


        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalInstances", instances.size());
        result.put("averageProcessDurationMs", averageProcessDuration);
        result.put("averageActivityDurationsMs", averageActivityDurations);
        result.put("averageUserDurationsMs", averageUserDurations);
        result.put("TareasFastaAndLow", fastAndLow);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/procesos/{proceso}/revisiones-por-usuario")
    public ResponseEntity<Map<String, Object>> getTopRevisores(@PathVariable String proceso,
                                                               @RequestParam("idDireccion") Integer idDireccion,
                                                               @RequestParam(value = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                                               @RequestParam(value = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        HistoricProcessInstanceQuery query = historyService
                .createHistoricProcessInstanceQuery()
                .processDefinitionKey(proceso)
                .finished();

        if (!Objects.equals(idDireccion, 0)) {
            query = query.variableValueEquals("idDireccion", idDireccion);
        }

        if(!Objects.equals(hasta, 0) && !Objects.equals(desde, 0)){
            Instant desdeInstant = desde.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant hastaInstant = hasta.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant(); // incluir el día 'hasta'

            query = query.startedAfter(Date.from(desdeInstant))
                    .startedBefore(Date.from(hastaInstant));
        }

        List<HistoricProcessInstance> instances = query.list();

        if (instances.isEmpty()) {
            return ResponseEntity.ok(Map.of("mensaje", "No hay instancias finalizadas para ese proceso."));


        }

        Map<String, Integer> revisionesPorUsuario = new HashMap<>();

        System.out.println("------"+instances);

        for (HistoricProcessInstance instance : instances) {

            List<HistoricActivityInstance> actividades = historyService
                    .createHistoricActivityInstanceQuery()
                    .processInstanceId(instance.getId())
                    .finished()
                    .list();

            for (HistoricActivityInstance act : actividades) {
                if (act.getActivityId() == null) continue;

                if (act.getActivityId().equals("revisarR1") || act.getActivityId().equals("revisarR2")) {
                    String usuario = act.getAssignee();
                    if (usuario != null) {
                        revisionesPorUsuario.put(usuario,
                                revisionesPorUsuario.getOrDefault(usuario, 0) + 1);
                    }
                }
            }
        }

        // Crear lista ordenada por cantidad descendente
        List<Map.Entry<String, Integer>> ranking = revisionesPorUsuario.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(5)
                .toList();

        List<Map<String, Object>> top5 = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : ranking) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("usuario", entry.getKey());
            item.put("cantidadRevisiones", entry.getValue());
            top5.add(item);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("rankingTop5Revisores", top5);

        return ResponseEntity.ok(response);
    }



}
