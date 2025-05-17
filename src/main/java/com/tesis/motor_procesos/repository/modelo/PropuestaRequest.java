package com.tesis.motor_procesos.repository.modelo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PropuestaRequest {

    private String idEstudiante;
    private Long idPropuesta;
    private String tipo;
    private String carrera;
    private String categoria;
    private String tema;
    private Long idEstudianteUno;
    private Long idEstudianteDos;
    private Long idEstudianteTres;
    private Long idTutor;
    private String urlArchivo;
    private LocalDateTime fecha;

}
