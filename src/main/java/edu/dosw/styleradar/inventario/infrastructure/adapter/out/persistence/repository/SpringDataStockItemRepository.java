package edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence.repository;

import edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence.entity.StockItemJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataStockItemRepository extends JpaRepository<StockItemJpaEntity, UUID> {

    @Override
    @EntityGraph(attributePaths = "variantes")
    Optional<StockItemJpaEntity> findById(UUID id);

    @EntityGraph(attributePaths = "variantes")
    Optional<StockItemJpaEntity> findByPrendaIdAndSedeId(UUID prendaId, UUID sedeId);

    @EntityGraph(attributePaths = "variantes")
    List<StockItemJpaEntity> findByPrendaId(UUID prendaId);

    @EntityGraph(attributePaths = "variantes")
    List<StockItemJpaEntity> findBySedeId(UUID sedeId);

    /**
     * Bloqueo pesimista para evitar sobreventa en reservas concurrentes.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = "variantes")
    @Query("SELECT s FROM StockItemJpaEntity s WHERE s.prendaId = :prendaId AND s.sedeId = :sedeId")
    Optional<StockItemJpaEntity> buscarPorPrendaYSedeConBloqueo(@Param("prendaId") UUID prendaId,
                                                                 @Param("sedeId") UUID sedeId);
}
