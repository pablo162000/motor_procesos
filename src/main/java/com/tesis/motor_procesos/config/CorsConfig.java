package com.tesis.motor_procesos.config;

import org.springframework.beans.factory.annotation.Value;
import  org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Value("${corsorigin.url}")
    private String corsOrigin;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")  // Permite todas las rutas
                        .allowedOrigins("http://localhost:4200",corsOrigin)  // Permite solicitudes desde el frontend
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // Métodos permitidos
                        .allowedHeaders("*")  // Permite todos los headers
                        .allowCredentials(true);  // Permite el uso de credenciales
            }
        };
    }
}
