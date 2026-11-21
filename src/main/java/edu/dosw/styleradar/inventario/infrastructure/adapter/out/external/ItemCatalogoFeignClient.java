package edu.dosw.styleradar.inventario.infrastructure.adapter.out.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "item-catalogo", url = "${clients.item-catalogo.url}")
public interface ItemCatalogoFeignClient {

    @GetMapping("/api/v1/prendas/{id}")
    PrendaCatalogoDto obtenerPrenda(@PathVariable("id") UUID id);
}
