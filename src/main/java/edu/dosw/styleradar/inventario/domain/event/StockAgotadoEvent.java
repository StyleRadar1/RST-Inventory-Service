package edu.dosw.styleradar.inventario.domain.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento disparado cuando una variante llega a cero unidades disponibles.
 */
public record StockAgotadoEvent(
        UUID eventId,
        UUID prendaId,
        UUID sedeId,
        String talla,
        int stockRestante,
        Instant ocurridoEn) implements DomainEvent {

    public static final String EVENT_TYPE = "inventario.stock.agotado";

    public StockAgotadoEvent {
        Objects.requireNonNull(eventId, "eventId es obligatorio");
        Objects.requireNonNull(prendaId, "prendaId es obligatorio");
        Objects.requireNonNull(sedeId, "sedeId es obligatorio");
        Objects.requireNonNull(talla, "talla es obligatoria");
        Objects.requireNonNull(ocurridoEn, "ocurridoEn es obligatorio");
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }

    @Override
    public Instant getOcurridoEn() {
        return ocurridoEn;
    }
}
