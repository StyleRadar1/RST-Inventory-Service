package edu.dosw.styleradar.inventario.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Resultado de una reserva temporal de stock.
 */
@Schema(description = "Resultado del bloqueo temporal de unidades")
public record ReservaResponseDTO(
        @Schema(description = "Indica si la reserva fue exitosa", example = "true")
        boolean reservaExitosa,
        @Schema(description = "Identificador de la prenda", example = "018e3a2b-1111-7000-8000-000000000001")
        UUID prendaId,
        @Schema(description = "Identificador de la sede", example = "018e3a2b-2222-7000-8000-000000000002")
        UUID sedeId,
        @Schema(description = "Talla reservada", example = "M")
        String talla,
        @Schema(description = "Unidades reservadas", example = "2")
        int unidadesReservadas,
        @Schema(description = "Stock disponible restante", example = "8")
        int stockDisponibleRestante,
        @Schema(description = "Fecha de expiración de la reserva", example = "2026-11-04T15:15:00Z")
        Instant expiracionReserva) {
}
