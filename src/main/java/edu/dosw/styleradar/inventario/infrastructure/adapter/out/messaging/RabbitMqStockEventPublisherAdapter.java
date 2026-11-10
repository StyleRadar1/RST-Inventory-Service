package edu.dosw.styleradar.inventario.infrastructure.adapter.out.messaging;

import edu.dosw.styleradar.inventario.domain.event.DomainEvent;
import edu.dosw.styleradar.inventario.domain.event.StockAgotadoEvent;
import edu.dosw.styleradar.inventario.domain.event.StockBajoEvent;
import edu.dosw.styleradar.inventario.domain.port.out.StockEventPublisherPort;
import edu.dosw.styleradar.inventario.infrastructure.config.RabbitMqConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Publicador de eventos de dominio hacia RabbitMQ.
 */
@Component
public class RabbitMqStockEventPublisherAdapter implements StockEventPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMqStockEventPublisherAdapter(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publicar(DomainEvent evento) {
        if (evento == null) {
            return;
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventId", evento.getEventId());
        payload.put("eventType", evento.getEventType());
        payload.put("timestamp", evento.getOcurridoEn());
        payload.put("producer", "ms-inventario");

        Map<String, Object> data = new LinkedHashMap<>();
        if (evento instanceof StockAgotadoEvent e) {
            data.put("prendaId", e.prendaId());
            data.put("sedeId", e.sedeId());
            data.put("talla", e.talla());
            data.put("stockRestante", e.stockRestante());
            data.put("estado", "AGOTADA");
        } else if (evento instanceof StockBajoEvent e) {
            data.put("prendaId", e.prendaId());
            data.put("sedeId", e.sedeId());
            data.put("talla", e.talla());
            data.put("stockRestante", e.stockRestante());
            data.put("estado", "ULTIMAS_UNIDADES");
        }

        payload.put("data", data);
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_EVENTS, evento.getEventType(), payload);
    }
}
