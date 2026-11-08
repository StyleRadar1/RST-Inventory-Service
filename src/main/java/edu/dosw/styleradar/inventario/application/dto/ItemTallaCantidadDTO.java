package edu.dosw.styleradar.inventario.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "Asociación de talla y existencias iniciales")
public record ItemTallaCantidadDTO(
        @NotBlank(message = "La talla es obligatoria")
        @Schema(description = "Talla tipificada (ej. 'M' para ropa o '40' para calzado)", example = "M")
        String talla,

        @NotNull(message = "La cantidad inicial es obligatoria")
        @PositiveOrZero(message = "La cantidad inicial no puede ser negativa")
        @Schema(description = "Unidades físicas iniciales disponibles", example = "10")
        Integer cantidad
) {}
