package edu.dosw.styleradar.inventario.application.service;

import edu.dosw.styleradar.inventario.application.dto.AjustarStockCommand;
import edu.dosw.styleradar.inventario.application.dto.ConfirmarReservaCommand;
import edu.dosw.styleradar.inventario.application.dto.DisponibilidadPrendaResponseDTO;
import edu.dosw.styleradar.inventario.application.dto.LiberarReservaCommand;
import edu.dosw.styleradar.inventario.application.dto.RegistrarStockCommand;
import edu.dosw.styleradar.inventario.application.dto.ReservaResponseDTO;
import edu.dosw.styleradar.inventario.application.dto.ReservarStockCommand;
import edu.dosw.styleradar.inventario.application.dto.StockItemResponseDTO;
import edu.dosw.styleradar.inventario.application.port.out.StockEventPublisherPort;
import edu.dosw.styleradar.inventario.application.port.out.StockRepositoryPort;
import edu.dosw.styleradar.inventario.domain.event.DomainEvent;
import edu.dosw.styleradar.inventario.domain.event.StockAgotadoEvent;
import edu.dosw.styleradar.inventario.domain.exception.ResourceNotFoundException;
import edu.dosw.styleradar.inventario.domain.model.EstadoItem;
import edu.dosw.styleradar.inventario.domain.model.StockItem;
import edu.dosw.styleradar.inventario.domain.model.TipoPrenda;
import edu.dosw.styleradar.inventario.domain.valueobject.Cantidad;
import edu.dosw.styleradar.inventario.domain.valueobject.TallaRopa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventarioApplicationServiceTest {

    @Mock
    private StockRepositoryPort stockRepository;

    @Mock
    private StockEventPublisherPort eventPublisher;

    @InjectMocks
    private InventarioApplicationService service;

    private UUID prendaId;
    private UUID sedeId;
    private Instant ahora;

    @BeforeEach
    void setUp() {
        prendaId = UUID.randomUUID();
        sedeId = UUID.randomUUID();
        ahora = Instant.now();
    }

    @Test
    @DisplayName("Registrar stock crea nuevo agregado si no existe y publica eventos")
    void registrarStockNuevo() {
        RegistrarStockCommand command = new RegistrarStockCommand(prendaId, sedeId, TipoPrenda.ROPA,
                "M", 0, ahora);

        when(stockRepository.buscarPorPrendaYSede(prendaId, sedeId)).thenReturn(Optional.empty());
        when(stockRepository.guardar(any(StockItem.class))).thenAnswer(inv -> inv.getArgument(0));

        StockItemResponseDTO response = service.ejecutar(command);

        assertThat(response.prendaId()).isEqualTo(prendaId);
        assertThat(response.sedeId()).isEqualTo(sedeId);
        assertThat(response.talla()).isEqualTo("M");
        assertThat(response.stockDisponible()).isEqualTo(0);
        assertThat(response.estado()).isEqualTo(EstadoItem.AGOTADA);

        verify(stockRepository).guardar(any(StockItem.class));
        verify(eventPublisher).publicar(any(StockAgotadoEvent.class));
    }

    @Test
    @DisplayName("Ajustar stock sobre un agregado existente")
    void ajustarStockExistente() {
        StockItem item = StockItem.crear(prendaId, sedeId, TipoPrenda.ROPA, ahora, ahora);
        item.registrarTalla(TallaRopa.M, Cantidad.de(5), ahora);
        item.pullDomainEvents();

        when(stockRepository.buscarPorId(item.getId())).thenReturn(Optional.of(item));
        when(stockRepository.guardar(any(StockItem.class))).thenAnswer(inv -> inv.getArgument(0));

        AjustarStockCommand command = new AjustarStockCommand(item.getId(), "M", 8);
        StockItemResponseDTO response = service.ejecutar(command);

        assertThat(response.stockDisponible()).isEqualTo(8);
        verify(stockRepository).guardar(item);
    }

    @Test
    @DisplayName("Ajustar stock falla con 404 si el item no existe")
    void ajustarStockNoExisteFalla() {
        UUID itemId = UUID.randomUUID();
        when(stockRepository.buscarPorId(itemId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.ejecutar(new AjustarStockCommand(itemId, "M", 10)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Reservar stock bloquea unidades y retorna expiración")
    void reservarStock() {
        StockItem item = StockItem.crear(prendaId, sedeId, TipoPrenda.ROPA, ahora, ahora);
        item.registrarTalla(TallaRopa.M, Cantidad.de(10), ahora);
        item.pullDomainEvents();

        when(stockRepository.buscarPorPrendaYSede(prendaId, sedeId)).thenReturn(Optional.of(item));
        when(stockRepository.guardar(any(StockItem.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservarStockCommand command = new ReservarStockCommand(prendaId, sedeId, "M", 2, UUID.randomUUID());
        ReservaResponseDTO response = service.reservar(command);

        assertThat(response.reservaExitosa()).isTrue();
        assertThat(response.unidadesReservadas()).isEqualTo(2);
        assertThat(response.stockDisponibleRestante()).isEqualTo(8);
        assertThat(response.expiracionReserva()).isAfter(ahora);
    }

    @Test
    @DisplayName("Confirmar reserva deduce definitivamente unidades")
    void confirmarReserva() {
        StockItem item = StockItem.crear(prendaId, sedeId, TipoPrenda.ROPA, ahora, ahora);
        item.registrarTalla(TallaRopa.M, Cantidad.de(10), ahora);
        item.reservarStock(TallaRopa.M, Cantidad.de(3), ahora);

        when(stockRepository.buscarPorPrendaYSede(prendaId, sedeId)).thenReturn(Optional.of(item));
        when(stockRepository.guardar(any(StockItem.class))).thenAnswer(inv -> inv.getArgument(0));

        service.confirmar(new ConfirmarReservaCommand(prendaId, sedeId, "M", 3));

        assertThat(item.obtenerVariante(TallaRopa.M).getStockReservado().valor()).isEqualTo(0);
        verify(stockRepository).guardar(item);
    }

    @Test
    @DisplayName("Liberar reserva devuelve existencias a disponible")
    void liberarReserva() {
        StockItem item = StockItem.crear(prendaId, sedeId, TipoPrenda.ROPA, ahora, ahora);
        item.registrarTalla(TallaRopa.M, Cantidad.de(5), ahora);
        item.reservarStock(TallaRopa.M, Cantidad.de(2), ahora);

        when(stockRepository.buscarPorPrendaYSede(prendaId, sedeId)).thenReturn(Optional.of(item));
        when(stockRepository.guardar(any(StockItem.class))).thenAnswer(inv -> inv.getArgument(0));

        service.liberar(new LiberarReservaCommand(prendaId, sedeId, "M", 2));

        assertThat(item.obtenerVariante(TallaRopa.M).getStockDisponible().valor()).isEqualTo(5);
        assertThat(item.obtenerVariante(TallaRopa.M).getStockReservado().valor()).isEqualTo(0);
    }

    @Test
    @DisplayName("Consultar disponibilidad consolida sedes y calcula estado general")
    void consultarDisponibilidad() {
        StockItem item1 = StockItem.crear(prendaId, sedeId, TipoPrenda.ROPA, ahora, ahora);
        item1.registrarTalla(TallaRopa.M, Cantidad.de(6), ahora);

        StockItem item2 = StockItem.crear(prendaId, UUID.randomUUID(), TipoPrenda.ROPA, ahora, ahora);
        item2.registrarTalla(TallaRopa.L, Cantidad.de(4), ahora);

        when(stockRepository.buscarPorPrenda(prendaId)).thenReturn(List.of(item1, item2));

        DisponibilidadPrendaResponseDTO response = service.consultarPorPrenda(prendaId);

        assertThat(response.prendaId()).isEqualTo(prendaId);
        assertThat(response.stockDisponibleTotal()).isEqualTo(10);
        assertThat(response.sedes()).hasSize(2);
    }

    @Test
    @DisplayName("Suspender y reactivar por sede actualiza los agregados")
    void suspenderYReactivarPorSede() {
        StockItem item = StockItem.crear(prendaId, sedeId, TipoPrenda.ROPA, ahora, ahora);
        when(stockRepository.buscarPorSede(sedeId)).thenReturn(List.of(item));
        when(stockRepository.guardar(any(StockItem.class))).thenAnswer(inv -> inv.getArgument(0));

        service.suspenderPorSede(sedeId);
        assertThat(item.isActivo()).isFalse();

        service.reactivarPorSede(sedeId);
        assertThat(item.isActivo()).isTrue();

        verify(stockRepository, times(2)).guardar(item);
    }
}
