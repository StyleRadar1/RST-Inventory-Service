package edu.dosw.styleradar.inventario.application.dto;

import edu.dosw.styleradar.inventario.domain.model.EstadoItem;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Resultado de la verificación de disponibilidad para venta (SR-RF-57)")
public record VerificacionDisponibilidadDTO(
        @Schema(description = "Identificador de la prenda", example = "018e3a2b-1111-7000-8000-000000000001")
        UUID prendaId,

        @Schema(description = "Indica si la prenda tiene inventario para ser mostrada en resultados", example = "true")
        boolean disponibleParaVenta,

        @Schema(description = "Estado de disponibilidad calculado", example = "DISPONIBLE")
        EstadoItem estado,

        @Schema(description = "Unidades físicas disponibles consolidadas", example = "10")
        int stockDisponibleTotal
) {}
