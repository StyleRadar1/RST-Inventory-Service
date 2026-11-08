package edu.dosw.styleradar.inventario.application.dto;

import edu.dosw.styleradar.inventario.domain.model.TipoPrenda;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;
import java.util.UUID;

/**
 * Comando de entrada para registrar existencias de una talla en una sede.
 */
@Schema(description = "Datos necesarios para registrar existencias iniciales de una talla")
public record RegistrarStockCommand(
        @NotNull(message = "El prendaId es obligatorio")
        @Schema(description = "Identificador UUID de la prenda", example = "018e3a2b-1111-7000-8000-000000000001")
        UUID prendaId,

        @NotNull(message = "El sedeId es obligatorio")
        @Schema(description = "Identificador UUID de la sede", example = "018e3a2b-2222-7000-8000-000000000002")
        UUID sedeId,

        @NotNull(message = "El tipo de prenda es obligatorio")
        @Schema(description = "Tipo de prenda que determina las tallas válidas", example = "ROPA")
        TipoPrenda tipoPrenda,

        @NotBlank(message = "La talla es obligatoria")
        @Schema(description = "Talla de la variante", example = "M")
        String talla,

        @NotNull(message = "La cantidad es obligatoria")
        @PositiveOrZero(message = "La cantidad no puede ser negativa")
        @Schema(description = "Unidades disponibles iniciales", example = "10")
        Integer cantidad,

        @NotNull(message = "La fecha de publicación es obligatoria")
        @Schema(description = "Fecha y hora de publicación de la prenda", example = "2026-11-01T10:00:00Z")
        Instant fechaPublicacion) {
}
