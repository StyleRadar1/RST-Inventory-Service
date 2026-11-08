package edu.dosw.styleradar.inventario.application.dto;

import edu.dosw.styleradar.inventario.domain.model.EstadoItem;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Stock de una talla concreta en una sede.
 */
@Schema(description = "Existencias de una talla en una sede")
public record VarianteStockDTO(
        @Schema(description = "Talla", example = "M")
        String talla,
        @Schema(description = "Unidades disponibles", example = "8")
        int stockDisponible,
        @Schema(description = "Unidades reservadas", example = "2")
        int stockReservado,
        @Schema(description = "Estado calculado de la variante", example = "DISPONIBLE")
        EstadoItem estadoVariante) {
}
