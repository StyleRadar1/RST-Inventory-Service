package edu.dosw.styleradar.inventario.application.service;

import edu.dosw.styleradar.inventario.application.dto.AjustarStockCommand;
import edu.dosw.styleradar.inventario.application.dto.ConfirmarReservaCommand;
import edu.dosw.styleradar.inventario.application.dto.DisponibilidadPrendaResponseDTO;
import edu.dosw.styleradar.inventario.application.dto.ItemTallaCantidadDTO;
import edu.dosw.styleradar.inventario.application.dto.LiberarReservaCommand;
import edu.dosw.styleradar.inventario.application.dto.RegistrarLoteStockCommand;
import edu.dosw.styleradar.inventario.application.dto.RegistrarStockCommand;
import edu.dosw.styleradar.inventario.application.dto.ReservaResponseDTO;
import edu.dosw.styleradar.inventario.application.dto.ReservarStockCommand;
import edu.dosw.styleradar.inventario.application.dto.SedeStockDTO;
import edu.dosw.styleradar.inventario.application.dto.StockItemResponseDTO;
import edu.dosw.styleradar.inventario.application.dto.VarianteStockDTO;
import edu.dosw.styleradar.inventario.application.port.in.AjustarStockUseCase;
import edu.dosw.styleradar.inventario.application.port.in.ConsultarStockUseCase;
import edu.dosw.styleradar.inventario.application.port.in.GestionarReservaUseCase;
import edu.dosw.styleradar.inventario.application.port.in.RegistrarLoteStockUseCase;
import edu.dosw.styleradar.inventario.application.port.in.RegistrarStockUseCase;
import edu.dosw.styleradar.inventario.application.port.in.SuspenderInventarioUseCase;
import edu.dosw.styleradar.inventario.application.port.out.StockEventPublisherPort;
import edu.dosw.styleradar.inventario.application.port.out.StockRepositoryPort;
import edu.dosw.styleradar.inventario.domain.event.DomainEvent;
import edu.dosw.styleradar.inventario.domain.exception.CantidadInvalidaException;
import edu.dosw.styleradar.inventario.domain.exception.ResourceNotFoundException;
import edu.dosw.styleradar.inventario.domain.model.EstadoItem;
import edu.dosw.styleradar.inventario.domain.model.InventarioVariante;
import edu.dosw.styleradar.inventario.domain.model.StockItem;
import edu.dosw.styleradar.inventario.domain.valueobject.Cantidad;
import edu.dosw.styleradar.inventario.domain.valueobject.Talla;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio de aplicación que orquesta los casos de uso del inventario.
 * Mantiene la transacción, coordina el agregado y publica eventos de dominio.
 */
