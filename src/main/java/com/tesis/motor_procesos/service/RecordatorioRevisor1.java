package com.tesis.motor_procesos.service;

import com.tesis.motor_procesos.client.CorreoRestClient;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("recordatorioRevisor1")
public class RecordatorioRevisor1 implements JavaDelegate {

    @Autowired
    private CorreoRestClient correoRestClient; // Inyec

    @Override
    public void execute(DelegateExecution execution) {

        String emailRevisor1 = (String) execution.getVariable("correoRevisor1");
        String nombreRevisor1 = (String) execution.getVariable("nombreRevisor1");
        String temaPropuesta = (String) execution.getVariable("temaPropuesta");
        String correoDireccion = (String) execution.getVariable("c");

        correoRestClient.notificacionRecordatorioRevisor(emailRevisor1, nombreRevisor1, temaPropuesta, correoDireccion);
    }
}
