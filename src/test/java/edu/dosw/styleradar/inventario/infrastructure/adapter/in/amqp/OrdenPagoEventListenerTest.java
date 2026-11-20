package edu.dosw.styleradar.inventario.infrastructure.adapter.in.amqp;

import edu.dosw.styleradar.inventario.application.dto.ConfirmarReservaCommand;
import edu.dosw.styleradar.inventario.application.dto.LiberarReservaCommand;
import edu.dosw.styleradar.inventario.domain.port.in.GestionarReservaUseCase;
import edu.dosw.styleradar.inventario.domain.port.out.EventoProcesadoPort;
import edu.dosw.styleradar.inventario.infrastructure.config.RabbitMqConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdenPagoEventListenerTest {

    @Mock
    private GestionarReservaUseCase useCase;

    @Mock
    private EventoProcesadoPort eventoProcesadoPort;

    @InjectMocks
    private OrdenPagoEventListener listener;

    @Test
    @DisplayName("RES-139: Procesa styleradar.pagos.pago.confirmado con sobre data plano")
    void procesaPagoConfirmadoConSobreData() {
        UUID prendaId = UUID.randomUUID();
        UUID sedeId = UUID.randomUUID();
        Map<String, Object> payloadNqs = Map.of(
                "eventoId", "evt-pagos-conf-1",
                "eventType", RabbitMqConfig.ROUTING_KEY_PAGO_CONFIRMADO,
                "data", Map.of(
                        "prendaId", prendaId.toString(),
                        "sedeId", sedeId.toString(),
                        "talla", "M",
                        "cantidad", 2));

        when(eventoProcesadoPort.yaFueProcesado("evt-pagos-conf-1")).thenReturn(false);

        listener.onPagoAprobado(payloadNqs);

        ArgumentCaptor<ConfirmarReservaCommand> captor = ArgumentCaptor.forClass(ConfirmarReservaCommand.class);
        verify(useCase).confirmar(captor.capture());
        verify(eventoProcesadoPort).registrarProcesado(
                "evt-pagos-conf-1", RabbitMqConfig.ROUTING_KEY_PAGO_CONFIRMADO);

        assertThat(captor.getValue().prendaId()).isEqualTo(prendaId);
        assertThat(captor.getValue().sedeId()).isEqualTo(sedeId);
        assertThat(captor.getValue().talla()).isEqualTo("M");
        assertThat(captor.getValue().cantidad()).isEqualTo(2);
    }

    @Test
    @DisplayName("RES-139: Procesa styleradar.pagos.pago.confirmado con lista data.items")
    void procesaPagoConfirmadoConListaItemsEnData() {
        UUID prenda1 = UUID.randomUUID();
        UUID prenda2 = UUID.randomUUID();
        UUID sedeId = UUID.randomUUID();

        Map<String, Object> payloadItems = Map.of(
                "eventId", "evt-pagos-items-1",
                "eventType", RabbitMqConfig.ROUTING_KEY_PAGO_CONFIRMADO,
                "data", Map.of(
                        "sedeId", sedeId.toString(),
                        "items", List.of(
                                Map.of("prendaId", prenda1.toString(), "talla", "S", "cantidad", 1),
                                Map.of("prendaId", prenda2.toString(), "talla", "L", "cantidad", 3))));

        when(eventoProcesadoPort.yaFueProcesado("evt-pagos-items-1")).thenReturn(false);

        listener.onPagoAprobado(payloadItems);

        verify(useCase, times(2)).confirmar(any(ConfirmarReservaCommand.class));
        verify(eventoProcesadoPort).registrarProcesado(
                "evt-pagos-items-1", RabbitMqConfig.ROUTING_KEY_PAGO_CONFIRMADO);
    }

    @Test
    @DisplayName("Procesa evento de pago rechazado e invoca caso de uso liberar")
    void procesaPagoRechazado() {
        UUID prendaId = UUID.randomUUID();
        UUID sedeId = UUID.randomUUID();
        Map<String, Object> payload = Map.of(
                "eventId", "evt-rechazado-1",
                "data", Map.of(
                        "prendaId", prendaId.toString(),
                        "sedeId", sedeId.toString(),
                        "talla", "38",
                        "cantidad", "3"));

        when(eventoProcesadoPort.yaFueProcesado("evt-rechazado-1")).thenReturn(false);

        listener.onPagoRechazado(payload);

        ArgumentCaptor<LiberarReservaCommand> captor = ArgumentCaptor.forClass(LiberarReservaCommand.class);
        verify(useCase).liberar(captor.capture());
        verify(eventoProcesadoPort).registrarProcesado(
                "evt-rechazado-1", RabbitMqConfig.ROUTING_KEY_PAGO_RECHAZADO_PENDIENTE);

        assertThat(captor.getValue().prendaId()).isEqualTo(prendaId);
        assertThat(captor.getValue().talla()).isEqualTo("38");
        assertThat(captor.getValue().cantidad()).isEqualTo(3);
    }

    @Test
    @DisplayName("Omite eventos duplicados de pago confirmado y rechazado")
    void omiteEventosDuplicados() {
        when(eventoProcesadoPort.yaFueProcesado("dup-1")).thenReturn(true);
        when(eventoProcesadoPort.yaFueProcesado("dup-2")).thenReturn(true);

        listener.onPagoAprobado(Map.of("eventoId", "dup-1", "talla", "M"));
        listener.onPagoRechazado(Map.of("eventId", "dup-2", "talla", "M"));

        verify(useCase, never()).confirmar(any());
        verify(useCase, never()).liberar(any());
    }

    @Test
    @DisplayName("Ignora payloads incompletos")
    void ignoraIncompletos() {
        listener.onPagoAprobado(Map.of());
        listener.onPagoAprobado(null);
        listener.onPagoRechazado(null);

        verify(useCase, never()).confirmar(any());
        verify(useCase, never()).liberar(any());
    }
}
