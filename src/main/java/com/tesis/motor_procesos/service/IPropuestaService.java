package com.tesis.motor_procesos.service;

import com.tesis.motor_procesos.repository.modelo.PropuestaRequest;
import com.tesis.motor_procesos.repository.modelo.ValidarTemaRequest;

public interface IPropuestaService {

    public void iniciarProcesoConPropuesta(PropuestaRequest request);
    public void validarTema(ValidarTemaRequest request);

}
