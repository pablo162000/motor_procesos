package com.tesis.motor_procesos.config;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyConfiguration {

    @Bean
    public TaskExecutor processTaskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }
}
