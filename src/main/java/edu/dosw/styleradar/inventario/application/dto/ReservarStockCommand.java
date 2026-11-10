package edu.dosw.styleradar.inventario.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

/**
 * Comando para bloquear temporalmente unidades durante el checkout.
 */
@Schema(description = "Datos para reservar unidades de una variante antes de confirmar el pago")
public record ReservarStockCommand(
        @NotNull(message = "El prendaId es obligatorio")
        @Schema(description = "Identificador UUID de la prenda", example = "018e3a2b-1111-7000-8000-000000000001")
        UUID prendaId,

        @NotNull(message = "El sedeId es obligatorio")
        @Schema(description = "Identificador UUID de la sede", example = "018e3a2b-2222-7000-8000-000000000002")
        UUID sedeId,

        @NotBlank(message = "La talla es obligatoria")
        @Schema(description = "Talla a reservar", example = "M")
        String talla,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad a reservar debe ser mayor que cero")
        @Schema(description = "Unidades a reservar", example = "2")
        Integer cantidad,

        @NotNull(message = "El transaccionId es obligatorio")
        @Schema(description = "Identificador de la transacción de checkout",
                example = "018e3a2b-5555-7000-8000-000000000005")
        UUID transaccionId) {
}
