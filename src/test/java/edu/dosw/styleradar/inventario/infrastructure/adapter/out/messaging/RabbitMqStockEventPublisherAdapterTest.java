package edu.dosw.styleradar.inventario.infrastructure.adapter.out.messaging;

import edu.dosw.styleradar.inventario.domain.event.StockAgotadoEvent;
import edu.dosw.styleradar.inventario.domain.event.StockBajoEvent;
import edu.dosw.styleradar.inventario.infrastructure.config.RabbitMqConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitMqStockEventPublisherAdapterTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RabbitMqStockEventPublisherAdapter publisher;

    @Test
    @DisplayName("Publica evento StockAgotadoEvent con routing key inventario.stock.agotado")
    void publicaStockAgotado() {
        StockAgotadoEvent evento = new StockAgotadoEvent(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "M", 0, Instant.now());

        publisher.publicar(evento);

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMqConfig.EXCHANGE_EVENTS),
                eq("inventario.stock.agotado"),
                captor.capture());

        Map<String, Object> payload = captor.getValue();
        assertThat(payload).containsEntry("producer", "ms-inventario");
        assertThat(payload).containsEntry("eventType", "inventario.stock.agotado");
    }

    @Test
    @DisplayName("Publica evento StockBajoEvent con routing key inventario.stock.ultimas-unidades")
    void publicaStockBajo() {
        StockBajoEvent evento = new StockBajoEvent(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "38", 2, Instant.now());

        publisher.publicar(evento);

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMqConfig.EXCHANGE_EVENTS),
                eq("inventario.stock.ultimas-unidades"),
                captor.capture());

        Map<String, Object> payload = captor.getValue();
        assertThat(payload).containsEntry("eventType", "inventario.stock.ultimas-unidades");
    }
}
