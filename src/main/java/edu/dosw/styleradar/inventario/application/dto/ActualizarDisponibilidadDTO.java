package edu.dosw.styleradar.inventario.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "Payload para actualizar de forma independiente las unidades disponibles de una talla")
public record ActualizarDisponibilidadDTO(
        @NotNull(message = "Las unidades disponibles son obligatorias")
        @PositiveOrZero(message = "Las unidades disponibles no pueden ser negativas")
        @Schema(description = "Nuevas unidades físicas disponibles", example = "15")
        Integer unidades
) {}
