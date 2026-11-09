package edu.dosw.styleradar.inventario.application.usecase;

import edu.dosw.styleradar.inventario.application.dto.DisponibilidadPrendaResponseDTO;
import edu.dosw.styleradar.inventario.application.dto.SedeStockDTO;
import edu.dosw.styleradar.inventario.application.dto.VarianteStockDTO;
import edu.dosw.styleradar.inventario.application.dto.VerificacionDisponibilidadDTO;
import edu.dosw.styleradar.inventario.domain.port.in.ConsultarStockUseCase;
import edu.dosw.styleradar.inventario.domain.port.out.StockRepositoryPort;
import edu.dosw.styleradar.inventario.domain.exception.ResourceNotFoundException;
import edu.dosw.styleradar.inventario.domain.model.EstadoItem;
import edu.dosw.styleradar.inventario.domain.model.InventarioVariante;
import edu.dosw.styleradar.inventario.domain.model.StockItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ConsultarStockUseCaseImpl implements ConsultarStockUseCase {

    private final StockRepositoryPort stockRepository;

    public ConsultarStockUseCaseImpl(StockRepositoryPort stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Override
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
    public VerificacionDisponibilidadDTO verificarDisponibilidad(UUID prendaId) {
        Instant ahora = Instant.now();
        List<StockItem> items = stockRepository.buscarPorPrenda(prendaId);
        if (items.isEmpty()) {
            throw new ResourceNotFoundException("No se encontró inventario para la prenda " + prendaId);
        }

        int stockTotal = items.stream()
                .filter(StockItem::isActivo)
                .mapToInt(StockItem::getStockDisponibleTotal)
                .sum();

        EstadoItem estadoGeneral = calcularEstadoGeneral(items, ahora);
        boolean disponibleParaVenta = stockTotal > 0 && estadoGeneral != EstadoItem.SUSPENDIDO;

        return new VerificacionDisponibilidadDTO(prendaId, disponibleParaVenta, estadoGeneral, stockTotal);
    }

    @Override
    public List<UUID> listarPrendasDisponiblesPorSede(UUID sedeId) {
        return stockRepository.buscarDisponiblesPorSede(sedeId).stream()
                .map(StockItem::getPrendaId)
                .distinct()
                .toList();
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
}
