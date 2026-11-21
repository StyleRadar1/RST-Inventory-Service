package edu.dosw.styleradar.inventario.infrastructure.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "edu.dosw.styleradar.inventario.infrastructure.adapter.out.external")
public class FeignConfig {
}
