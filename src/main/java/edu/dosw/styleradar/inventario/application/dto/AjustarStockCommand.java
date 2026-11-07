package edu.dosw.styleradar.inventario.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

/**
 * Comando para fijar el stock físico de una variante ya registrada.
 */
@Schema(description = "Datos para ajustar manualmente las unidades físicas de una talla")
public record AjustarStockCommand(
        @Schema(description = "Identificador del stock item; se toma del path cuando aplica",
                example = "018e3a2b-3333-7000-8000-000000000003")
        UUID stockItemId,

        @NotBlank(message = "La talla es obligatoria")
        @Schema(description = "Talla a ajustar", example = "M")
        String talla,

        @NotNull(message = "La nueva cantidad es obligatoria")
        @PositiveOrZero(message = "La nueva cantidad no puede ser negativa")
        @Schema(description = "Nuevas unidades disponibles", example = "7")
        Integer nuevaCantidad) {
}
