package edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.inventario.application.port.out.StockRepositoryPort;
import edu.dosw.styleradar.inventario.domain.model.InventarioVariante;
import edu.dosw.styleradar.inventario.domain.model.StockItem;
import edu.dosw.styleradar.inventario.domain.valueobject.Cantidad;
import edu.dosw.styleradar.inventario.domain.valueobject.Talla;
import edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence.entity.InventarioVarianteJpaEntity;
import edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence.entity.StockItemJpaEntity;
import edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence.repository.SpringDataStockItemRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de salida que traduce entre el agregado de dominio y JPA.
 */
@Component
public class PostgreSqlStockRepositoryAdapter implements StockRepositoryPort {

    private final SpringDataStockItemRepository repository;

    public PostgreSqlStockRepositoryAdapter(SpringDataStockItemRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StockItem> buscarPorId(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StockItem> buscarPorPrendaYSede(UUID prendaId, UUID sedeId) {
        return repository.findByPrendaIdAndSedeId(prendaId, sedeId).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockItem> buscarPorPrenda(UUID prendaId) {
        return repository.findByPrendaId(prendaId).stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockItem> buscarPorSede(UUID sedeId) {
        return repository.findBySedeId(sedeId).stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional
    public StockItem guardar(StockItem stockItem) {
        StockItemJpaEntity entity = repository.findById(stockItem.getId())
                .orElseGet(() -> nuevaEntidad(stockItem));

        actualizarCabecera(entity, stockItem);
        sincronizarVariantes(entity, stockItem);

        return toDomain(repository.save(entity));
    }

    private StockItemJpaEntity nuevaEntidad(StockItem stockItem) {
        StockItemJpaEntity entity = new StockItemJpaEntity();
        entity.setId(stockItem.getId());
        return entity;
    }

    private void actualizarCabecera(StockItemJpaEntity entity, StockItem stockItem) {
        entity.setPrendaId(stockItem.getPrendaId());
        entity.setSedeId(stockItem.getSedeId());
        entity.setTipoPrenda(stockItem.getTipoPrenda());
        entity.setEstado(stockItem.calcularEstado(Instant.now()));
        entity.setUmbralUltimasUnidades(stockItem.getUmbralUltimasUnidades());
        entity.setActivo(stockItem.isActivo());
        entity.setFechaPublicacion(stockItem.getFechaPublicacion());
        entity.setFechaActualizacion(stockItem.getFechaActualizacion());
    }

    private void sincronizarVariantes(StockItemJpaEntity entity, StockItem stockItem) {
        Map<String, InventarioVariante> variantesDominio = stockItem.getVariantes();
        Map<String, InventarioVarianteJpaEntity> existentes = new LinkedHashMap<>();
        entity.getVariantes().forEach(variante -> existentes.put(variante.getTalla(), variante));

        entity.getVariantes().removeIf(variante -> !variantesDominio.containsKey(variante.getTalla()));

        for (Map.Entry<String, InventarioVariante> entrada : variantesDominio.entrySet()) {
            InventarioVariante varianteDominio = entrada.getValue();
            InventarioVarianteJpaEntity varianteEntity = existentes.get(entrada.getKey());
            if (varianteEntity == null) {
                varianteEntity = new InventarioVarianteJpaEntity();
                varianteEntity.setId(idVariante(stockItem.getId(), entrada.getKey()));
                varianteEntity.setTalla(entrada.getKey());
                entity.agregarVariante(varianteEntity);
            }
            varianteEntity.setUnidadesDisponibles(varianteDominio.getStockDisponible().valor());
            varianteEntity.setUnidadesReservadas(varianteDominio.getStockReservado().valor());
        }
    }

    private StockItem toDomain(StockItemJpaEntity entity) {
        Map<String, InventarioVariante> variantes = new LinkedHashMap<>();
        for (InventarioVarianteJpaEntity varianteEntity : entity.getVariantes()) {
            Talla talla = Talla.desde(varianteEntity.getTalla(), entity.getTipoPrenda());
            variantes.put(talla.getValor(), new InventarioVariante(talla,
                    Cantidad.de(varianteEntity.getUnidadesDisponibles()),
                    Cantidad.de(varianteEntity.getUnidadesReservadas())));
        }

        return StockItem.reconstruir(entity.getId(), entity.getPrendaId(), entity.getSedeId(),
                entity.getTipoPrenda(), entity.getFechaPublicacion(), entity.getFechaActualizacion(),
                entity.getUmbralUltimasUnidades(), entity.isActivo(), variantes);
    }

    private static UUID idVariante(UUID stockItemId, String talla) {
        return UUID.nameUUIDFromBytes((stockItemId + ":" + talla).getBytes(StandardCharsets.UTF_8));
    }
}
