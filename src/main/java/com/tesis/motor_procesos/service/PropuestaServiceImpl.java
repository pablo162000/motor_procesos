package com.tesis.motor_procesos.service;

import com.tesis.motor_procesos.repository.modelo.PropuestaRequest;
import com.tesis.motor_procesos.repository.modelo.ValidarTemaRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class PropuestaServiceImpl implements IPropuestaService{
    private static final Logger logger = LogManager.getLogger(PropuestaServiceImpl.class);

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Override
    public void iniciarProcesoConPropuesta(PropuestaRequest request) {
/*
        // 1. Iniciar proceso con variables iniciales
        Map<String, Object> variables = new HashMap<>();
        //variables.put("idEstudiante", request.getIdEstudiante());

        ProcessInstance instance = runtimeService.startProcessInstanceByKey("t_inicial", variables);

        // 2. Obtener tarea activa (Enviar Propuesta)
        Task task = taskService.createTaskQuery()
                .processInstanceId(instance.getId())
                //.taskAssignee(request.getIdEstudiante())
                .singleResult();

        if (task == null) {
            throw new IllegalStateException("No se encontró tarea para el estudiante.");
        }

        // 3. Completar tarea con datos del formulario
        Map<String, Object> formVars = new HashMap<>();
        formVars.put("idPropuesta", request.getIdPropuesta());
        formVars.put("idTipo", request.getTipo());
        formVars.put("idCarrera", request.getCarrera());
        formVars.put("idCategoria", request.getCategoria());
        formVars.put("idTema", request.getTema());
        formVars.put("idEstudianteUno", request.getIdEstudianteUno());
        formVars.put("idEstudianteDos", request.getIdEstudianteDos());
        formVars.put("idEstudianteTres", request.getIdEstudianteTres());
        formVars.put("idTutor", request.getIdTutor());
        formVars.put("idUrlArchivo", request.getUrlArchivo());
        formVars.put("idFecha", request.getFecha());

        taskService.complete(task.getId(), formVars);

 */
    }

    @Override
    public void validarTema(ValidarTemaRequest request) {
        /*
        // Buscar la tarea activa del proceso asignada al director
        Task tarea = taskService.createTaskQuery()
                .processInstanceId(request.getProcesoId())
                .taskAssignee(request.getIdDireccion())
                .taskName("Validar Tema")
                .singleResult();

        if (tarea == null) {
            throw new IllegalStateException("No se encontró la tarea 'Validar Tema' para este usuario.");
        }

        // Completar la tarea con la decisión
        Map<String, Object> variables = new HashMap<>();
        variables.put("varTemaValidado", request.getIdTemaValidado());

        taskService.complete(tarea.getId(), variables);

         */
    }

}
