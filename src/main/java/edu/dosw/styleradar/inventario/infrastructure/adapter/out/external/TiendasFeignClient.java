package edu.dosw.styleradar.inventario.infrastructure.adapter.out.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "ms-tiendas", url = "${clients.tiendas.url}")
public interface TiendasFeignClient {

    @GetMapping("/users/{userId}/store")
    AlmacenDelUsuarioDto almacenDelUsuario(@PathVariable("userId") Long userId,
                                           @RequestHeader("X-User-Id") Long solicitante,
                                           @RequestHeader("X-User-Roles") String roles);
}
