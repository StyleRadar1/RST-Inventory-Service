package edu.dosw.styleradar.inventario.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

/**
 * Stock de una prenda en una sede específica.
 */
@Schema(description = "Disponibilidad de una prenda en una sede")
public record SedeStockDTO(
        @Schema(description = "Identificador de la sede", example = "018e3a2b-2222-7000-8000-000000000002")
        UUID sedeId,
        @Schema(description = "Indica si el inventario de la sede está activo", example = "true")
        boolean activo,
        @Schema(description = "Variantes de talla registradas")
        List<VarianteStockDTO> variantes) {
}
