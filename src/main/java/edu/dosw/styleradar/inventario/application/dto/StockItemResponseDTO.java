package edu.dosw.styleradar.inventario.application.dto;

import edu.dosw.styleradar.inventario.domain.model.EstadoItem;
import edu.dosw.styleradar.inventario.domain.model.TipoPrenda;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Vista de una variante de stock con su estado calculado.
 */
@Schema(description = "Stock disponible y reservado de una variante")
public record StockItemResponseDTO(
        @Schema(description = "Identificador del stock item", example = "018e3a2b-3333-7000-8000-000000000003")
        UUID id,
        @Schema(description = "Identificador de la prenda", example = "018e3a2b-1111-7000-8000-000000000001")
        UUID prendaId,
        @Schema(description = "Identificador de la sede", example = "018e3a2b-2222-7000-8000-000000000002")
        UUID sedeId,
        @Schema(description = "Tipo de prenda", example = "ROPA")
        TipoPrenda tipoPrenda,
        @Schema(description = "Talla consultada", example = "M")
        String talla,
        @Schema(description = "Unidades disponibles", example = "10")
        int stockDisponible,
        @Schema(description = "Unidades reservadas", example = "2")
        int stockReservado,
        @Schema(description = "Estado calculado del item", example = "NUEVA_PRENDA")
        EstadoItem estado,
        @Schema(description = "Indica si el inventario está activo", example = "true")
        boolean activo,
        @Schema(description = "Última fecha de actualización", example = "2026-11-04T15:00:00Z")
        Instant fechaActualizacion) {
}
