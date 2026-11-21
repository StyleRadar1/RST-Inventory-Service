package edu.dosw.styleradar.inventario.application.usecase;

import edu.dosw.styleradar.inventario.application.dto.DisponibilidadPrendaResponseDTO;
import edu.dosw.styleradar.inventario.application.dto.ItemTallaCantidadDTO;
import edu.dosw.styleradar.inventario.application.dto.RegistrarLoteStockCommand;
import edu.dosw.styleradar.inventario.domain.exception.PrendaDeOtroAlmacenException;
import edu.dosw.styleradar.inventario.domain.exception.PrendaNoEncontradaEnCatalogoException;
import edu.dosw.styleradar.inventario.domain.model.StockItem;
import edu.dosw.styleradar.inventario.domain.port.in.ConsultarStockUseCase;
import edu.dosw.styleradar.inventario.domain.port.in.RegistrarLoteStockUseCase;
import edu.dosw.styleradar.inventario.domain.port.out.ItemCatalogoPort;
import edu.dosw.styleradar.inventario.domain.port.out.PrendaCatalogoInfo;
import edu.dosw.styleradar.inventario.domain.port.out.StockEventPublisherPort;
import edu.dosw.styleradar.inventario.domain.port.out.StockRepositoryPort;
import edu.dosw.styleradar.inventario.domain.valueobject.Cantidad;
import edu.dosw.styleradar.inventario.domain.valueobject.Talla;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Service
@Transactional
public class RegistrarLoteStockUseCaseImpl implements RegistrarLoteStockUseCase {

    private final StockRepositoryPort stockRepository;
    private final StockEventPublisherPort eventPublisher;
    private final ConsultarStockUseCase consultarStockUseCase;
    private final ItemCatalogoPort itemCatalogoPort;

    public RegistrarLoteStockUseCaseImpl(StockRepositoryPort stockRepository,
                                        StockEventPublisherPort eventPublisher,
                                        ConsultarStockUseCase consultarStockUseCase,
                                        ItemCatalogoPort itemCatalogoPort) {
        this.stockRepository = stockRepository;
        this.eventPublisher = eventPublisher;
        this.consultarStockUseCase = consultarStockUseCase;
        this.itemCatalogoPort = itemCatalogoPort;
    }

    @Override
    public DisponibilidadPrendaResponseDTO registrarLote(RegistrarLoteStockCommand command) {
        PrendaCatalogoInfo prenda = itemCatalogoPort.obtenerPrendaPorId(command.prendaId())
                .orElseThrow(() -> new PrendaNoEncontradaEnCatalogoException(
                        "La prenda " + command.prendaId() + " no existe en ms-item-catalogo"));

        if (prenda.almacenId() == null || !Objects.equals(command.sedeId(), prenda.almacenId())) {
            throw new PrendaDeOtroAlmacenException(
                    "La prenda " + command.prendaId() + " no pertenece al almacén de la sede " + command.sedeId());
        }

        Instant ahora = Instant.now();
        Instant fechaPub = command.fechaPublicacion() == null ? ahora : command.fechaPublicacion();

        StockItem stockItem = stockRepository
                .buscarPorPrendaYSede(command.prendaId(), command.sedeId())
                .orElseGet(() -> StockItem.crear(command.prendaId(), command.sedeId(),
                        command.tipoPrenda(), fechaPub, ahora));

        Map<Talla, Cantidad> tallasConCantidad = new LinkedHashMap<>();
        for (ItemTallaCantidadDTO itemDTO : command.tallas()) {
            Talla talla = Talla.desde(itemDTO.talla(), command.tipoPrenda());
            tallasConCantidad.put(talla, Cantidad.de(itemDTO.cantidad()));
        }

        stockItem.registrarTallasLote(tallasConCantidad, ahora);
        stockRepository.guardar(stockItem);
        stockItem.pullDomainEvents().forEach(eventPublisher::publicar);

        return consultarStockUseCase.consultarPorPrenda(command.prendaId());
    }
}
