package com.tesis.motor_procesos.service;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("recordatorioRevisor2")
public class RecordatorioRevisor2 implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) {
        String idRevisor2 = (String) execution.getVariable("idRevisor2");

        // Lógica similar
        System.out.println("Recordando al revisor con ID: " + idRevisor2);
    }
}
