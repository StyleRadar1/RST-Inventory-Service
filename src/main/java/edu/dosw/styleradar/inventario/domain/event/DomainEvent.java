package edu.dosw.styleradar.inventario.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Contrato de los eventos de dominio emitidos por el agregado StockItem.
 */
public interface DomainEvent {

    UUID getEventId();

    String getEventType();

    Instant getOcurridoEn();
}
