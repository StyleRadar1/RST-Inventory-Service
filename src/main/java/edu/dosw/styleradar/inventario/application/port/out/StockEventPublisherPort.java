package edu.dosw.styleradar.inventario.application.port.out;

import edu.dosw.styleradar.inventario.domain.event.DomainEvent;

public interface StockEventPublisherPort {

    void publicar(DomainEvent evento);
}
