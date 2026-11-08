package edu.dosw.styleradar.inventario.application.service;

import edu.dosw.styleradar.inventario.application.dto.ConfirmarReservaCommand;
import edu.dosw.styleradar.inventario.application.dto.DisponibilidadPrendaResponseDTO;
import edu.dosw.styleradar.inventario.application.dto.LiberarReservaCommand;
import edu.dosw.styleradar.inventario.application.dto.RegistrarStockCommand;
import edu.dosw.styleradar.inventario.application.dto.ReservarStockCommand;
import edu.dosw.styleradar.inventario.application.port.out.StockEventPublisherPort;
import edu.dosw.styleradar.inventario.application.port.out.StockRepositoryPort;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventarioApplicationServiceExtendedTest {

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
    @DisplayName("Registrar stock sobre item existente actualiza la variante")
    void registrarStockExistente() {
        StockItem item = StockItem.crear(prendaId, sedeId, TipoPrenda.ROPA, ahora, ahora);
        when(stockRepository.buscarPorPrendaYSede(prendaId, sedeId)).thenReturn(Optional.of(item));
        when(stockRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        RegistrarStockCommand command = new RegistrarStockCommand(
                prendaId, sedeId, TipoPrenda.ROPA, "M", 12, ahora);

        var response = service.ejecutar(command);

        assertThat(response.stockDisponible()).isEqualTo(12);
        assertThat(item.obtenerVariante(TallaRopa.M).getStockDisponible().valor()).isEqualTo(12);
    }

    @Test
    @DisplayName("Consultar prenda no encontrada lanza ResourceNotFoundException")
    void consultarPrendaInexistente() {
        when(stockRepository.buscarPorPrenda(prendaId)).thenReturn(List.of());

        assertThatThrownBy(() -> service.consultarPorPrenda(prendaId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No se encontró inventario");
    }

    @Test
    @DisplayName("Consultar prenda con sedes inactivas retorna estado general SUSPENDIDO")
    void consultarPrendaSuspendida() {
        StockItem item = StockItem.crear(prendaId, sedeId, TipoPrenda.ROPA, ahora, ahora);
        item.suspenderPorMora();
        when(stockRepository.buscarPorPrenda(prendaId)).thenReturn(List.of(item));

        DisponibilidadPrendaResponseDTO response = service.consultarPorPrenda(prendaId);

        assertThat(response.estadoGeneral()).isEqualTo(EstadoItem.SUSPENDIDO);
        assertThat(response.stockDisponibleTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Consultar prenda con stock cero calcula estado general AGOTADA")
    void consultarPrendaAgotada() {
        StockItem item = StockItem.crear(prendaId, sedeId, TipoPrenda.ROPA, ahora, ahora);
        item.registrarTalla(TallaRopa.S, Cantidad.cero(), ahora);
        when(stockRepository.buscarPorPrenda(prendaId)).thenReturn(List.of(item));

        DisponibilidadPrendaResponseDTO response = service.consultarPorPrenda(prendaId);

        assertThat(response.estadoGeneral()).isEqualTo(EstadoItem.AGOTADA);
    }

    @Test
    @DisplayName("Consultar prenda con stock <= umbral total calcula ULTIMAS_UNIDADES")
    void consultarPrendaUltimasUnidades() {
        StockItem item = StockItem.crear(prendaId, sedeId, TipoPrenda.ROPA, ahora.minus(Duration.ofDays(20)), ahora);
        item.registrarTalla(TallaRopa.S, Cantidad.de(2), ahora);
        when(stockRepository.buscarPorPrenda(prendaId)).thenReturn(List.of(item));

        DisponibilidadPrendaResponseDTO response = service.consultarPorPrenda(prendaId);

        assertThat(response.estadoGeneral()).isEqualTo(EstadoItem.ULTIMAS_UNIDADES);
    }

    @Test
    @DisplayName("Operaciones de reserva sobre item inexistente lanzan ResourceNotFoundException")
    void operacionesReservaInexistente() {
        when(stockRepository.buscarPorPrendaYSede(prendaId, sedeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reservar(new ReservarStockCommand(prendaId, sedeId, "M", 1, UUID.randomUUID())))
                .isInstanceOf(ResourceNotFoundException.class);

        assertThatThrownBy(() -> service.confirmar(new ConfirmarReservaCommand(prendaId, sedeId, "M", 1)))
                .isInstanceOf(ResourceNotFoundException.class);

        assertThatThrownBy(() -> service.liberar(new LiberarReservaCommand(prendaId, sedeId, "M", 1)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
