package edu.dosw.styleradar.inventario.infrastructure.adapter.in.amqp;

import edu.dosw.styleradar.inventario.domain.port.in.SuspenderInventarioUseCase;
import edu.dosw.styleradar.inventario.infrastructure.config.RabbitMqConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class SuscripcionEventListener {

    private static final Logger log = LoggerFactory.getLogger(SuscripcionEventListener.class);

    private final SuspenderInventarioUseCase suspenderInventarioUseCase;

    public SuscripcionEventListener(SuspenderInventarioUseCase suspenderInventarioUseCase) {
        this.suspenderInventarioUseCase = suspenderInventarioUseCase;
    }

    @RabbitListener(queues = RabbitMqConfig.QUEUE_SUSCRIPCION)
    public void onSuscripcionSuspendida(Map<String, Object> payload) {
        log.info("Recibido evento de suscripción suspendida: {}", payload);
        UUID sedeId = extraerUuid(payload, "sedeId");
        if (sedeId == null) {
            log.warn("Payload de suspensión inválido o sin sedeId: {}", payload);
            return;
        }
        suspenderInventarioUseCase.suspenderPorSede(sedeId);
    }

    static UUID extraerUuid(Map<String, Object> payload, String clave) {
        if (payload == null) {
            return null;
        }
        Object directo = payload.get(clave);
        if (directo != null) {
            try {
                return UUID.fromString(directo.toString());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        Object dataObj = payload.get("data");
        if (dataObj instanceof Map<?, ?> dataMap) {
            Object valor = dataMap.get(clave);
            if (valor != null) {
                try {
                    return UUID.fromString(valor.toString());
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
        }
        return null;
    }
}
