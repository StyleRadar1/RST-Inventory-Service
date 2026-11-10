package edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.inventario.domain.port.out.StockRepositoryPort;
import edu.dosw.styleradar.inventario.domain.model.StockItem;
import edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence.entity.StockItemJpaEntity;
import edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence.mapper.StockItemEntityMapper;
import edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence.repository.SpringDataStockItemRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PostgreSqlStockRepositoryAdapter implements StockRepositoryPort {

    private final SpringDataStockItemRepository repository;
    private final StockItemEntityMapper entityMapper;

    public PostgreSqlStockRepositoryAdapter(SpringDataStockItemRepository repository,
                                            StockItemEntityMapper entityMapper) {
        this.repository = repository;
        this.entityMapper = entityMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StockItem> buscarPorId(UUID id) {
        return repository.findById(id).map(entityMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StockItem> buscarPorPrendaYSede(UUID prendaId, UUID sedeId) {
        return repository.findByPrendaIdAndSedeId(prendaId, sedeId).map(entityMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockItem> buscarPorPrenda(UUID prendaId) {
        return repository.findByPrendaId(prendaId).stream().map(entityMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockItem> buscarPorSede(UUID sedeId) {
        return repository.findBySedeId(sedeId).stream().map(entityMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockItem> buscarDisponiblesPorSede(UUID sedeId) {
        return repository.findBySedeId(sedeId).stream()
                .map(entityMapper::toDomain)
                .filter(item -> item.isActivo() && !item.estaAgotada())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockItem> buscarPrendasDisponiblesPorTalla(String talla, UUID sedeId) {
        return repository.buscarPrendasDisponiblesPorTalla(talla, sedeId).stream()
                .map(entityMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public StockItem guardar(StockItem stockItem) {
        StockItemJpaEntity entity = repository.findById(stockItem.getId())
                .orElseGet(() -> {
                    StockItemJpaEntity nueva = new StockItemJpaEntity();
                    nueva.setId(stockItem.getId());
                    return nueva;
                });

        entityMapper.actualizarEntidad(entity, stockItem);
        return entityMapper.toDomain(repository.save(entity));
    }
}
