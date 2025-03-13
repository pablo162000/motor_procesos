package com.tesis.motor_procesos.config;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.flowable.engine.ProcessEngine;


@Configuration
public class FlowableConfig {

    @Bean
    public ProcessEngine processEngine() {
        ProcessEngineConfiguration cfg = new StandaloneProcessEngineConfiguration()
                .setJdbcUrl("jdbc:postgresql://localhost:5432/flowable_db") // Cambia el nombre de la BD
                .setJdbcUsername("postgres")
                .setJdbcPassword("postgres")
                .setJdbcDriver("org.postgresql.Driver")
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);

        return cfg.buildProcessEngine();
    }
}
