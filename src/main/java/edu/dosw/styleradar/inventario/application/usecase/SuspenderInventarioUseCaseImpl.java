package edu.dosw.styleradar.inventario.application.usecase;

import edu.dosw.styleradar.inventario.domain.port.in.SuspenderInventarioUseCase;
import edu.dosw.styleradar.inventario.domain.port.out.StockEventPublisherPort;
import edu.dosw.styleradar.inventario.domain.port.out.StockRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class SuspenderInventarioUseCaseImpl implements SuspenderInventarioUseCase {

    private final StockRepositoryPort stockRepository;
    private final StockEventPublisherPort eventPublisher;

    public SuspenderInventarioUseCaseImpl(StockRepositoryPort stockRepository,
                                         StockEventPublisherPort eventPublisher) {
        this.stockRepository = stockRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void suspenderPorSede(UUID sedeId) {
        stockRepository.buscarPorSede(sedeId).forEach(stockItem -> {
            stockItem.suspenderPorMora();
            stockRepository.guardar(stockItem);
            stockItem.pullDomainEvents().forEach(eventPublisher::publicar);
        });
    }

    @Override
    public void reactivarPorSede(UUID sedeId) {
        stockRepository.buscarPorSede(sedeId).forEach(stockItem -> {
            stockItem.reactivarSuscripcion();
            stockRepository.guardar(stockItem);
            stockItem.pullDomainEvents().forEach(eventPublisher::publicar);
        });
    }
}
