package edu.dosw.styleradar.inventario.infrastructure.adapter.in.amqp;

import edu.dosw.styleradar.inventario.application.dto.ConfirmarReservaCommand;
import edu.dosw.styleradar.inventario.application.dto.LiberarReservaCommand;
import edu.dosw.styleradar.inventario.application.port.in.GestionarReservaUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrdenPagoEventListenerTest {

    @Mock
    private GestionarReservaUseCase useCase;

    @InjectMocks
    private OrdenPagoEventListener listener;

    @Test
    @DisplayName("Procesa evento de pago aprobado e invoca caso de uso confirmar")
    void procesaPagoAprobado() {
        UUID prendaId = UUID.randomUUID();
        UUID sedeId = UUID.randomUUID();
        Map<String, Object> payload = Map.of(
                "prendaId", prendaId.toString(),
                "sedeId", sedeId.toString(),
                "talla", "M",
                "cantidad", 2);

        listener.onPagoAprobado(payload);

        ArgumentCaptor<ConfirmarReservaCommand> captor = ArgumentCaptor.forClass(ConfirmarReservaCommand.class);
        verify(useCase).confirmar(captor.capture());

        assertThat(captor.getValue().prendaId()).isEqualTo(prendaId);
        assertThat(captor.getValue().talla()).isEqualTo("M");
        assertThat(captor.getValue().cantidad()).isEqualTo(2);
    }

    @Test
    @DisplayName("Procesa evento de pago rechazado e invoca caso de uso liberar")
    void procesaPagoRechazado() {
        UUID prendaId = UUID.randomUUID();
        UUID sedeId = UUID.randomUUID();
        Map<String, Object> payload = Map.of("data", Map.of(
                "prendaId", prendaId.toString(),
                "sedeId", sedeId.toString(),
                "talla", "38",
                "cantidad", "3"));

        listener.onPagoRechazado(payload);

        ArgumentCaptor<LiberarReservaCommand> captor = ArgumentCaptor.forClass(LiberarReservaCommand.class);
        verify(useCase).liberar(captor.capture());

        assertThat(captor.getValue().prendaId()).isEqualTo(prendaId);
        assertThat(captor.getValue().talla()).isEqualTo("38");
        assertThat(captor.getValue().cantidad()).isEqualTo(3);
    }

    @Test
    @DisplayName("Ignora payloads incompletos")
    void ignoraIncompletos() {
        listener.onPagoAprobado(Map.of());
        listener.onPagoRechazado(null);

        verify(useCase, never()).confirmar(org.mockito.ArgumentMatchers.any());
        verify(useCase, never()).liberar(org.mockito.ArgumentMatchers.any());
    }
}
