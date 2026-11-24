package edu.dosw.styleradar.inventario.infrastructure.adapter.in.amqp;

import edu.dosw.styleradar.inventario.domain.port.in.SuspenderInventarioUseCase;
import edu.dosw.styleradar.inventario.domain.port.out.EventoProcesadoPort;
import edu.dosw.styleradar.inventario.infrastructure.config.RabbitMqConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuscripcionEventListenerTest {

    @Mock
    private SuspenderInventarioUseCase useCase;

    @Mock
    private EventoProcesadoPort eventoProcesadoPort;

    @InjectMocks
    private SuscripcionEventListener listener;

    @Test
    @DisplayName("RES-139: Procesa styleradar.pagos.suscripcion.suspendida con sobre data (sedeId o tiendaId)")
    void procesaSuspenSionRealPagos() {
        UUID sedeId = UUID.randomUUID();
        Map<String, Object> payloadNqs = Map.of(
                "eventoId", "evt-pagos-susp-1",
                "eventType", RabbitMqConfig.ROUTING_KEY_SUSCRIPCION_SUSPENDIDA,
                "data", Map.of("sedeId", sedeId.toString()));

        when(eventoProcesadoPort.yaFueProcesado("evt-pagos-susp-1")).thenReturn(false);

        listener.onSuscripcionSuspendida(payloadNqs);
        verify(useCase).suspenderPorSede(sedeId);
        verify(eventoProcesadoPort).registrarProcesado(
                "evt-pagos-susp-1", RabbitMqConfig.ROUTING_KEY_SUSCRIPCION_SUSPENDIDA);

        UUID sede2 = UUID.randomUUID();
        Map<String, Object> payloadTiendaId = Map.of(
                "data", Map.of("eventId", "evt-pagos-susp-2", "tiendaId", sede2.toString()));
        when(eventoProcesadoPort.yaFueProcesado("evt-pagos-susp-2")).thenReturn(false);

        listener.onSuscripcionSuspendida(payloadTiendaId);
        verify(useCase).suspenderPorSede(sede2);
        verify(eventoProcesadoPort).registrarProcesado(
                "evt-pagos-susp-2", RabbitMqConfig.ROUTING_KEY_SUSCRIPCION_SUSPENDIDA);
    }

    @Test
    @DisplayName("Omite evento duplicado garantizando idempotencia")
    void omiteEventoDuplicado() {
        UUID sedeId = UUID.randomUUID();
        when(eventoProcesadoPort.yaFueProcesado("evt-dup")).thenReturn(true);

        listener.onSuscripcionSuspendida(Map.of(
                "eventId", "evt-dup",
                "data", Map.of("sedeId", sedeId.toString())));

        verify(useCase, never()).suspenderPorSede(any());
        verify(eventoProcesadoPort, never()).registrarProcesado(any(), any());
    }

    @Test
    @DisplayName("Ignora payloads inválidos o sin sedeId")
    void ignoraPayloadInvalido() {
        listener.onSuscripcionSuspendida(null);
        listener.onSuscripcionSuspendida(Map.of());
        listener.onSuscripcionSuspendida(Map.of("data", Map.of("sedeId", "no-es-uuid")));

        verify(useCase, never()).suspenderPorSede(any());
    }
}
