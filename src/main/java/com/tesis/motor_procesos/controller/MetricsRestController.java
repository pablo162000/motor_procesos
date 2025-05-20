package com.tesis.motor_procesos.controller;

import com.tesis.motor_procesos.service.MetricsService;
import com.tesis.motor_procesos.service.ProcessMetrics;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.flowable.engine.HistoryService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.util.*;

@RestController
@RequestMapping("/metrics")
public class MetricsRestController {


    @Autowired
    private HistoryService historyService;

    private final MetricsService metricsService;

    public MetricsRestController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }


    @GetMapping("/{processInstanceId}")
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

        // Actividades históricas
        List<org.flowable.engine.history.HistoricActivityInstance> actividades =
                historyService.createHistoricActivityInstanceQuery()
                        .processInstanceId(processInstanceId)
                        .orderByHistoricActivityInstanceStartTime()
                        .asc()
                        .list();

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


    @GetMapping("/procesos/{processDefinitionKey}")
    public ResponseEntity<?> getMetrics(@PathVariable String processDefinitionKey) {

        List<HistoricProcessInstance> instances = historyService.createHistoricProcessInstanceQuery()
                .processDefinitionKey(processDefinitionKey)
                .finished()
                .list();

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

            List<HistoricActivityInstance> activities = historyService
                    .createHistoricActivityInstanceQuery()
                    .processInstanceId(instance.getId())
                    .finished()
                    .list();

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

        Map<String, Double> averageUserDurations = new HashMap<>();
        for (String user : userDurations.keySet()) {
            double avg = userDurations.get(user) / (double) userCounts.get(user);
            averageUserDurations.put(user, avg);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalInstances", instances.size());
        result.put("averageProcessDurationMs", averageProcessDuration);
        result.put("averageActivityDurationsMs", averageActivityDurations);
        result.put("averageUserDurationsMs", averageUserDurations);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/procesos/{processDefinitionKey}/revisiones-por-usuario")
    public ResponseEntity<Map<String, Object>> getTopRevisores(@PathVariable String processDefinitionKey) {

        List<HistoricProcessInstance> instances = historyService.createHistoricProcessInstanceQuery()
                .processDefinitionKey(processDefinitionKey)
                .finished()
                .list();

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



    @GetMapping("/procesos/{processDefinitionKey}/export")
    public ResponseEntity<byte[]> exportMetricsToExcel(@PathVariable String processDefinitionKey) throws Exception {
        ProcessMetrics metrics = metricsService.getProcessMetrics(processDefinitionKey);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Métricas");

        int rowNum = 0;
        Row row;
        Cell cell;

        // Header general
        row = sheet.createRow(rowNum++);
        cell = row.createCell(0);
        cell.setCellValue("Total Instancias");
        cell = row.createCell(1);
        cell.setCellValue(metrics.getTotalInstances());

        row = sheet.createRow(rowNum++);
        cell = row.createCell(0);
        cell.setCellValue("Duración Promedio Proceso (ms)");
        cell = row.createCell(1);
        cell.setCellValue(metrics.getAverageProcessDurationMs());

        // Espacio
        rowNum++;

        // Duraciones por actividad
        row = sheet.createRow(rowNum++);
        cell = row.createCell(0);
        cell.setCellValue("Actividad");
        cell = row.createCell(1);
        cell.setCellValue("Duración Promedio (ms)");

        for (Map.Entry<String, Double> entry : metrics.getAverageActivityDurationsMs().entrySet()) {
            row = sheet.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue(entry.getKey());
            cell = row.createCell(1);
            cell.setCellValue(entry.getValue());
        }

        // Espacio
        rowNum++;

        // Duraciones por usuario
        row = sheet.createRow(rowNum++);
        cell = row.createCell(0);
        cell.setCellValue("Usuario ID");
        cell = row.createCell(1);
        cell.setCellValue("Duración Promedio (ms)");

        for (Map.Entry<String, Double> entry : metrics.getAverageUserDurationsMs().entrySet()) {
            row = sheet.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue(entry.getKey());
            cell = row.createCell(1);
            cell.setCellValue(entry.getValue());
        }

        // Auto-ajustar columnas
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        byte[] bytes = out.toByteArray();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=metrics_" + processDefinitionKey + ".xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }


}
