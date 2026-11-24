package edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence.repository;

import edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence.entity.EventoProcesadoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataEventoProcesadoRepository extends JpaRepository<EventoProcesadoJpaEntity, String> {
}
