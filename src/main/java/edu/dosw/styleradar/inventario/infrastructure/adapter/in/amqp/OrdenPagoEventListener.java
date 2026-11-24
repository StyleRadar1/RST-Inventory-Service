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

import java.util.ArrayList;
import java.util.List;
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

    /**
     * Consume 'styleradar.pagos.pago.confirmado' publicado por ms-pagos (NQS) con sobre 'data'.
     * Soporta tanto campos directos en 'data' como arreglo 'data.items'.
     */
    @RabbitListener(queues = RabbitMqConfig.QUEUE_PAGO_APROBADO)
    @Transactional
    public void onPagoAprobado(Map<String, Object> payload) {
        log.info("Recibido evento {}: {}", RabbitMqConfig.ROUTING_KEY_PAGO_CONFIRMADO, payload);
        String eventId = SuscripcionEventListener.extraerEventId(payload);
        if (eventId != null && eventoProcesadoPort.yaFueProcesado(eventId)) {
            log.info("Evento de pago confirmado duplicado (eventId={}), omitiendo.", eventId);
            return;
        }

        List<ConfirmarReservaCommand> comandos = extraerComandos(payload, ConfirmarReservaCommand::new);
        if (comandos.isEmpty()) {
            log.warn("Payload de {} incompleto o sin items válidos: {}",
                    RabbitMqConfig.ROUTING_KEY_PAGO_CONFIRMADO, payload);
            return;
        }

        comandos.forEach(gestionarReservaUseCase::confirmar);
        if (eventId != null) {
            eventoProcesadoPort.registrarProcesado(eventId, RabbitMqConfig.ROUTING_KEY_PAGO_CONFIRMADO);
        }
    }

    /**
     * NOTA RES-139: Pendiente confirmar con NQS (ms-pagos) si existe evento de pago rechazado.
     * Se mantiene el consumidor listo sobre la cola local para liberación ante rechazo.
     */
    @RabbitListener(queues = RabbitMqConfig.QUEUE_PAGO_RECHAZADO)
    @Transactional
    public void onPagoRechazado(Map<String, Object> payload) {
        log.info("Recibido evento de pago rechazado: {}", payload);
        String eventId = SuscripcionEventListener.extraerEventId(payload);
        if (eventId != null && eventoProcesadoPort.yaFueProcesado(eventId)) {
            log.info("Evento de pago rechazado duplicado (eventId={}), omitiendo.", eventId);
            return;
        }
        List<LiberarReservaCommand> comandos = extraerComandos(payload, LiberarReservaCommand::new);
        if (comandos.isEmpty()) {
            log.warn("Payload de pago rechazado incompleto o sin items válidos: {}", payload);
            return;
        }
        comandos.forEach(gestionarReservaUseCase::liberar);
        if (eventId != null) {
            eventoProcesadoPort.registrarProcesado(eventId, RabbitMqConfig.ROUTING_KEY_PAGO_RECHAZADO_PENDIENTE);
        }
    }

    private <T> List<T> extraerComandos(Map<String, Object> payload, ItemCommandFactory<T> factory) {
        if (payload == null) {
            return List.of();
        }
        UUID sedeGlobal = SuscripcionEventListener.extraerUuid(payload, "sedeId", "tiendaId");
        Map<?, ?> origenData = (payload.get("data") instanceof Map<?, ?> dataMap) ? dataMap : payload;

        Object itemsObj = origenData.get("items");
        if (itemsObj instanceof List<?> lista) {
            List<T> resultado = new ArrayList<>();
            for (Object elemento : lista) {
                if (elemento instanceof Map<?, ?> itemMap) {
                    T cmd = construirDesdeMapa(itemMap, sedeGlobal, factory);
                    if (cmd != null) {
                        resultado.add(cmd);
                    }
                }
            }
            return resultado;
        }

        T unico = construirDesdeMapa(payload, sedeGlobal, factory);
        return unico == null ? List.of() : List.of(unico);
    }

    private <T> T construirDesdeMapa(Map<?, ?> mapa, UUID sedeFallback, ItemCommandFactory<T> factory) {
        UUID prendaId = SuscripcionEventListener.extraerUuid(mapa, "prendaId");
        UUID sedeId = SuscripcionEventListener.extraerUuid(mapa, "sedeId", "tiendaId");
        if (sedeId == null) {
            sedeId = sedeFallback;
        }
        String talla = extraerTexto(mapa, "talla");
        Integer cantidad = extraerEntero(mapa, "cantidad");

        if (prendaId == null || sedeId == null || talla == null || talla.isBlank()
                || cantidad == null || cantidad <= 0) {
            return null;
        }
        return factory.crear(prendaId, sedeId, talla, cantidad);
    }

    static String extraerTexto(Map<?, ?> payload, String clave) {
        if (payload == null) {
            return null;
        }
        Object dataObj = payload.get("data");
        if (dataObj instanceof Map<?, ?> dataMap) {
            Object valor = dataMap.get(clave);
            if (valor != null) {
                return valor.toString();
            }
        }
        Object directo = payload.get(clave);
        return directo == null ? null : directo.toString();
    }

    static Integer extraerEntero(Map<?, ?> payload, String clave) {
        if (payload == null) {
            return null;
        }
        Object valorObj = null;
        Object dataObj = payload.get("data");
        if (dataObj instanceof Map<?, ?> dataMap) {
            valorObj = dataMap.get(clave);
        }
        if (valorObj == null) {
            valorObj = payload.get(clave);
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

    @FunctionalInterface
    private interface ItemCommandFactory<T> {
        T crear(UUID prendaId, UUID sedeId, String talla, Integer cantidad);
    }
}
