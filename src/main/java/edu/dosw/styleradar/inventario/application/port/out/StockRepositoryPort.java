package edu.dosw.styleradar.inventario.application.port.out;

import edu.dosw.styleradar.inventario.domain.model.StockItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockRepositoryPort {

    Optional<StockItem> buscarPorId(UUID id);

    Optional<StockItem> buscarPorPrendaYSede(UUID prendaId, UUID sedeId);

    List<StockItem> buscarPorPrenda(UUID prendaId);

    List<StockItem> buscarPorSede(UUID sedeId);

    StockItem guardar(StockItem stockItem);
}
