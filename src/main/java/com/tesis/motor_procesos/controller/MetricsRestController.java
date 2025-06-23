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
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                           @RequestParam(value = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                           @RequestParam(value = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        ProcessInstanceQuery query =runtimeService.
                createProcessInstanceQuery()
                .processDefinitionKey(nombreproceso)
                .active();

        if (!Objects.equals(idDireccion, 0)) {
            query = query.variableValueEquals("idDireccion", idDireccion);
        }

        if((desde!=null) && (hasta!=null)){
            Instant desdeInstant = desde.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant hastaInstant = hasta.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant(); // incluir el día 'hasta'

            query = query.startedAfter(Date.from(desdeInstant))
                    .startedBefore(Date.from(hastaInstant));
        }
        boolean d =desde!=null;
        return query.count();
    }


    @GetMapping("/procesos/{nombreproceso}/estado/activos/fecha-creacion")
    public ResponseEntity<?> getActivosFechaCreacion(@PathVariable String nombreproceso,
                                                     @RequestParam("idDireccion") Integer idDireccion,
                                                     @RequestParam(value = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                                     @RequestParam(value = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        ProcessInstanceQuery query =runtimeService.
                createProcessInstanceQuery()
                .processDefinitionKey(nombreproceso)
                .active();

        if (!Objects.equals(idDireccion, 0)) {
            query = query.variableValueEquals("idDireccion", idDireccion);
        }


        if((desde!=null) && (hasta!=null)){
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
                               @RequestParam(value = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                               @RequestParam(value = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {


        HistoricProcessInstanceQuery query = historyService
                .createHistoricProcessInstanceQuery()
                .processDefinitionKey(nombreproceso)
                .finished();

        if (!Objects.equals(idDireccion, 0)) {
            query = query.variableValueEquals("idDireccion", idDireccion);
        }

        if((desde!=null) && (hasta!=null)){
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
            @RequestParam(value = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(value = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam("idDireccion") Integer idDireccion,
            @PathVariable String nombreproceso) {
        // Convertir a instantes

        HistoricTaskInstanceQuery query= historyService.
                createHistoricTaskInstanceQuery()
                .processDefinitionKey(nombreproceso)
                .finished();

        if((desde!=null) && (hasta!=null)){
            Instant desdeInstant = desde.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant hastaInstant = hasta.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant(); // incluir el día 'hasta'

            query = query.taskCompletedAfter(Date.from(desdeInstant))
                    .taskCompletedBefore(Date.from(hastaInstant));
        }

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
    public ResponseEntity<?> tareasPendientesPorUsuario(@RequestParam("idDireccion") Integer idDireccion,
                                                        @PathVariable String nombreproceso) {

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
                                                             @RequestParam(value = "desde",required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                                             @RequestParam(value = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                                             @RequestParam("idDireccion") Integer idDireccion) {
        HistoricProcessInstanceQuery query = historyService
                .createHistoricProcessInstanceQuery()
                .processDefinitionKey(nombreproceso)
                .finished();

        if (!Objects.equals(idDireccion, 0)) {
            query = query.variableValueEquals("idDireccion", idDireccion);
        }

        if((desde!=null) && (hasta!=null)){
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
                                        @RequestParam(value ="carrera", required = false) String carrera,
                                        @RequestParam(value ="periodo", required = false) String periodo,
                                        @RequestParam(value = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                        @RequestParam(value = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        HistoricProcessInstanceQuery query = historyService
                .createHistoricProcessInstanceQuery()
                .processDefinitionKey(nombreproceso)
                .finished();


        if (carrera != null) {
            query = query.variableValueEquals("carrera", carrera);

        }

        if (periodo != null) {
            query = query.variableValueEquals("periodo", periodo);
        }

        if (desde != null && hasta != null) {
            Instant desdeInstant = desde.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant hastaInstant = hasta.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
            query = query.startedAfter(Date.from(desdeInstant)).startedBefore(Date.from(hastaInstant));
        }

        List<HistoricProcessInstance> instances = query.list();
        if (instances.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyMap());
        }

        double totalDuration = 0.0;
        Map<String, List<Double>> activityDurations = new HashMap<>();
        Map<String, Integer> activityCounts = new HashMap<>();
        Map<String, Double> userDurations = new HashMap<>();
        Map<String, Integer> userCounts = new HashMap<>();
        Map<String, Map<String, List<Double>>> duracionesPorActividadYUsuario = new HashMap<>();

        for (HistoricProcessInstance instance : instances) {
            totalDuration += instance.getDurationInMillis();

            List<HistoricActivityInstance> activities = historyService
                    .createHistoricActivityInstanceQuery()
                    .processInstanceId(instance.getId())
                    .finished()
                    .list()
                    .stream()
                    .filter(a -> !List.of("boundaryEvent", "exclusiveGateway", "startEvent", "endEvent", "sequenceFlow", "parallelGateway")
                            .contains(a.getActivityType()))
                    .collect(Collectors.toList());

            for (HistoricActivityInstance activity : activities) {
                if (activity.getDurationInMillis() == null) continue;

                // Duración por actividad
                activityDurations
                        .computeIfAbsent(activity.getActivityId(), k -> new ArrayList<>())
                        .add(activity.getDurationInMillis().doubleValue());

                activityCounts.put(activity.getActivityId(),
                        activityCounts.getOrDefault(activity.getActivityId(), 0) + 1);

                // Obtener nombre de la variable asociada a la actividad
                String nombreVariable = switch (activity.getActivityId()) {
                    case "designarRevisores" -> "nombreSecretaria";
                    case "revisarR1" -> "nombreRevisor1";
                    case "revisarR2" -> "nombreRevisor2";
                    case "validacionTema", "generarActa", "notificarRechazo" -> "carrera";
                    default -> null;
                };

                String usuario = "Sin Responsable";
                if (nombreVariable != null) {
                    HistoricVariableInstance variable = historyService
                            .createHistoricVariableInstanceQuery()
                            .processInstanceId(instance.getId())
                            .variableName(nombreVariable)
                            .singleResult();

                    if (variable != null && variable.getValue() != null) {
                        usuario = String.valueOf(variable.getValue());
                    }
                }

                // Duración por usuario
                userDurations.put(usuario,
                        userDurations.getOrDefault(usuario, 0.0) + activity.getDurationInMillis());

                userCounts.put(usuario,
                        userCounts.getOrDefault(usuario, 0) + 1);

                // Duración por actividad y usuario
                duracionesPorActividadYUsuario
                        .computeIfAbsent(activity.getActivityId(), k -> new HashMap<>())
                        .computeIfAbsent(usuario, k -> new ArrayList<>())
                        .add(activity.getDurationInMillis().doubleValue());
            }
        }

        // Calcular promedios por actividad y usuario
        Map<String, Map<String, Double>> promedioActividadUsuario = new HashMap<>();
        for (var entryActividad : duracionesPorActividadYUsuario.entrySet()) {
            String actividad = entryActividad.getKey();
            Map<String, List<Double>> porUsuario = entryActividad.getValue();
            Map<String, Double> promedios = new HashMap<>();

            for (var entryUsuario : porUsuario.entrySet()) {
                double promedio = entryUsuario.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0) / 86400000.0;
                promedios.put(entryUsuario.getKey(), Math.round(promedio * 1e7) / 1e7);
            }

            promedioActividadUsuario.put(actividad, promedios);
        }

        double averageProcessDuration = Math.round((totalDuration / instances.size() / 86400000.0) * 1e7) / 1e7;

        Map<String, Double> averageActivityDurations = new HashMap<>();
        for (var entry : activityDurations.entrySet()) {
            double avg = entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0) / 86400000.0;
            averageActivityDurations.put(entry.getKey(), Math.round(avg * 1e7) / 1e7);
        }

        // Agrupar revisarR1 y revisarR2 en CalificacionRevisores
        Double r1 = averageActivityDurations.get("revisarR1");
        Double r2 = averageActivityDurations.get("revisarR2");
        if (r1 != null && r2 != null) {
            averageActivityDurations.put("CalifiacaionRevisores", (r1 + r2) / 2);
        } else if (r1 != null) {
            averageActivityDurations.put("CalifiacaionRevisores", r1);
        } else if (r2 != null) {
            averageActivityDurations.put("CalifiacaionRevisores", r2);
        }
        averageActivityDurations.remove("revisarR1");
        averageActivityDurations.remove("revisarR2");

        // Agrupar generarActa y notificarRechazo en RespuestaFinal
        Double acta = averageActivityDurations.get("generarActa");
        Double rechazo = averageActivityDurations.get("notificarRechazo");

        if (acta != null && rechazo != null) {
            averageActivityDurations.put("RespuestaFinal", (acta + rechazo) / 2);
        } else if (acta != null) {
            averageActivityDurations.put("RespuestaFinal", acta);
        } else if (rechazo != null) {
            averageActivityDurations.put("RespuestaFinal", rechazo);
        }
        averageActivityDurations.remove("generarActa");
        averageActivityDurations.remove("notificarRechazo");

        // Promedios por usuario
        Map<String, Double> averageUserDurations = new HashMap<>();
        for (var entry : userDurations.entrySet()) {
            double avg = entry.getValue() / userCounts.get(entry.getKey()) / 86400000.0;
            averageUserDurations.put(entry.getKey(), Math.round(avg * 1e7) / 1e7);
        }

        // Tarea más rápida y más lenta
        Map<String, String> fastAndLow = new HashMap<>();
        averageActivityDurations.entrySet().stream().min(Map.Entry.comparingByValue())
                .ifPresent(e -> fastAndLow.put("Tarea mas rapida", e.getKey()));
        averageActivityDurations.entrySet().stream().max(Map.Entry.comparingByValue())
                .ifPresent(e -> fastAndLow.put("Tarea mas lenta", e.getKey()));

        // Secciones por rol
        Map<String, Map<String, Double>> Secretarias = new HashMap<>();
        if (promedioActividadUsuario.containsKey("designarRevisores")) {
            Secretarias.put("DesignacionRevisores", promedioActividadUsuario.get("designarRevisores"));
        }

        Map<String, Double> revisoresCombinados = new HashMap<>();
        Map<String, Double> r1porUsuario = promedioActividadUsuario.getOrDefault("revisarR1", Map.of());
        Map<String, Double> r2porUsuario = promedioActividadUsuario.getOrDefault("revisarR2", Map.of());
        revisoresCombinados.putAll(r1porUsuario);
        r2porUsuario.forEach((usuario, duracion) ->
                revisoresCombinados.merge(usuario, duracion, (d1, d2) -> (d1 + d2) / 2));

        Map<String, Double> direccionCombinada = new HashMap<>();
        Map<String, Double> generarActaUsuario = promedioActividadUsuario.getOrDefault("generarActa", Map.of());
        Map<String, Double> notificarRechazoUsuario = promedioActividadUsuario.getOrDefault("notificarRechazo", Map.of());
        direccionCombinada.putAll(generarActaUsuario);
        notificarRechazoUsuario.forEach((usuario, duracion) ->
                direccionCombinada.merge(usuario, duracion, (d1, d2) -> (d1 + d2) / 2));

        Map<String, Map<String, Double>> direccion = new HashMap<>();
        Map<String, Double> validacionTema = promedioActividadUsuario.get("validacionTema");
        if (validacionTema != null) {
            direccion.put("validacionTema", validacionTema);
        }
        direccion.put("respuestaFinal", direccionCombinada);

        // Resultado final
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalInstances", instances.size());
        result.put("averageProcessDurationDias", averageProcessDuration);
        result.put("averageActivityDurationsDias", averageActivityDurations);
        result.put("averageUserDurationsDias", averageUserDurations);
        result.put("secretarias", Secretarias.get("DesignacionRevisores"));
        result.put("docentes", revisoresCombinados);
        result.put("direccion", direccion);
        result.put("TareasFastaAndLow", fastAndLow);

        return ResponseEntity.ok(result);
    }


    //---------------------------------------------------------------------------
    @GetMapping("/procesos/{nombreproceso}/roles")
    public ResponseEntity<?> getMetricsPorRoles(@PathVariable String nombreproceso,
                                                @RequestParam(value ="carrera", required = false) String carrera,
                                                @RequestParam(value ="periodo", required = false) String periodo,
                                                @RequestParam(value = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                                @RequestParam(value = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                                @RequestParam(value = "rol") Integer rol) {

        HistoricProcessInstanceQuery query = historyService
                .createHistoricProcessInstanceQuery()
                .processDefinitionKey(nombreproceso)
                .finished();

        if (carrera != null) {
            query = query.variableValueEquals("carrera", carrera);

        }

        if (periodo != null) {
            query = query.variableValueEquals("periodo", periodo);
        }


        if (desde != null && hasta != null) {
            Instant desdeInstant = desde.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant hastaInstant = hasta.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
            query = query.startedAfter(Date.from(desdeInstant)).startedBefore(Date.from(hastaInstant));
        }

        List<HistoricProcessInstance> instances = query.list();

        if (instances.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyMap());
        }

        Map<String, Map<String, List<Double>>> duracionesPorActividadYUsuario = new HashMap<>();

        for (HistoricProcessInstance instance : instances) {
            List<HistoricActivityInstance> activities = historyService
                    .createHistoricActivityInstanceQuery()
                    .processInstanceId(instance.getId())
                    .finished()
                    .list()
                    .stream()
                    .filter(a -> !List.of(
                            "boundaryEvent", "exclusiveGateway", "startEvent",
                            "endEvent", "sequenceFlow", "parallelGateway"
                    ).contains(a.getActivityType()))
                    .collect(Collectors.toList());

            for (HistoricActivityInstance activity : activities) {
                if (activity.getDurationInMillis() == null) continue;

                String nombreVariable = switch (activity.getActivityId()) {
                    case "designarRevisores" -> "nombreSecretaria";
                    case "revisarR1" -> "nombreRevisor1";
                    case "revisarR2" -> "nombreRevisor2";
                    case "validacionTema", "generarActa", "notificarRechazo" -> "carrera";
                    default -> null;
                };

                String usuario = "Sin Responsable";
                if (nombreVariable != null) {
                    HistoricVariableInstance variable = historyService
                            .createHistoricVariableInstanceQuery()
                            .processInstanceId(instance.getId())
                            .variableName(nombreVariable)
                            .singleResult();

                    if (variable != null && variable.getValue() != null) {
                        usuario = String.valueOf(variable.getValue());
                    }
                }

                duracionesPorActividadYUsuario
                        .computeIfAbsent(activity.getActivityId(), k -> new HashMap<>())
                        .computeIfAbsent(usuario, k -> new ArrayList<>())
                        .add(activity.getDurationInMillis().doubleValue());
            }
        }

        // Calcular promedios por actividad y usuario (una sola vez)
        Map<String, Map<String, Double>> promedioActividadUsuario = new HashMap<>();

        for (var entryActividad : duracionesPorActividadYUsuario.entrySet()) {
            String actividad = entryActividad.getKey();
            Map<String, List<Double>> porUsuario = entryActividad.getValue();
            Map<String, Double> promedios = new HashMap<>();

            for (var entryUsuario : porUsuario.entrySet()) {
                String usuario = entryUsuario.getKey();
                List<Double> duraciones = entryUsuario.getValue();
                double promedio = duraciones.stream().mapToDouble(Double::doubleValue).average().orElse(0.0) / 86400000.0;
                double promedioRedondeado = Math.round(promedio * 1e7) / 1e7;
                promedios.put(usuario, promedioRedondeado);
            }

            promedioActividadUsuario.put(actividad, promedios);
        }

        Map<String, Object> result = new LinkedHashMap<>();

        if (rol == 0) {
            result.put("secretarias", promedioActividadUsuario.getOrDefault("designarRevisores", Map.of()));
        } else if (rol == 1) {
            Map<String, Double> revisarR1 = promedioActividadUsuario.getOrDefault("revisarR1", Map.of());
            Map<String, Double> revisarR2 = promedioActividadUsuario.getOrDefault("revisarR2", Map.of());
            Map<String, Double> revisores = new HashMap<>(revisarR1);

            revisarR2.forEach((usuario, duracion) ->
                    revisores.merge(usuario, duracion, (d1, d2) -> (d1 + d2) / 2)
            );

            result.put("docentes", revisores);
        } else if (rol == 2) {
            Map<String, Double> generarActa = promedioActividadUsuario.getOrDefault("generarActa", Map.of());
            Map<String, Double> notificarRechazo = promedioActividadUsuario.getOrDefault("notificarRechazo", Map.of());
            Map<String, Double> validacionTema = promedioActividadUsuario.getOrDefault("validacionTema", Map.of());
            Map<String, Double> direccionCombinada = new HashMap<>();

            Set<String> usuarios = new HashSet<>();
            usuarios.addAll(generarActa.keySet());
            usuarios.addAll(notificarRechazo.keySet());

            for (String usuario : usuarios) {
                Double d1 = generarActa.get(usuario);
                Double d2 = notificarRechazo.get(usuario);

                if (d1 != null && d2 != null) {
                    direccionCombinada.put(usuario, (d1 + d2) / 2);
                } else if (d1 != null) {
                    direccionCombinada.put(usuario, d1);
                } else if (d2 != null) {
                    direccionCombinada.put(usuario, d2);
                }
            }

            Map<String, Object> direccion = new HashMap<>();
            direccion.put("validacionTema", validacionTema);
            direccion.put("respuestaFinal", direccionCombinada);
            result.put("direccion", direccion);
        } else {
            return ResponseEntity.badRequest().body("Ese rol no existe");
        }

        return ResponseEntity.ok(result);
    }



    @GetMapping("/procesos/{nombreproceso}/revisiones-por-usuario")
    public ResponseEntity<Map<String, Object>> getTopRevisores(@PathVariable String nombreproceso,
                                                               @RequestParam(value ="carrera", required = false) String carrera,
                                                               @RequestParam(value ="periodo", required = false) String periodo,
                                                               @RequestParam(value = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                                               @RequestParam(value = "hasta",required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        HistoricProcessInstanceQuery query = historyService
                .createHistoricProcessInstanceQuery()
                .processDefinitionKey(nombreproceso)
                .finished();

        if (carrera != null) {
            query = query.variableValueEquals("carrera", carrera);

        }

        if (periodo != null) {
            query = query.variableValueEquals("periodo", periodo);
        }


        if((desde!=null) && (hasta!=null)){
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


        for (HistoricProcessInstance instance : instances) {

            List<HistoricActivityInstance> actividades = historyService
                    .createHistoricActivityInstanceQuery()
                    .processInstanceId(instance.getId())
                    .finished()
                    .list();




            for (HistoricActivityInstance act : actividades) {
                String activityId = act.getActivityId();

                if (activityId == null) continue;




                if (act.getActivityId().equals("revisarR1")) {
                    HistoricVariableInstance variable = historyService
                            .createHistoricVariableInstanceQuery()
                            .processInstanceId(instance.getId())
                            .variableName("nombreRevisor1")
                            .singleResult();

                    if (variable != null) {
                        String usuario = String.valueOf(variable.getValue());
                        revisionesPorUsuario.put(usuario,
                                revisionesPorUsuario.getOrDefault(usuario, 0) + 1);
                    }
                }else if (act.getActivityId().equals("revisarR2")){
                    HistoricVariableInstance variable = historyService
                            .createHistoricVariableInstanceQuery()
                            .processInstanceId(instance.getId())
                            .variableName("nombreRevisor2")
                            .singleResult();

                    if (variable != null) {
                        String usuario = String.valueOf(variable.getValue());
                        revisionesPorUsuario.put(usuario,
                                revisionesPorUsuario.getOrDefault(usuario, 0) + 1);
                    }
                }
            }
        }


        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalRevisionesPorUsuario", revisionesPorUsuario);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/procesos/{nombreproceso}/tutorias-por-usuario")
    public ResponseEntity<Map<String, Object>> getTopTutores(@PathVariable String nombreproceso,
                                                             @RequestParam(value ="carrera", required = false) String carrera,
                                                             @RequestParam(value ="periodo", required = false) String periodo,
                                                             @RequestParam(value = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                                             @RequestParam(value = "hasta",required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        HistoricProcessInstanceQuery query = historyService
                .createHistoricProcessInstanceQuery()
                .processDefinitionKey(nombreproceso)
                .finished();

        if (carrera!=null) {
            query = query.variableValueEquals("carrera", carrera);
        }

        if (periodo!=null) {
            query = query.variableValueEquals("periodo", periodo);
        }

        if((desde!=null) && (hasta!=null)){
            Instant desdeInstant = desde.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant hastaInstant = hasta.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant(); // incluir el día 'hasta'

            query = query.startedAfter(Date.from(desdeInstant))
                    .startedBefore(Date.from(hastaInstant));
        }

        List<HistoricProcessInstance> instances = query.list();

        if (instances.isEmpty()) {
            return ResponseEntity.ok(Map.of("mensaje", "No hay instancias finalizadas para ese proceso."));


        }

        Map<String, Integer> tutoriasPorUsuario = new HashMap<>();

        System.out.println("------"+instances);

        for (HistoricProcessInstance instance : instances) {
            List<HistoricActivityInstance> actividades = historyService
                    .createHistoricActivityInstanceQuery()
                    .processInstanceId(instance.getId())
                    .finished()
                    .list();

            for (HistoricActivityInstance act : actividades) {
                String activityId = act.getActivityId();


                if (activityId == null) continue;

                if (activityId.equals("generarActa")) {
                    HistoricVariableInstance variable = historyService
                            .createHistoricVariableInstanceQuery()
                            .processInstanceId(instance.getId())
                            .variableName("tutor")
                            .singleResult();

                    System.out.println("variable encontrada=..."+variable);

                    if (variable != null) {
                        String usuario = String.valueOf(variable.getValue());
                        tutoriasPorUsuario.put(usuario,
                                tutoriasPorUsuario.getOrDefault(usuario, 0) + 1);
                    }
                }
            }
        }


        Map<String, Object> response = new LinkedHashMap<>();
        response.put("turoeiasAprobadasPorUsuario", tutoriasPorUsuario);

        return ResponseEntity.ok(response);
    }




    @GetMapping("/procesos/{nombreproceso}/estado/rechazo/tutorias-por-usuario")
    public ResponseEntity<Map<String, Object>> getTopTutoresRechazados(@PathVariable String nombreproceso,
                                                               @RequestParam(value ="carrera", required = false) String carrera,
                                                               @RequestParam(value ="periodo", required = false) String periodo,
                                                               @RequestParam(value = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                                               @RequestParam(value = "hasta",required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        HistoricProcessInstanceQuery query = historyService
                .createHistoricProcessInstanceQuery()
                .processDefinitionKey(nombreproceso)
                .finished();

        if (carrera!=null) {
            query = query.variableValueEquals("carrera", carrera);
        }

        if (periodo!=null) {
            query = query.variableValueEquals("periodo", periodo);
        }

        if((desde!=null) && (hasta!=null)){
            Instant desdeInstant = desde.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant hastaInstant = hasta.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant(); // incluir el día 'hasta'

            query = query.startedAfter(Date.from(desdeInstant))
                    .startedBefore(Date.from(hastaInstant));
        }

        List<HistoricProcessInstance> instances = query.list();

        if (instances.isEmpty()) {
            return ResponseEntity.ok(Map.of("mensaje", "No hay instancias finalizadas para ese proceso."));


        }

        Map<String, Integer> tutoriasPorUsuario = new HashMap<>();

        System.out.println("------"+instances);

        for (HistoricProcessInstance instance : instances) {
            List<HistoricActivityInstance> actividades = historyService
                    .createHistoricActivityInstanceQuery()
                    .processInstanceId(instance.getId())
                    .finished()
                    .list();

            for (HistoricActivityInstance act : actividades) {
                String activityId = act.getActivityId();


                if (activityId == null) continue;

                if (activityId.equals("notificarRechazo")) {
                    HistoricVariableInstance variable = historyService
                            .createHistoricVariableInstanceQuery()
                            .processInstanceId(instance.getId())
                            .variableName("tutor")
                            .singleResult();

                    System.out.println("variable encontrada=..."+variable);

                    if (variable != null) {
                        String usuario = String.valueOf(variable.getValue());
                        tutoriasPorUsuario.put(usuario,
                                tutoriasPorUsuario.getOrDefault(usuario, 0) + 1);
                    }
                }
            }
        }
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("tutoriasRechazadasPorUsuario", tutoriasPorUsuario);

        return ResponseEntity.ok(response);
    }



    @GetMapping("/procesos/{nombreproceso}/estado/rechazo/vaidacion/tutorias-por-usuario")
    public ResponseEntity<Map<String, Object>> getTopTutoresRechazadosValidadcion(@PathVariable String nombreproceso,
                                                                                  @RequestParam(value ="carrera", required = false) String carrera,
                                                                                  @RequestParam(value ="periodo", required = false) String periodo,
                                                                                  @RequestParam(value = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                                                                  @RequestParam(value = "hasta",required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        HistoricProcessInstanceQuery query = historyService
                .createHistoricProcessInstanceQuery()
                .processDefinitionKey(nombreproceso)
                .finished();

        if (carrera != null) {
            query = query.variableValueEquals("carrera", carrera);
        }

        if (periodo != null) {
            query = query.variableValueEquals("periodo", periodo);
        }


        if ((desde != null) && (hasta != null)) {
            Instant desdeInstant = desde.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant hastaInstant = hasta.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

            query = query.startedAfter(Date.from(desdeInstant))
                    .startedBefore(Date.from(hastaInstant));
        }

        List<HistoricProcessInstance> instances = query.list();

        List<HistoricProcessInstance> instancesFiltradas = instances.stream()
                .filter(i -> "theEnd".equals(i.getEndActivityId()))
                .collect(Collectors.toList());

        if (instancesFiltradas.isEmpty()) {
            return ResponseEntity.ok(Map.of("mensaje", "No hay instancias finalizadas para ese proceso con endActivityId 'theEnd2'."));
        }

        Map<String, Integer> tutoriasPorUsuario = new HashMap<>();

        for (HistoricProcessInstance instance : instancesFiltradas) {
            // Obtienes variable "tutor" una sola vez por instancia
            HistoricVariableInstance variable = historyService
                    .createHistoricVariableInstanceQuery()
                    .processInstanceId(instance.getId())
                    .variableName("tutor")
                    .singleResult();

            if (variable != null) {
                String usuario = String.valueOf(variable.getValue());
                tutoriasPorUsuario.put(usuario,
                        tutoriasPorUsuario.getOrDefault(usuario, 0) + 1);
            }
            else {
                // Si no hay variable tutor, cuentas como "Sin Tutor"
                tutoriasPorUsuario.put("Sin Tutor",
                        tutoriasPorUsuario.getOrDefault("Sin Tutor", 0) + 1);
            }

        }

        return ResponseEntity.ok(Map.of("tutoriasNoValidadasPorUsuario", tutoriasPorUsuario));
    }

    @GetMapping("/procesos/{nombreproceso}/resumen")
    public ResponseEntity<Map<String, Object>> getClasificacionProcesos(@PathVariable String nombreproceso,
                                                                                  @RequestParam(value ="carrera", required = false) String carrera,
                                                                                  @RequestParam(value ="periodo", required = false) String periodo,
                                                                                  @RequestParam(value = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                                                                  @RequestParam(value = "hasta",required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        HistoricProcessInstanceQuery query = historyService
                .createHistoricProcessInstanceQuery()
                .processDefinitionKey(nombreproceso)
                .finished();

        ProcessInstanceQuery query2 =runtimeService.
                createProcessInstanceQuery()
                .processDefinitionKey(nombreproceso)
                .active();



        if (carrera != null) {
            query = query.variableValueEquals("carrera", carrera);
            query2 = query2.variableValueEquals("carrera", carrera);

        }

        if (periodo != null) {
            query = query.variableValueEquals("periodo", periodo);
            query2 = query2.variableValueEquals("periodo", periodo);
        }


        if ((desde != null) && (hasta != null)) {
            Instant desdeInstant = desde.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant hastaInstant = hasta.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

            query = query.startedAfter(Date.from(desdeInstant))
                    .startedBefore(Date.from(hastaInstant));

            query2 = query2.startedAfter(Date.from(desdeInstant))
                    .startedBefore(Date.from(hastaInstant));
        }

        List<HistoricProcessInstance> instances = query.list();

        long totalInstanciasNoValidadas = 0;
        long totalInstanciasActivas = 0;
        List<HistoricProcessInstance> instancesFiltradasNovalidadas = instances.stream()
                .filter(i -> "theEnd".equals(i.getEndActivityId()))
                .collect(Collectors.toList());

        totalInstanciasNoValidadas = instancesFiltradasNovalidadas.stream().count();

        List<ProcessInstance> instances2 = query2.list();

        totalInstanciasActivas = instances2.stream().count();
        long totalInstanciasAprobadas = 0;
        long totalInstanciasRechazadas = 0;
        long totalInstanciasFianlizadas = instances.stream().count();
        long totalInstancias = totalInstanciasFianlizadas + totalInstanciasActivas;

        for (HistoricProcessInstance instance : instances) {
            List<HistoricActivityInstance> actividades = historyService
                    .createHistoricActivityInstanceQuery()
                    .processInstanceId(instance.getId())
                    .finished()
                    .list();

            for (HistoricActivityInstance act : actividades) {
                String activityId = act.getActivityId();


                if (activityId == null) continue;

                if (activityId.equals("generarActa")) {

                    totalInstanciasAprobadas++;
                }

                if (activityId.equals("notificarRechazo")) {

                    totalInstanciasRechazadas++;
                }
            }
        }
        Map<String, Object> valores = new LinkedHashMap<>();
        Map<String, Object> response = new LinkedHashMap<>();

        valores.put("No validadas",totalInstanciasNoValidadas);
        valores.put("Rechazadas",totalInstanciasRechazadas);
        valores.put("Aprobadas",totalInstanciasAprobadas);
        valores.put("Activas",totalInstanciasActivas);
        response.put("Total Procesos:", totalInstancias);
        response.put("Instancias Activas:", totalInstanciasActivas);
        response.put("Instancias Finalizadas:", totalInstanciasFianlizadas);
        response.put("Categoria", valores);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/procesos/{nombreproceso}/tutorias-clasificadas")
    public ResponseEntity<Map<String, Map<String, Integer>>> getTutoriasClasificadasPorUsuario(
            @PathVariable String nombreproceso,
            @RequestParam(value = "carrera", required = false) String carrera,
            @RequestParam(value = "periodo", required = false) String periodo,
            @RequestParam(value = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(value = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        HistoricProcessInstanceQuery query = historyService
                .createHistoricProcessInstanceQuery()
                .processDefinitionKey(nombreproceso)
                .finished();

        if (carrera != null) {
            query = query.variableValueEquals("carrera", carrera);
        }

        if (periodo != null) {
            query = query.variableValueEquals("periodo", periodo);
        }

        if (desde != null && hasta != null) {
            Instant desdeInstant = desde.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant hastaInstant = hasta.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
            query = query.startedAfter(Date.from(desdeInstant)).startedBefore(Date.from(hastaInstant));
        }

        List<HistoricProcessInstance> instances = query.list();

        Map<String, Map<String, Integer>> resultado = new LinkedHashMap<>();

        for (HistoricProcessInstance instance : instances) {
            boolean fueAprobada = false;
            boolean fueRechazada = false;
            boolean fueNoValidada = "theEnd".equals(instance.getEndActivityId());

            List<HistoricActivityInstance> actividades = historyService
                    .createHistoricActivityInstanceQuery()
                    .processInstanceId(instance.getId())
                    .finished()
                    .list();

            for (HistoricActivityInstance act : actividades) {
                String activityId = act.getActivityId();
                if (activityId == null) continue;

                if ("generarActa".equals(activityId)) {
                    fueAprobada = true;
                }

                if ("notificarRechazo".equals(activityId)) {
                    fueRechazada = true;
                }
            }


            HistoricVariableInstance variable = historyService
                    .createHistoricVariableInstanceQuery()
                    .processInstanceId(instance.getId())
                    .variableName("tutor")
                    .singleResult();

            String tutor = (variable != null) ? String.valueOf(variable.getValue()) : "Sin Tutor";

            resultado.putIfAbsent(tutor, new LinkedHashMap<>());
            Map<String, Integer> conteos = resultado.get(tutor);

            if (fueAprobada) {
                conteos.put("Aprobadas", conteos.getOrDefault("Aprobadas", 0) + 1);
            }
            if (fueRechazada) {
                conteos.put("Rechazadas", conteos.getOrDefault("Rechazadas", 0) + 1);
            }
            if (fueNoValidada) {
                conteos.put("No validadas", conteos.getOrDefault("No validadas", 0) + 1);
            }
        }

        return ResponseEntity.ok(resultado);
    }



}
