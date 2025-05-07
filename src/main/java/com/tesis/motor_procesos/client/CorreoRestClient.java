package com.tesis.motor_procesos.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "correoRestClient", url = "http://localhost:8282/API/tesis/")
public interface CorreoRestClient {
    @PostMapping("correo/notificacionrecordatoriov2")
    String notificacionRecordatorioRevisor( @RequestParam String toEmail,
                                            @RequestParam String revisor,
                                            @RequestParam String tema,
                                            @RequestParam String correoDireccion);
}
