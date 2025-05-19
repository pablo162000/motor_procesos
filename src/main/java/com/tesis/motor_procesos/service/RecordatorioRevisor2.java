package com.tesis.motor_procesos.service;
import com.tesis.motor_procesos.client.CorreoRestClient;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("recordatorioRevisor2")
public class RecordatorioRevisor2 implements JavaDelegate {

    @Autowired
    private CorreoRestClient correoRestClient; // In

    @Override
    public void execute(DelegateExecution execution) {
        String emailRevisor2 = (String) execution.getVariable("correoRevisor2");
        String nombreRevisor2 = (String) execution.getVariable("nombreRevisor2");
        String temaPropuesta = (String) execution.getVariable("temaPropuesta");
        String correoDireccion = (String) execution.getVariable("c");
        correoRestClient.notificacionRecordatorioRevisor(emailRevisor2, nombreRevisor2, temaPropuesta, correoDireccion);


    }
}
