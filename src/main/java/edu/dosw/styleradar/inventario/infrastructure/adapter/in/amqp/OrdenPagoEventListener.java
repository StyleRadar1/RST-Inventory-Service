package edu.dosw.styleradar.inventario.infrastructure.adapter.in.amqp;

import edu.dosw.styleradar.inventario.application.dto.ConfirmarReservaCommand;
import edu.dosw.styleradar.inventario.application.dto.LiberarReservaCommand;
import edu.dosw.styleradar.inventario.domain.port.in.GestionarReservaUseCase;
import edu.dosw.styleradar.inventario.domain.port.out.EventoProcesadoPort;
import edu.dosw.styleradar.inventario.infrastructure.config.RabbitMqConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Component
public class OrdenPagoEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrdenPagoEventListener.class);

    private final GestionarReservaUseCase gestionarReservaUseCase;
    private final EventoProcesadoPort eventoProcesadoPort;

    public OrdenPagoEventListener(GestionarReservaUseCase gestionarReservaUseCase,
                                  EventoProcesadoPort eventoProcesadoPort) {
        this.gestionarReservaUseCase = gestionarReservaUseCase;
        this.eventoProcesadoPort = eventoProcesadoPort;
    }

    @RabbitListener(queues = RabbitMqConfig.QUEUE_PAGO_APROBADO)
    @Transactional
    public void onPagoAprobado(Map<String, Object> payload) {
        log.info("Recibido evento de pago aprobado: {}", payload);
        String eventId = SuscripcionEventListener.extraerEventId(payload);
        if (eventId != null && eventoProcesadoPort.yaFueProcesado(eventId)) {
            log.info("Evento de pago aprobado duplicado (eventId={}), omitiendo.", eventId);
            return;
        }
        ConfirmarReservaCommand command = construirConfirmar(payload);
        if (command == null) {
            log.warn("Payload de pago aprobado incompleto: {}", payload);
            return;
        }
        gestionarReservaUseCase.confirmar(command);
        if (eventId != null) {
            eventoProcesadoPort.registrarProcesado(eventId, RabbitMqConfig.ROUTING_KEY_PAGO_APROBADO);
        }
    }

    @RabbitListener(queues = RabbitMqConfig.QUEUE_PAGO_RECHAZADO)
    @Transactional
    public void onPagoRechazado(Map<String, Object> payload) {
        log.info("Recibido evento de pago rechazado: {}", payload);
        String eventId = SuscripcionEventListener.extraerEventId(payload);
        if (eventId != null && eventoProcesadoPort.yaFueProcesado(eventId)) {
            log.info("Evento de pago rechazado duplicado (eventId={}), omitiendo.", eventId);
            return;
        }
        LiberarReservaCommand command = construirLiberar(payload);
        if (command == null) {
            log.warn("Payload de pago rechazado incompleto: {}", payload);
            return;
        }
        gestionarReservaUseCase.liberar(command);
        if (eventId != null) {
            eventoProcesadoPort.registrarProcesado(eventId, RabbitMqConfig.ROUTING_KEY_PAGO_RECHAZADO);
        }
    }

    private ConfirmarReservaCommand construirConfirmar(Map<String, Object> payload) {
        UUID prendaId = SuscripcionEventListener.extraerUuid(payload, "prendaId");
        UUID sedeId = SuscripcionEventListener.extraerUuid(payload, "sedeId");
        String talla = extraerTexto(payload, "talla");
        Integer cantidad = extraerEntero(payload, "cantidad");

        if (prendaId == null || sedeId == null || talla == null || cantidad == null) {
            return null;
        }
        return new ConfirmarReservaCommand(prendaId, sedeId, talla, cantidad);
    }

    private LiberarReservaCommand construirLiberar(Map<String, Object> payload) {
        UUID prendaId = SuscripcionEventListener.extraerUuid(payload, "prendaId");
        UUID sedeId = SuscripcionEventListener.extraerUuid(payload, "sedeId");
        String talla = extraerTexto(payload, "talla");
        Integer cantidad = extraerEntero(payload, "cantidad");

        if (prendaId == null || sedeId == null || talla == null || cantidad == null) {
            return null;
        }
        return new LiberarReservaCommand(prendaId, sedeId, talla, cantidad);
    }

    static String extraerTexto(Map<String, Object> payload, String clave) {
        if (payload == null) {
            return null;
        }
        Object directo = payload.get(clave);
        if (directo != null) {
            return directo.toString();
        }
        Object dataObj = payload.get("data");
        if (dataObj instanceof Map<?, ?> dataMap) {
            Object valor = dataMap.get(clave);
            return valor == null ? null : valor.toString();
        }
        return null;
    }

    static Integer extraerEntero(Map<String, Object> payload, String clave) {
        if (payload == null) {
            return null;
        }
        Object valorObj = payload.get(clave);
        if (valorObj == null) {
            Object dataObj = payload.get("data");
            if (dataObj instanceof Map<?, ?> dataMap) {
                valorObj = dataMap.get(clave);
            }
        }
        if (valorObj instanceof Number n) {
            return n.intValue();
        }
        if (valorObj != null) {
            try {
                return Integer.parseInt(valorObj.toString());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
