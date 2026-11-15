package edu.dosw.styleradar.inventario.infrastructure.adapter.in.amqp;

import edu.dosw.styleradar.inventario.domain.port.in.SuspenderInventarioUseCase;
import edu.dosw.styleradar.inventario.domain.port.out.EventoProcesadoPort;
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
    @DisplayName("Procesa evento de suscripción suspendida con sedeId directo o anidado")
    void procesaSuspenSionValida() {
        UUID sedeId = UUID.randomUUID();
        Map<String, Object> payloadDirecto = Map.of(
                "eventoId", "evt-1",
                "sedeId", sedeId.toString());

        when(eventoProcesadoPort.yaFueProcesado("evt-1")).thenReturn(false);

        listener.onSuscripcionSuspendida(payloadDirecto);
        verify(useCase).suspenderPorSede(sedeId);
        verify(eventoProcesadoPort).registrarProcesado(eq("evt-1"), any());

        Map<String, Object> payloadAnidado = Map.of("data", Map.of("sedeId", sedeId.toString()));
        listener.onSuscripcionSuspendida(payloadAnidado);
        verify(useCase, org.mockito.Mockito.times(2)).suspenderPorSede(sedeId);
    }

    @Test
    @DisplayName("Omite evento duplicado garantizando idempotencia")
    void omiteEventoDuplicado() {
        UUID sedeId = UUID.randomUUID();
        when(eventoProcesadoPort.yaFueProcesado("evt-dup")).thenReturn(true);

        listener.onSuscripcionSuspendida(Map.of(
                "eventId", "evt-dup",
                "sedeId", sedeId.toString()));

        verify(useCase, never()).suspenderPorSede(any());
        verify(eventoProcesadoPort, never()).registrarProcesado(any(), any());
    }

    @Test
    @DisplayName("Ignora payloads inválidos o sin sedeId")
    void ignoraPayloadInvalido() {
        listener.onSuscripcionSuspendida(null);
        listener.onSuscripcionSuspendida(Map.of());
        listener.onSuscripcionSuspendida(Map.of("sedeId", "no-es-uuid"));

        verify(useCase, never()).suspenderPorSede(any());
    }
}
