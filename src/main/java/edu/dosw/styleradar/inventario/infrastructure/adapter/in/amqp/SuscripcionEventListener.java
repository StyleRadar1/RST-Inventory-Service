package edu.dosw.styleradar.inventario.infrastructure.adapter.in.amqp;

import edu.dosw.styleradar.inventario.domain.port.in.SuspenderInventarioUseCase;
import edu.dosw.styleradar.inventario.domain.port.out.EventoProcesadoPort;
import edu.dosw.styleradar.inventario.infrastructure.config.RabbitMqConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Component
public class SuscripcionEventListener {

    private static final Logger log = LoggerFactory.getLogger(SuscripcionEventListener.class);

    private final SuspenderInventarioUseCase suspenderInventarioUseCase;
    private final EventoProcesadoPort eventoProcesadoPort;

    public SuscripcionEventListener(SuspenderInventarioUseCase suspenderInventarioUseCase,
                                     EventoProcesadoPort eventoProcesadoPort) {
        this.suspenderInventarioUseCase = suspenderInventarioUseCase;
        this.eventoProcesadoPort = eventoProcesadoPort;
    }

    @RabbitListener(queues = RabbitMqConfig.QUEUE_SUSCRIPCION)
    @Transactional
    public void onSuscripcionSuspendida(Map<String, Object> payload) {
        log.info("Recibido evento {}: {}", RabbitMqConfig.ROUTING_KEY_SUSCRIPCION_SUSPENDIDA, payload);
        String eventId = extraerEventId(payload);
        if (eventId != null && eventoProcesadoPort.yaFueProcesado(eventId)) {
            log.info("Evento de suscripción suspendida duplicado (eventId={}), omitiendo.", eventId);
            return;
        }
        UUID sedeId = extraerUuid(payload, "sedeId", "tiendaId");
        if (sedeId == null) {
            sedeId = extraerUuidDesdeNit(payload);
        }
        if (sedeId == null) {
            log.warn("Payload de suscripción suspendida inválido o sin sedeId/tiendaId/nit: {}", payload);
            return;
        }
        suspenderInventarioUseCase.suspenderPorSede(sedeId);
        if (eventId != null) {
            eventoProcesadoPort.registrarProcesado(eventId, RabbitMqConfig.ROUTING_KEY_SUSCRIPCION_SUSPENDIDA);
        }
    }

    static String extraerEventId(Map<String, Object> payload) {
        if (payload == null) {
            return null;
        }
        String directo = extraerStringDirecto(payload, "eventoId", "eventId");
        if (directo != null) {
            return directo;
        }
        Object dataObj = payload.get("data");
        if (dataObj instanceof Map<?, ?> dataMap) {
            return extraerStringDirecto(dataMap, "eventoId", "eventId");
        }
        return null;
    }

    static UUID extraerUuid(Map<?, ?> payload, String... claves) {
        if (payload == null) {
            return null;
        }
        // Prioridad al sobre 'data' estándar de ms-pagos, con fallback al nivel raíz
        Object dataObj = payload.get("data");
        if (dataObj instanceof Map<?, ?> dataMap) {
            UUID desdeData = extraerUuidDirecto(dataMap, claves);
            if (desdeData != null) {
                return desdeData;
            }
        }
        return extraerUuidDirecto(payload, claves);
    }

    private static UUID extraerUuidDesdeNit(Map<?, ?> payload) {
        if (payload == null) {
            return null;
        }
        String nit = null;
        Object dataObj = payload.get("data");
        if (dataObj instanceof Map<?, ?> dataMap) {
            nit = extraerStringDirecto(dataMap, "nit", "storeNit");
        }
        if (nit == null) {
            nit = extraerStringDirecto(payload, "nit", "storeNit");
        }
        if (nit == null) {
            return null;
        }
        return UUID.nameUUIDFromBytes(("ms-tiendas:almacen:" + nit.trim()).getBytes(StandardCharsets.UTF_8));
    }

    private static UUID extraerUuidDirecto(Map<?, ?> map, String... claves) {
        for (String clave : claves) {
            Object val = map.get(clave);
            if (val != null) {
                try {
                    return UUID.fromString(val.toString().trim());
                } catch (IllegalArgumentException ignored) {
                    // Continuar con siguiente clave candidata
                }
            }
        }
        return null;
    }

    private static String extraerStringDirecto(Map<?, ?> map, String... claves) {
        for (String clave : claves) {
            Object val = map.get(clave);
            if (val != null) {
                String str = val.toString().trim();
                if (!str.isEmpty()) {
                    return str;
                }
            }
        }
        return null;
    }
}
