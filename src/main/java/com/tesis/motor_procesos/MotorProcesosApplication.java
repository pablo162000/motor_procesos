package com.tesis.motor_procesos;

import com.tesis.motor_procesos.service.MyService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(proxyBeanMethods = false)
public class MotorProcesosApplication {

	public static void main(String[] args) {
		SpringApplication.run(MotorProcesosApplication.class, args);
	}

	@Bean
	public CommandLineRunner init(final MyService myService) {
		return new CommandLineRunner() {
			@Override
			public void run(String... args) throws Exception {
			}
		};
	}

}
