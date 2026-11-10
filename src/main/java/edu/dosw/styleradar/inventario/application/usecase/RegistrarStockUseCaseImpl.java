package edu.dosw.styleradar.inventario.application.usecase;

import edu.dosw.styleradar.inventario.application.dto.RegistrarStockCommand;
import edu.dosw.styleradar.inventario.application.dto.StockItemResponseDTO;
import edu.dosw.styleradar.inventario.application.mapper.StockItemDtoMapper;
import edu.dosw.styleradar.inventario.domain.port.in.RegistrarStockUseCase;
import edu.dosw.styleradar.inventario.domain.port.out.StockEventPublisherPort;
import edu.dosw.styleradar.inventario.domain.port.out.StockRepositoryPort;
import edu.dosw.styleradar.inventario.domain.model.InventarioVariante;
import edu.dosw.styleradar.inventario.domain.model.StockItem;
import edu.dosw.styleradar.inventario.domain.valueobject.Cantidad;
import edu.dosw.styleradar.inventario.domain.valueobject.Talla;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class RegistrarStockUseCaseImpl implements RegistrarStockUseCase {

    private final StockRepositoryPort stockRepository;
    private final StockEventPublisherPort eventPublisher;
    private final StockItemDtoMapper dtoMapper;

    public RegistrarStockUseCaseImpl(StockRepositoryPort stockRepository,
                                    StockEventPublisherPort eventPublisher,
                                    StockItemDtoMapper dtoMapper) {
        this.stockRepository = stockRepository;
        this.eventPublisher = eventPublisher;
        this.dtoMapper = dtoMapper;
    }

    @Override
    public StockItemResponseDTO ejecutar(RegistrarStockCommand command) {
        Instant ahora = Instant.now();
        Talla talla = Talla.desde(command.talla(), command.tipoPrenda());
        Instant fechaPub = command.fechaPublicacion() == null ? ahora : command.fechaPublicacion();

        StockItem stockItem = stockRepository
                .buscarPorPrendaYSede(command.prendaId(), command.sedeId())
                .orElseGet(() -> StockItem.crear(command.prendaId(), command.sedeId(),
                        command.tipoPrenda(), fechaPub, ahora));

        stockItem.registrarTalla(talla, Cantidad.de(command.cantidad()), ahora);
        StockItem guardado = stockRepository.guardar(stockItem);
        stockItem.pullDomainEvents().forEach(eventPublisher::publicar);

        InventarioVariante variante = guardado.obtenerVariante(talla);
        return dtoMapper.toResponse(guardado, talla.getValor(), variante.getStockDisponible().valor(),
                variante.getStockReservado().valor(), guardado.calcularEstado(ahora), guardado.getFechaActualizacion());
    }
}
