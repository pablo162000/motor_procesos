package com.tesis.motor_procesos.controller;

import com.tesis.motor_procesos.service.MyService;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.flowable.task.api.Task;
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

    @Autowired
    private MyService myService;


    @GetMapping("/procesos/{nombreproceso}/estado/activos")
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


    @GetMapping("/procesos/{nombreproceso}/estado/activos/fecha-creacion")
    public ResponseEntity<?> getActivosFechaCreacion(@PathVariable String nombreproceso,
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

        if (instances.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyMap());
        }

        return ResponseEntity.ok(instances.stream()
                .collect(Collectors.groupingBy(
                        h -> h.getStartTime().toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                                .toString(),
                        Collectors.counting()
                )));
    }


    @GetMapping("/procesos/{nombreproceso}/estado/completado")
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


    @GetMapping("/procesos/{nombreproceso}/tareas/completadas")
    public ResponseEntity<?> tareasCompletadasPorFecha(
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

        if (tareas.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyMap());
        }

        // Agrupar por fecha de finalización (formato: yyyy-MM-dd)
        return  ResponseEntity.ok(tareas.stream()
                .filter(t -> t.getEndTime() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getEndTime()
                                .toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                                .toString(),
                        Collectors.counting()
                )));
    }


    @GetMapping("/procesos/{nombreproceso}/tareas/pendientes")
    public ResponseEntity<?> tareasPendientesPorUsuario(
            @RequestParam("desde") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam("hasta") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam("idDireccion") Integer idDireccion,
            @PathVariable String nombreproceso) {

        LocalDate marcador = LocalDate.of(1900, 1, 1);


        List<String> usuarios = historyService.createHistoricTaskInstanceQuery()
                .list()
                .stream()
                .map(HistoricTaskInstance::getAssignee)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        System.out.println("Tareas encontradas: " + usuarios.size());
        if (usuarios.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyMap());
        }

        Map<String, Long> tareasPendientes = new HashMap<>();


        usuarios.parallelStream().forEach(user -> {
            List<Task> tareasActivas = myService.getTasks(user);

 /*
            long count = tareasActivas.stream()

                    .filter(task -> {
                        // Validar proceso
                        String processId = task.getProcessDefinitionId();
                        boolean mismoProceso = processId != null && processId.startsWith(nombreproceso);
                        if (!mismoProceso) return false;

                        // Validar fecha
                        if (!desde.equals(marcador) && !hasta.equals(marcador)) {
                            LocalDate fechaCreacion = task.getCreateTime().toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate();
                            if (fechaCreacion.isBefore(desde) || fechaCreacion.isAfter(hasta)) {
                                return false;
                            }
                        }

                        // Validar idDireccion si no es 0
                        if (idDireccion != 0) {
                            Object varDireccion = runtimeService.getVariable(task.getExecutionId(), "idDireccion");
                            if (varDireccion == null || !varDireccion.equals(idDireccion)) {
                                return false;
                            }
                        }

                        return true;
                    })
                    .count();

            if (count > 0) {
                tareasPendientes.put(user, count);
            }

                     */

            long count = tareasActivas.stream()
                    .filter(task -> {
                        // Validar proceso
                        String processId = task.getProcessDefinitionId();
                        boolean mismoProceso = processId != null && processId.startsWith(nombreproceso);
                        if (!mismoProceso) return false;

                        // Si no se filtra por dirección
                        if (idDireccion == 0) return true;

                        // Obtener variable desde el runtimeService
                        Object varDireccion = null;
                        try {
                            varDireccion = runtimeService.getVariable(task.getExecutionId(), "idDireccion");
                        } catch (Exception e) {
                            // Opcional: loggear si hay problema
                            System.out.println("No se pudo obtener idDireccion para tarea: " + task.getId());
                        }

                        return varDireccion != null && varDireccion.equals(idDireccion);
                    })
                    .count();

            if (count > 0) {
                tareasPendientes.put(user, count);
            }
        });

        return ResponseEntity.ok(tareasPendientes);
    }


    @GetMapping("/procesos/{nombreproceso}/duracion-promedio")
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
                .orElse(0.0)/ 86400000.0;
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

    @GetMapping("/procesos/{nombreproceso}/completados-por-dia")
    public Map<String, Long> getCompletadosPorDiaRangoFechas(@PathVariable String nombreproceso,
                                                             @RequestParam("desde") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                                             @RequestParam("hasta") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                                             @RequestParam("idDireccion") Integer idDireccion) {
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


    @GetMapping("/procesos/instancia/{processInstanceId}")
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
            double durationMillis = instance.getEndTime().getTime() - instance.getStartTime().getTime();
            double duracionDias = Math.round((durationMillis / 86400000.0) * 1e7) / 1e7;
            metrics.put("duracionTotalProcesoDias", duracionDias);
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
        Map<String, List<Double>> duracionesPorTipo = new HashMap<>();

        Double mayorDuracion = 0.0;
        String cuelloBotella = "";

        for (var act : actividades) {
            if (act.getEndTime() == null) continue; // ignorar actividades en curso

            Double duracion = (act.getEndTime().getTime() - act.getStartTime().getTime())/ 86400000.0;

            Double duracionDias = Math.round(duracion * 1e7) / 1e7;

            Map<String, Object> info = new HashMap<>();
            info.put("actividadId", act.getActivityId());
            info.put("nombre", act.getActivityName());
            info.put("tipo", act.getActivityType());
            info.put("asignadoA", act.getAssignee());
            info.put("inicio", act.getStartTime());
            info.put("fin", act.getEndTime());
            info.put("duracionDias", duracionDias);

            actividadMetrics.add(info);

            // Agrupación por tipo
            duracionesPorTipo.computeIfAbsent(act.getActivityType(), k -> new ArrayList<>()).add(duracionDias);

            // Determinar cuello de botella
            if (duracionDias > mayorDuracion) {
                mayorDuracion = duracionDias;
                cuelloBotella = act.getActivityName() + " (" + act.getActivityId() + ")";
            }
        }

        // Métricas por tipo
        List<Map<String, Object>> resumenPorTipo = duracionesPorTipo.entrySet().stream()
                .map(entry -> {
                    String tipo = entry.getKey();
                    List<Double> duraciones = entry.getValue();
                    Double total = duraciones.stream().mapToDouble(Double::doubleValue).sum();
                    Double promedio = total / duraciones.size();
                    Map<String, Object> resumen = new HashMap<>();
                    resumen.put("tipo", tipo);
                    resumen.put("cantidad", duraciones.size());
                    resumen.put("duracionTotalDias", total);
                    resumen.put("duracionPromedioDias", promedio);
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


        if (instances.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyMap());
        }

        Double totalDuration = 0.0;
        Map<String, List<Double>> activityDurations = new HashMap<>();
        Map<String, Integer> activityCounts = new HashMap<>();
        Map<String, Double> userDurations = new HashMap<>();
        Map<String, Integer> userCounts = new HashMap<>();
        Map<String, Map<String, List<Double>>> duracionesPorActividadYUsuario = new HashMap<>();
        Map<String, Map<String, Double>> promedioActividadUsuario = new HashMap<>();



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
                            .add(activity.getDurationInMillis().doubleValue());

                    activityCounts.put(activity.getActivityId(),
                            activityCounts.getOrDefault(activity.getActivityId(), 0) + 1);

                    // Duración por usuario
                    if (activity.getAssignee() != null) {
                        userDurations.put(activity.getAssignee(),
                                userDurations.getOrDefault(activity.getAssignee(), 0.0) + activity.getDurationInMillis());

                        userCounts.put(activity.getAssignee(),
                                userCounts.getOrDefault(activity.getAssignee(), 0) + 1);
                    }

                    if (activity.getDurationInMillis() != null && activity.getAssignee() != null) {
                        // Duración por actividad y usuario
                        duracionesPorActividadYUsuario
                                .computeIfAbsent(activity.getActivityId(), k -> new HashMap<>())
                                .computeIfAbsent(activity.getAssignee(), k -> new ArrayList<>())
                                .add(activity.getDurationInMillis().doubleValue());
                    }

                    for (String actividad : duracionesPorActividadYUsuario.keySet()) {
                        Map<String, List<Double>> porUsuario = duracionesPorActividadYUsuario.get(actividad);
                        Map<String, Double> promedios = new HashMap<>();

                        for (String usuario : porUsuario.keySet()) {
                            List<Double> duraciones = porUsuario.get(usuario);
                            Double promedio = duraciones.stream().mapToDouble(Double::doubleValue).average().orElse(0.0)/ 86400000.0;
                            promedios.put(usuario, Math.round(promedio*1e7) / 1e7);
                        }

                        promedioActividadUsuario.put(actividad, promedios);
                    }

                }

            }

        }

        double averageProcessDuration = Math.round((totalDuration / (double) instances.size()/ 86400000.0)* 1e7) / 1e7;


        Map<String, Double> averageActivityDurations = new HashMap<>();
        for (String actId : activityDurations.keySet()) {
            List<Double> durations = activityDurations.get(actId);
            double avg = durations.stream().mapToDouble(Double::doubleValue).average().orElse(0)/ 86400000.0;
            Double avgDias =Math.round(avg * 1e7) / 1e7;
            averageActivityDurations.put(actId, avgDias);
        }

        averageActivityDurations.put("CalifiacaionRevisores", (averageActivityDurations.get("revisarR1")+averageActivityDurations.get("revisarR2"))/2);
        averageActivityDurations.remove("revisarR1");
        averageActivityDurations.remove("revisarR2");

        averageActivityDurations.put("RespuestaFinal", (averageActivityDurations.get("generarActa")+averageActivityDurations.get("notificarRechazo"))/2);
        averageActivityDurations.remove("notificarRechazo");
        averageActivityDurations.remove("generarActa");


        Map<String, Map<String, Double>> Secretarias = new HashMap<>();


        Secretarias.put("DesignacionRevisores", promedioActividadUsuario.get("designarRevisores"));

        //------------------------------------------------------------------------

        Map<String, Double> revisoresCombinados = new HashMap<>();

        revisoresCombinados.putAll(promedioActividadUsuario.get("revisarR1"));

        promedioActividadUsuario.get("revisarR2").forEach((usuario, duracion) ->
                revisoresCombinados.merge(usuario, duracion, (d1, d2) -> (d1 + d2) / 2)
        );

        //------------------------------------------------------------------------

        Map<String, Double> direccionCombinada = new HashMap<>();
        direccionCombinada.putAll(promedioActividadUsuario.get("generarActa"));

        promedioActividadUsuario.get("notificarRechazo").forEach((usuario, duracion) ->
                direccionCombinada.merge(usuario, duracion, (d1, d2) -> (d1 + d2) / 2)
        );

        Map<String, Map<String, Double>> direccion = new HashMap<>();

        direccion.put("validacionTema", promedioActividadUsuario.get("validacionTema"));
        direccion.put("respuestaFinal", direccionCombinada);


        Map<String, Double> averageUserDurations = new HashMap<>();
        for (String user : userDurations.keySet()) {
            Double avg = userDurations.get(user) / (double) userCounts.get(user)/ 86400000.0;
            Double avgDias =Math.round(avg * 1e7) / 1e7;
            averageUserDurations.put(user, avgDias);
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
        result.put("averageProcessDurationDias", averageProcessDuration);
        result.put("averageActivityDurationsDias", averageActivityDurations);
        result.put("averageUserDurationsDias", averageUserDurations);
        //result.put("promediosPorActividadYUsuario", promedioActividadUsuario);
        result.put("secretarias", promedioActividadUsuario.get("designarRevisores"));
        result.put("docentes", revisoresCombinados);
        result.put("direccion", direccion);
        result.put("TareasFastaAndLow", fastAndLow);

        return ResponseEntity.ok(result);
    }


    //---------------------------------------------------------------------------
    @GetMapping("/procesos/{nombreproceso}/roles")
    public ResponseEntity<?> getMetricsPorRoles(@PathVariable String nombreproceso,
                                                @RequestParam("idDireccion") Integer idDireccion,
                                                @RequestParam(value = "desde") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                                @RequestParam(value = "hasta") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                                @RequestParam(value = "rol") Integer rol) {

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
            return ResponseEntity.ok(Collections.emptyMap());
        }

        Double totalDuration = 0.0;
        Map<String, List<Double>> activityDurations = new HashMap<>();
        Map<String, Integer> activityCounts = new HashMap<>();
        Map<String, Double> userDurations = new HashMap<>();
        Map<String, Integer> userCounts = new HashMap<>();
        Map<String, Map<String, List<Double>>> duracionesPorActividadYUsuario = new HashMap<>();
        Map<String, Map<String, Double>> promedioActividadUsuario = new HashMap<>();



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
                            .add(activity.getDurationInMillis().doubleValue());

                    activityCounts.put(activity.getActivityId(),
                            activityCounts.getOrDefault(activity.getActivityId(), 0) + 1);

                    // Duración por usuario
                    if (activity.getAssignee() != null) {
                        userDurations.put(activity.getAssignee(),
                                userDurations.getOrDefault(activity.getAssignee(), 0.0) + activity.getDurationInMillis());

                        userCounts.put(activity.getAssignee(),
                                userCounts.getOrDefault(activity.getAssignee(), 0) + 1);
                    }

                    if (activity.getDurationInMillis() != null && activity.getAssignee() != null) {
                        // Duración por actividad y usuario
                        duracionesPorActividadYUsuario
                                .computeIfAbsent(activity.getActivityId(), k -> new HashMap<>())
                                .computeIfAbsent(activity.getAssignee(), k -> new ArrayList<>())
                                .add(activity.getDurationInMillis().doubleValue());
                    }

                    for (String actividad : duracionesPorActividadYUsuario.keySet()) {
                        Map<String, List<Double>> porUsuario = duracionesPorActividadYUsuario.get(actividad);
                        Map<String, Double> promedios = new HashMap<>();

                        for (String usuario : porUsuario.keySet()) {
                            List<Double> duraciones = porUsuario.get(usuario);
                            Double promedio = duraciones.stream().mapToDouble(Double::doubleValue).average().orElse(0.0)/ 86400000.0;
                            Double promedioDias = Math.round(promedio * 1e7) / 1e7;
                            promedios.put(usuario, promedioDias);
                        }

                        promedioActividadUsuario.put(actividad, promedios);
                    }

                }

            }

        }


        //------------------------------------------------------------------------

        Map<String, Double> revisoresCombinados = new HashMap<>();

        //------------------------------------------------------------------------

        Map<String, Double> direccionCombinada = new HashMap<>();


        Map<String, Object> result = new LinkedHashMap<>();

        if (Integer.valueOf(0).equals(rol)) {
            Map<String, Double> designacion = promedioActividadUsuario.get("designarRevisores");
            if (designacion != null) {
                result.put("secretarias", designacion);
            }
        } else if (Integer.valueOf(1).equals(rol)) {
            Map<String, Double> revisarR1 = promedioActividadUsuario.get("revisarR1");
            Map<String, Double> revisarR2 = promedioActividadUsuario.get("revisarR2");

            if (revisarR1 != null) revisoresCombinados.putAll(revisarR1);
            if (revisarR2 != null) {
                revisarR2.forEach((usuario, duracion) ->
                        revisoresCombinados.merge(usuario, duracion, (d1, d2) -> (d1 + d2) / 2)
                );
            }

            result.put("docentes", revisoresCombinados);

        } else if (Integer.valueOf(2).equals(rol)) {
            Map<String, Double> generarActa = promedioActividadUsuario.get("generarActa");
            Map<String, Double> notificarRechazo = promedioActividadUsuario.get("notificarRechazo");
            Map<String, Double> validacionTema = promedioActividadUsuario.get("validacionTema");

            if (generarActa != null) direccionCombinada.putAll(generarActa);

            if (notificarRechazo != null) {
                notificarRechazo.forEach((usuario, duracion) ->
                        direccionCombinada.merge(usuario, duracion, (d1, d2) -> (d1 + d2) / 2)
                );
            }

            Map<String, Map<String, Double>> direccion = new HashMap<>();
            if (validacionTema != null) direccion.put("validacionTema", validacionTema);
            direccion.put("respuestaFinal", direccionCombinada);

            result.put("direccion", direccion);
        } else {
            return ResponseEntity.badRequest().body("Ese rol no existe");
        }


            return ResponseEntity.ok(result);
    }


    @GetMapping("/procesos/{proceso}/revisiones-por-usuario")
    public ResponseEntity<Map<String, Object>> getTopRevisores(@PathVariable String proceso,
                                                               @RequestParam("idDireccion") Integer idDireccion,
                                                               @RequestParam(value = "desde") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                                               @RequestParam(value = "hasta") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

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
