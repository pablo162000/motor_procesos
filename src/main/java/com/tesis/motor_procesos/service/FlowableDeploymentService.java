package com.tesis.motor_procesos.service;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.stereotype.Service;


@Service
public class FlowableDeploymentService {


    private final RepositoryService repositoryService;

    public FlowableDeploymentService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    public void deployProcess() {
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("processes/holiday-request.bpmn20.xml") // Ubicación en `src/main/resources/processes/`
                .deploy();

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();

        System.out.println("Found process definition: " + processDefinition.getName());
    }

}
