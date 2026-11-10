package edu.dosw.styleradar.inventario.infrastructure.adapter.in.amqp;

import edu.dosw.styleradar.inventario.domain.port.in.SuspenderInventarioUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SuscripcionEventListenerTest {

    @Mock
    private SuspenderInventarioUseCase useCase;

    @InjectMocks
    private SuscripcionEventListener listener;

    @Test
    @DisplayName("Procesa evento de suscripción suspendida con sedeId directo o anidado")
    void procesaSuspenSionValida() {
        UUID sedeId = UUID.randomUUID();
        Map<String, Object> payloadDirecto = Map.of("sedeId", sedeId.toString());

        listener.onSuscripcionSuspendida(payloadDirecto);
        verify(useCase).suspenderPorSede(sedeId);

        Map<String, Object> payloadAnidado = Map.of("data", Map.of("sedeId", sedeId.toString()));
        listener.onSuscripcionSuspendida(payloadAnidado);
        verify(useCase, org.mockito.Mockito.times(2)).suspenderPorSede(sedeId);
    }

    @Test
    @DisplayName("Ignora payloads inválidos o sin sedeId")
    void ignoraPayloadInvalido() {
        listener.onSuscripcionSuspendida(null);
        listener.onSuscripcionSuspendida(Map.of());
        listener.onSuscripcionSuspendida(Map.of("sedeId", "no-es-uuid"));

        verify(useCase, never()).suspenderPorSede(org.mockito.ArgumentMatchers.any());
    }
}
