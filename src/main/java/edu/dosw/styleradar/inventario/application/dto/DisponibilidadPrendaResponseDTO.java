package edu.dosw.styleradar.inventario.application.dto;

import edu.dosw.styleradar.inventario.domain.model.EstadoItem;
import edu.dosw.styleradar.inventario.domain.model.TipoPrenda;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

/**
 * Disponibilidad consolidada de una prenda en todas sus sedes.
 */
@Schema(description = "Disponibilidad consolidada de una prenda por sede y talla")
public record DisponibilidadPrendaResponseDTO(
        @Schema(description = "Identificador de la prenda", example = "018e3a2b-1111-7000-8000-000000000001")
        UUID prendaId,
        @Schema(description = "Tipo de prenda", example = "ROPA")
        TipoPrenda tipoPrenda,
        @Schema(description = "Estado general consolidado", example = "DISPONIBLE")
        EstadoItem estadoGeneral,
        @Schema(description = "Total de unidades disponibles en sedes activas", example = "18")
        int stockDisponibleTotal,
        @Schema(description = "Detalle por sede")
        List<SedeStockDTO> sedes) {
}
