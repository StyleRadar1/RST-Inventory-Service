package edu.dosw.styleradar.inventario.application.usecase;

import edu.dosw.styleradar.inventario.application.dto.ConfirmarReservaCommand;
import edu.dosw.styleradar.inventario.application.dto.LiberarReservaCommand;
import edu.dosw.styleradar.inventario.application.dto.ReservaResponseDTO;
import edu.dosw.styleradar.inventario.application.dto.ReservarStockCommand;
import edu.dosw.styleradar.inventario.domain.port.in.GestionarReservaUseCase;
import edu.dosw.styleradar.inventario.domain.port.out.StockEventPublisherPort;
import edu.dosw.styleradar.inventario.domain.port.out.StockRepositoryPort;
import edu.dosw.styleradar.inventario.domain.exception.ResourceNotFoundException;
import edu.dosw.styleradar.inventario.domain.model.InventarioVariante;
import edu.dosw.styleradar.inventario.domain.model.StockItem;
import edu.dosw.styleradar.inventario.domain.valueobject.Cantidad;
import edu.dosw.styleradar.inventario.domain.valueobject.Talla;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class GestionarReservaUseCaseImpl implements GestionarReservaUseCase {

    private static final Duration DURACION_RESERVA = Duration.ofMinutes(15);

    private final StockRepositoryPort stockRepository;
    private final StockEventPublisherPort eventPublisher;

    public GestionarReservaUseCaseImpl(StockRepositoryPort stockRepository,
                                      StockEventPublisherPort eventPublisher) {
        this.stockRepository = stockRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ReservaResponseDTO reservar(ReservarStockCommand command) {
        Instant ahora = Instant.now();
        StockItem stockItem = buscarPorPrendaYSede(command.prendaId(), command.sedeId());
        Talla talla = Talla.desde(command.talla(), stockItem.getTipoPrenda());

        stockItem.reservarStock(talla, Cantidad.de(command.cantidad()), ahora);
        StockItem guardado = stockRepository.guardar(stockItem);
        stockItem.pullDomainEvents().forEach(eventPublisher::publicar);

        InventarioVariante variante = guardado.obtenerVariante(talla);
        return new ReservaResponseDTO(true, guardado.getPrendaId(), guardado.getSedeId(),
                talla.getValor(), command.cantidad(), variante.getStockDisponible().valor(),
                ahora.plus(DURACION_RESERVA));
    }

    @Override
    public void confirmar(ConfirmarReservaCommand command) {
        Instant ahora = Instant.now();
        StockItem stockItem = buscarPorPrendaYSede(command.prendaId(), command.sedeId());
        Talla talla = Talla.desde(command.talla(), stockItem.getTipoPrenda());

        stockItem.confirmarVenta(talla, Cantidad.de(command.cantidad()), ahora);
        stockRepository.guardar(stockItem);
        stockItem.pullDomainEvents().forEach(eventPublisher::publicar);
    }

    @Override
    public void liberar(LiberarReservaCommand command) {
        Instant ahora = Instant.now();
        StockItem stockItem = buscarPorPrendaYSede(command.prendaId(), command.sedeId());
        Talla talla = Talla.desde(command.talla(), stockItem.getTipoPrenda());

        stockItem.liberarReserva(talla, Cantidad.de(command.cantidad()), ahora);
        stockRepository.guardar(stockItem);
        stockItem.pullDomainEvents().forEach(eventPublisher::publicar);
    }

    private StockItem buscarPorPrendaYSede(UUID prendaId, UUID sedeId) {
        return stockRepository.buscarPorPrendaYSede(prendaId, sedeId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró inventario para la prenda " + prendaId + " en la sede " + sedeId));
    }
}
