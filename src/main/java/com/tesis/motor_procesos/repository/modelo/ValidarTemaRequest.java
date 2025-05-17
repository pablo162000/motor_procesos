package com.tesis.motor_procesos.repository.modelo;

import lombok.Data;

@Data
public class ValidarTemaRequest {

    private String idDireccion; // El director que hace la validación
    private String procesoId;
    private Boolean idTemaValidado; // true o false según decisión
}
