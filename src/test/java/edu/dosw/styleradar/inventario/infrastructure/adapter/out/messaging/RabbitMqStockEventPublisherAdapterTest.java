package edu.dosw.styleradar.inventario.infrastructure.adapter.out.messaging;

import edu.dosw.styleradar.inventario.domain.event.StockActualizadoEvent;
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
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class RabbitMqStockEventPublisherAdapterTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RabbitMqStockEventPublisherAdapter publisher;

    @Test
    @DisplayName("Publica evento StockActualizadoEvent con eventoId único y routing key oficial")
    void publicaStockActualizado() {
        UUID eventId = UUID.randomUUID();
        StockActualizadoEvent evento = new StockActualizadoEvent(
                eventId, UUID.randomUUID(), UUID.randomUUID(), "M", 10, 0, Instant.now());

        publisher.publicar(evento);

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMqConfig.EXCHANGE_EVENTS),
                eq("styleradar.inventario.actualizado"),
                captor.capture());

        Map<String, Object> payload = captor.getValue();
        assertThat(payload).containsEntry("producer", "ms-inventario");
        assertThat(payload).containsEntry("eventoId", eventId);
        assertThat(payload).containsEntry("eventId", eventId);
        assertThat(payload).containsEntry("eventType", "styleradar.inventario.actualizado");
    }

    @Test
    @DisplayName("Publica evento StockAgotadoEvent con routing key styleradar.inventario.stock.agotado")
    void publicaStockAgotado() {
        StockAgotadoEvent evento = new StockAgotadoEvent(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "M", 0, Instant.now());

        publisher.publicar(evento);

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMqConfig.EXCHANGE_EVENTS),
                eq("styleradar.inventario.stock.agotado"),
                captor.capture());

        assertThat(captor.getValue()).containsEntry("eventType", "styleradar.inventario.stock.agotado");
    }

    @Test
    @DisplayName("Publica evento StockBajoEvent con routing key styleradar.inventario.stock.ultimas-unidades")
    void publicaStockBajo() {
        StockBajoEvent evento = new StockBajoEvent(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "38", 2, Instant.now());

        publisher.publicar(evento);

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMqConfig.EXCHANGE_EVENTS),
                eq("styleradar.inventario.stock.ultimas-unidades"),
                captor.capture());

        assertThat(captor.getValue()).containsEntry("eventType", "styleradar.inventario.stock.ultimas-unidades");
    }

    @Test
    @DisplayName("Ignora evento nulo sin interactuar con RabbitTemplate")
    void ignoraEventoNulo() {
        publisher.publicar(null);
        verifyNoInteractions(rabbitTemplate);
    }
}
