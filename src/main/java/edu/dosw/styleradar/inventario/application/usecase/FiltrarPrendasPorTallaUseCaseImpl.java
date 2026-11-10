package edu.dosw.styleradar.inventario.application.usecase;

import edu.dosw.styleradar.inventario.domain.port.in.FiltrarPrendasPorTallaUseCase;
import edu.dosw.styleradar.inventario.domain.port.out.StockRepositoryPort;
import edu.dosw.styleradar.inventario.domain.model.StockItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class FiltrarPrendasPorTallaUseCaseImpl implements FiltrarPrendasPorTallaUseCase {

    private final StockRepositoryPort stockRepository;

    public FiltrarPrendasPorTallaUseCaseImpl(StockRepositoryPort stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Override
    public List<UUID> filtrarPrendasDisponiblesPorTalla(String talla, UUID sedeId) {
        if (talla == null || talla.isBlank()) {
            return List.of();
        }
        return stockRepository.buscarPrendasDisponiblesPorTalla(talla.trim(), sedeId).stream()
                .filter(item -> item.tieneStockDisponibleEnTalla(talla))
                .map(StockItem::getPrendaId)
                .distinct()
                .toList();
    }
}
