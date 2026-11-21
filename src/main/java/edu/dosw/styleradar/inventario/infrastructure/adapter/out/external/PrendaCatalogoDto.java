package edu.dosw.styleradar.inventario.infrastructure.adapter.out.external;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PrendaCatalogoDto(
        UUID id,
        @JsonAlias("sedeId") UUID almacenId) {
}
