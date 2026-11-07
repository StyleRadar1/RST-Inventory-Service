package edu.dosw.styleradar.inventario.application.service;

import edu.dosw.styleradar.inventario.application.dto.StockItemResponseDTO;
import edu.dosw.styleradar.inventario.application.port.out.StockEventPublisherPort;
import edu.dosw.styleradar.inventario.application.port.out.StockRepositoryPort;
import edu.dosw.styleradar.inventario.domain.event.StockAgotadoEvent;
import edu.dosw.styleradar.inventario.domain.event.StockBajoEvent;
import edu.dosw.styleradar.inventario.domain.exception.CantidadInvalidaException;
import edu.dosw.styleradar.inventario.domain.exception.InventarioSuspendidoException;
import edu.dosw.styleradar.inventario.domain.exception.TallaNoRegistradaException;
import edu.dosw.styleradar.inventario.domain.model.StockItem;
import edu.dosw.styleradar.inventario.domain.model.TipoPrenda;
import edu.dosw.styleradar.inventario.domain.valueobject.Cantidad;
import edu.dosw.styleradar.inventario.domain.valueobject.TallaRopa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActualizarDisponibilidadTallaTest {

    @Mock
    private StockRepositoryPort stockRepository;

    @Mock
    private StockEventPublisherPort eventPublisher;

    @InjectMocks
    private InventarioApplicationService service;

    private UUID prendaId;
    private UUID sedeId;
    private Instant ahora;
    private StockItem stockItem;

    @BeforeEach
    void setUp() {
        prendaId = UUID.randomUUID();
        sedeId = UUID.randomUUID();
        ahora = Instant.now();
        stockItem = StockItem.crear(prendaId, sedeId, TipoPrenda.ROPA, ahora, ahora);
        stockItem.registrarTalla(TallaRopa.M, Cantidad.de(10), ahora);
        stockItem.pullDomainEvents();
    }

    @Test
    @DisplayName("RES-7 / SR-RF-56: Modifica exitosamente las existencias de una talla de forma independiente")
    void modificaDisponibilidadTallaExitoso() {
        when(stockRepository.buscarPorPrendaYSede(prendaId, sedeId)).thenReturn(Optional.of(stockItem));
        when(stockRepository.guardar(any(StockItem.class))).thenAnswer(inv -> inv.getArgument(0));

        StockItemResponseDTO response = service.actualizarPorPrendaYSede(prendaId, sedeId, "M", 25);

        assertThat(response.stockDisponible()).isEqualTo(25);
        assertThat(stockItem.obtenerVariante(TallaRopa.M).getStockDisponible().valor()).isEqualTo(25);
        verify(stockRepository).guardar(stockItem);
    }

    @Test
    @DisplayName("RES-7 / SR-RF-56: Al modificar a stock bajo (<= 3), dispara StockBajoEvent")
    void modificaADisponibilidadBajaDisparaEvento() {
        when(stockRepository.buscarPorPrendaYSede(prendaId, sedeId)).thenReturn(Optional.of(stockItem));
        when(stockRepository.guardar(any(StockItem.class))).thenAnswer(inv -> inv.getArgument(0));

        service.actualizarPorPrendaYSede(prendaId, sedeId, "M", 2);

        verify(eventPublisher).publicar(any(StockBajoEvent.class));
    }

    @Test
    @DisplayName("RES-7 / SR-RF-56: Al modificar a 0 existencias, dispara StockAgotadoEvent")
    void modificaACeroDisparaEventoAgotado() {
        when(stockRepository.buscarPorPrendaYSede(prendaId, sedeId)).thenReturn(Optional.of(stockItem));
        when(stockRepository.guardar(any(StockItem.class))).thenAnswer(inv -> inv.getArgument(0));

        service.actualizarPorPrendaYSede(prendaId, sedeId, "M", 0);

        verify(eventPublisher).publicar(any(StockAgotadoEvent.class));
    }

    @Test
    @DisplayName("RES-7 / SR-RF-56: Falla con 404 si la talla no ha sido registrada")
    void fallaSiTallaNoEstaRegistrada() {
        when(stockRepository.buscarPorPrendaYSede(prendaId, sedeId)).thenReturn(Optional.of(stockItem));

        assertThatThrownBy(() -> service.actualizarPorPrendaYSede(prendaId, sedeId, "XL", 5))
                .isInstanceOf(TallaNoRegistradaException.class)
                .hasMessageContaining("no está registrada");
    }

    @Test
    @DisplayName("RES-7 / SR-RF-56: Falla con 400 si las unidades son negativas")
    void fallaSiUnidadesNegativas() {
        when(stockRepository.buscarPorPrendaYSede(prendaId, sedeId)).thenReturn(Optional.of(stockItem));

        assertThatThrownBy(() -> service.actualizarPorPrendaYSede(prendaId, sedeId, "M", -5))
                .isInstanceOf(CantidadInvalidaException.class);
    }

    @Test
    @DisplayName("RES-7 / SR-RF-56: Falla con 403 si la sede está suspendida por mora")
    void fallaSiSedeSuspendida() {
        stockItem.suspenderPorMora();
        when(stockRepository.buscarPorPrendaYSede(prendaId, sedeId)).thenReturn(Optional.of(stockItem));

        assertThatThrownBy(() -> service.actualizarPorPrendaYSede(prendaId, sedeId, "M", 10))
                .isInstanceOf(InventarioSuspendidoException.class);
    }
}
