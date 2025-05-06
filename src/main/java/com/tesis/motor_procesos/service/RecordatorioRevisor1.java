package com.tesis.motor_procesos.service;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("recordatorioRevisor1")
public class RecordatorioRevisor1 implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {

        String idRevisor1 = (String) execution.getVariable("idRevisor1");


    }
}
