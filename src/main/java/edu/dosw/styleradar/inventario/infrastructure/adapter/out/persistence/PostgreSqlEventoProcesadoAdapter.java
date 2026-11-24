package edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.inventario.domain.port.out.EventoProcesadoPort;
import edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence.entity.EventoProcesadoJpaEntity;
import edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence.repository.SpringDataEventoProcesadoRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class PostgreSqlEventoProcesadoAdapter implements EventoProcesadoPort {

    private final SpringDataEventoProcesadoRepository repository;

    public PostgreSqlEventoProcesadoAdapter(SpringDataEventoProcesadoRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean yaFueProcesado(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            return false;
        }
        return repository.existsById(eventId);
    }

    @Override
    @Transactional
    public void registrarProcesado(String eventId, String eventType) {
        if (eventId == null || eventId.isBlank()) {
            return;
        }
        String tipo = (eventType == null || eventType.isBlank()) ? "UNKNOWN" : eventType;
        repository.save(new EventoProcesadoJpaEntity(eventId, tipo, Instant.now()));
    }
}
