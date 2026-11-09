package edu.dosw.styleradar.inventario.application.usecase;

import edu.dosw.styleradar.inventario.application.dto.AjustarStockCommand;
import edu.dosw.styleradar.inventario.application.dto.StockItemResponseDTO;
import edu.dosw.styleradar.inventario.application.mapper.StockItemDtoMapper;
import edu.dosw.styleradar.inventario.domain.port.in.AjustarStockUseCase;
import edu.dosw.styleradar.inventario.domain.port.out.StockEventPublisherPort;
import edu.dosw.styleradar.inventario.domain.port.out.StockRepositoryPort;
import edu.dosw.styleradar.inventario.domain.exception.ResourceNotFoundException;
import edu.dosw.styleradar.inventario.domain.model.InventarioVariante;
import edu.dosw.styleradar.inventario.domain.model.StockItem;
import edu.dosw.styleradar.inventario.domain.valueobject.Cantidad;
import edu.dosw.styleradar.inventario.domain.valueobject.Talla;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class AjustarStockUseCaseImpl implements AjustarStockUseCase {

    private final StockRepositoryPort stockRepository;
    private final StockEventPublisherPort eventPublisher;
    private final StockItemDtoMapper dtoMapper;

    public AjustarStockUseCaseImpl(StockRepositoryPort stockRepository,
                                 StockEventPublisherPort eventPublisher,
                                 StockItemDtoMapper dtoMapper) {
        this.stockRepository = stockRepository;
        this.eventPublisher = eventPublisher;
        this.dtoMapper = dtoMapper;
    }

    @Override
    public StockItemResponseDTO ejecutar(AjustarStockCommand command) {
        Instant ahora = Instant.now();
        StockItem stockItem = stockRepository.buscarPorId(command.stockItemId())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el stock item " + command.stockItemId()));

        Talla talla = Talla.desde(command.talla(), stockItem.getTipoPrenda());
        stockItem.actualizarStockFisico(talla, Cantidad.de(command.nuevaCantidad()), ahora);

        StockItem guardado = stockRepository.guardar(stockItem);
        stockItem.pullDomainEvents().forEach(eventPublisher::publicar);

        InventarioVariante variante = guardado.obtenerVariante(talla);
        return dtoMapper.toResponse(guardado, talla.getValor(), variante.getStockDisponible().valor(),
                variante.getStockReservado().valor(), guardado.calcularEstado(ahora), guardado.getFechaActualizacion());
    }

    @Override
    public StockItemResponseDTO actualizarPorPrendaYSede(UUID prendaId, UUID sedeId, String tallaStr, Integer nuevasUnidades) {
        Instant ahora = Instant.now();
        StockItem stockItem = stockRepository.buscarPorPrendaYSede(prendaId, sedeId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró inventario para la prenda " + prendaId + " en la sede " + sedeId));

        Talla talla = Talla.desde(tallaStr, stockItem.getTipoPrenda());
        stockItem.actualizarStockFisico(talla, Cantidad.de(nuevasUnidades), ahora);

        StockItem guardado = stockRepository.guardar(stockItem);
        stockItem.pullDomainEvents().forEach(eventPublisher::publicar);

        InventarioVariante variante = guardado.obtenerVariante(talla);
        return dtoMapper.toResponse(guardado, talla.getValor(), variante.getStockDisponible().valor(),
                variante.getStockReservado().valor(), guardado.calcularEstado(ahora), guardado.getFechaActualizacion());
    }
}
