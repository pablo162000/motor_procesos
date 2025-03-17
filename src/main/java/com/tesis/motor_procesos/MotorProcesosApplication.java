package com.tesis.motor_procesos;

import com.tesis.motor_procesos.service.FlowableDeploymentService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MotorProcesosApplication {

	public static void main(String[] args) {

		SpringApplication.run(MotorProcesosApplication.class, args);
	}

	@Bean
	CommandLineRunner init(FlowableDeploymentService deploymentService) {
		return args -> {
			System.out.println("📢 Deploying BPMN Process...");
			deploymentService.deployProcess(); // 🚀 Llamada al método al iniciar
			System.out.println("✅ Process deployed!");
		};
	}

}
