package edu.dosw.styleradar.inventario.application.dto;

import edu.dosw.styleradar.inventario.domain.model.TipoPrenda;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Comando para asociar un conjunto de tallas disponibles a una prenda en una sede (SR-RF-52)")
public record RegistrarLoteStockCommand(
        @NotNull(message = "El prendaId es obligatorio")
        @Schema(description = "Identificador UUID de la prenda en catálogo", example = "018e3a2b-1111-7000-8000-000000000001")
        UUID prendaId,

        @NotNull(message = "El sedeId es obligatorio")
        @Schema(description = "Identificador UUID de la sede comercial", example = "018e3a2b-2222-7000-8000-000000000002")
        UUID sedeId,

        @NotNull(message = "El tipo de prenda es obligatorio")
        @Schema(description = "Tipo de prenda (determina si las tallas son textiles o calzado)", example = "ROPA")
        TipoPrenda tipoPrenda,

        @NotEmpty(message = "Debe registrar al menos una talla")
        @Schema(description = "Lista de tallas con sus existencias iniciales")
        List<@Valid ItemTallaCantidadDTO> tallas,

        @NotNull(message = "La fecha de publicación es obligatoria")
        @Schema(description = "Fecha de alta en catálogo para cálculo de novedad (SR-RF-60)", example = "2026-11-01T10:00:00Z")
        Instant fechaPublicacion
) {}