@Service
@Transactional
public class InventarioApplicationService implements RegistrarStockUseCase, RegistrarLoteStockUseCase,
        AjustarStockUseCase, GestionarReservaUseCase, ConsultarStockUseCase, SuspenderInventarioUseCase {

    static final Duration DURACION_RESERVA = Duration.ofMinutes(15);

    private final StockRepositoryPort stockRepository;
    private final StockEventPublisherPort eventPublisher;

    public InventarioApplicationService(StockRepositoryPort stockRepository,
                                        StockEventPublisherPort eventPublisher) {
        this.stockRepository = stockRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public StockItemResponseDTO ejecutar(RegistrarStockCommand command) {
        Instant ahora = Instant.now();
        Talla talla = Talla.desde(command.talla(), command.tipoPrenda());

        StockItem stockItem = stockRepository
                .buscarPorPrendaYSede(command.prendaId(), command.sedeId())
                .orElseGet(() -> StockItem.crear(command.prendaId(), command.sedeId(),
                        command.tipoPrenda(), fechaPublicacion(command, ahora), ahora));

        stockItem.registrarTalla(talla, cantidadDe(command.cantidad()), ahora);
        StockItem guardado = guardarYPublicar(stockItem);
        return construirRespuesta(guardado, talla, ahora);
    }

    @Override
    public DisponibilidadPrendaResponseDTO registrarLote(RegistrarLoteStockCommand command) {
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
        guardarYPublicar(stockItem);

        return consultarPorPrenda(command.prendaId());
    }

    @Override
    public StockItemResponseDTO ejecutar(AjustarStockCommand command) {
        Instant ahora = Instant.now();
        StockItem stockItem = buscarPorId(command.stockItemId());
        Talla talla = Talla.desde(command.talla(), stockItem.getTipoPrenda());

        stockItem.actualizarStockFisico(talla, cantidadDe(command.nuevaCantidad()), ahora);
        StockItem guardado = guardarYPublicar(stockItem);
        return construirRespuesta(guardado, talla, ahora);
    }

    @Override
    public ReservaResponseDTO reservar(ReservarStockCommand command) {
        Instant ahora = Instant.now();
        StockItem stockItem = buscarPorPrendaYSede(command.prendaId(), command.sedeId());
        Talla talla = Talla.desde(command.talla(), stockItem.getTipoPrenda());

        stockItem.reservarStock(talla, cantidadDe(command.cantidad()), ahora);
        StockItem guardado = guardarYPublicar(stockItem);
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

        stockItem.confirmarVenta(talla, cantidadDe(command.cantidad()), ahora);
        guardarYPublicar(stockItem);
    }

    @Override
    public void liberar(LiberarReservaCommand command) {
        Instant ahora = Instant.now();
        StockItem stockItem = buscarPorPrendaYSede(command.prendaId(), command.sedeId());
        Talla talla = Talla.desde(command.talla(), stockItem.getTipoPrenda());

        stockItem.liberarReserva(talla, cantidadDe(command.cantidad()), ahora);
        guardarYPublicar(stockItem);
    }

    @Override
    @Transactional(readOnly = true)
    public DisponibilidadPrendaResponseDTO consultarPorPrenda(UUID prendaId) {
        Instant ahora = Instant.now();
        List<StockItem> items = stockRepository.buscarPorPrenda(prendaId);
        if (items.isEmpty()) {
            throw new ResourceNotFoundException("No se encontró inventario para la prenda " + prendaId);
        }

        List<SedeStockDTO> sedes = items.stream()
                .map(item -> new SedeStockDTO(item.getSedeId(), item.isActivo(),
                        item.getVariantes().values().stream()
                                .map(variante -> new VarianteStockDTO(variante.getTalla().getValor(),
                                        variante.getStockDisponible().valor(),
                                        variante.getStockReservado().valor(),
                                        calcularEstadoVariante(item, variante, ahora)))
                                .toList()))
                .toList();

        int stockTotal = items.stream()
                .filter(StockItem::isActivo)
                .mapToInt(StockItem::getStockDisponibleTotal)
                .sum();

        return new DisponibilidadPrendaResponseDTO(prendaId, items.getFirst().getTipoPrenda(),
                calcularEstadoGeneral(items, ahora), stockTotal, sedes);
    }

    @Override
    public void suspenderPorSede(UUID sedeId) {
        stockRepository.buscarPorSede(sedeId).forEach(stockItem -> {
            stockItem.suspenderPorMora();
            guardarYPublicar(stockItem);
        });
    }

    @Override
    public void reactivarPorSede(UUID sedeId) {
        stockRepository.buscarPorSede(sedeId).forEach(stockItem -> {
            stockItem.reactivarSuscripcion();
            guardarYPublicar(stockItem);
        });
    }

    private StockItem buscarPorId(UUID id) {
        return stockRepository.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el stock item " + id));
    }

    private StockItem buscarPorPrendaYSede(UUID prendaId, UUID sedeId) {
        return stockRepository.buscarPorPrendaYSede(prendaId, sedeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró inventario para la prenda " + prendaId + " en la sede " + sedeId));
    }

    private StockItem guardarYPublicar(StockItem stockItem) {
        StockItem guardado = stockRepository.guardar(stockItem);
        List<DomainEvent> eventos = stockItem.pullDomainEvents();
        eventos.forEach(eventPublisher::publicar);
        return guardado;
    }

    private StockItemResponseDTO construirRespuesta(StockItem stockItem, Talla talla, Instant ahora) {
        InventarioVariante variante = stockItem.obtenerVariante(talla);
        return new StockItemResponseDTO(stockItem.getId(), stockItem.getPrendaId(), stockItem.getSedeId(),
                stockItem.getTipoPrenda(), talla.getValor(), variante.getStockDisponible().valor(),
                variante.getStockReservado().valor(), stockItem.calcularEstado(ahora),
                stockItem.isActivo(), stockItem.getFechaActualizacion());
    }

    private EstadoItem calcularEstadoGeneral(List<StockItem> items, Instant ahora) {
        List<StockItem> activos = items.stream().filter(StockItem::isActivo).toList();
        if (activos.isEmpty()) {
            return EstadoItem.SUSPENDIDO;
        }
        int stockTotal = activos.stream().mapToInt(StockItem::getStockDisponibleTotal).sum();
        if (stockTotal == 0) {
            return EstadoItem.AGOTADA;
        }
        int umbralTotal = activos.stream().mapToInt(StockItem::getUmbralUltimasUnidades).sum();
        if (stockTotal <= umbralTotal) {
            return EstadoItem.ULTIMAS_UNIDADES;
        }
        boolean tieneNovedad = activos.stream()
                .anyMatch(item -> item.getFechaPublicacion().plus(Duration.ofDays(7)).isAfter(ahora));
        return tieneNovedad ? EstadoItem.NUEVA_PRENDA : EstadoItem.DISPONIBLE;
    }

    private EstadoItem calcularEstadoVariante(StockItem stockItem, InventarioVariante variante, Instant ahora) {
        if (!stockItem.isActivo()) {
            return EstadoItem.SUSPENDIDO;
        }
        int disponible = variante.getStockDisponible().valor();
        if (disponible == 0) {
            return EstadoItem.AGOTADA;
        }
        if (disponible <= stockItem.getUmbralUltimasUnidades()) {
            return EstadoItem.ULTIMAS_UNIDADES;
        }
        return stockItem.getFechaPublicacion().plus(Duration.ofDays(7)).isAfter(ahora)
                ? EstadoItem.NUEVA_PRENDA
                : EstadoItem.DISPONIBLE;
    }

    private static Instant fechaPublicacion(RegistrarStockCommand command, Instant ahora) {
        return command.fechaPublicacion() == null ? ahora : command.fechaPublicacion();
    }

    private static Cantidad cantidadDe(Integer valor) {
        if (valor == null) {
            throw new CantidadInvalidaException("La cantidad es obligatoria");
        }
        return Cantidad.de(valor);
    }
}
