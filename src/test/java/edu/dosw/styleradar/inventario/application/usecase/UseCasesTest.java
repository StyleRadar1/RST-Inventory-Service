package edu.dosw.styleradar.inventario.application.usecase;

import edu.dosw.styleradar.inventario.application.dto.ActualizarDisponibilidadDTO;
import edu.dosw.styleradar.inventario.application.dto.AjustarStockCommand;
import edu.dosw.styleradar.inventario.application.dto.ConfirmarReservaCommand;
import edu.dosw.styleradar.inventario.application.dto.DisponibilidadPrendaResponseDTO;
import edu.dosw.styleradar.inventario.application.dto.ItemTallaCantidadDTO;
import edu.dosw.styleradar.inventario.application.dto.LiberarReservaCommand;
import edu.dosw.styleradar.inventario.application.dto.RegistrarLoteStockCommand;
import edu.dosw.styleradar.inventario.application.dto.RegistrarStockCommand;
import edu.dosw.styleradar.inventario.application.dto.ReservaResponseDTO;
import edu.dosw.styleradar.inventario.application.dto.ReservarStockCommand;
import edu.dosw.styleradar.inventario.application.dto.StockItemResponseDTO;
import edu.dosw.styleradar.inventario.application.dto.VerificacionDisponibilidadDTO;
import edu.dosw.styleradar.inventario.application.mapper.StockItemDtoMapper;
import edu.dosw.styleradar.inventario.application.mapper.StockItemDtoMapperImpl;
import edu.dosw.styleradar.inventario.domain.event.StockAgotadoEvent;
import edu.dosw.styleradar.inventario.domain.exception.ResourceNotFoundException;
import edu.dosw.styleradar.inventario.domain.model.EstadoItem;
import edu.dosw.styleradar.inventario.domain.model.StockItem;
import edu.dosw.styleradar.inventario.domain.model.TipoPrenda;
import edu.dosw.styleradar.inventario.domain.port.out.StockEventPublisherPort;
import edu.dosw.styleradar.inventario.domain.port.out.StockRepositoryPort;
import edu.dosw.styleradar.inventario.domain.valueobject.Cantidad;
import edu.dosw.styleradar.inventario.domain.valueobject.TallaRopa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UseCasesTest {

    @Mock
    private StockRepositoryPort stockRepository;

    @Mock
    private StockEventPublisherPort eventPublisher;

    @Spy
    private StockItemDtoMapper dtoMapper = new StockItemDtoMapperImpl();

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
    @DisplayName("RegistrarStockUseCase registra stock y publica evento si stock es cero")
    void registrarStockNuevo() {
        RegistrarStockUseCaseImpl useCase = new RegistrarStockUseCaseImpl(stockRepository, eventPublisher, dtoMapper);
        RegistrarStockCommand command = new RegistrarStockCommand(prendaId, sedeId, TipoPrenda.ROPA, "M", 0, ahora);

        when(stockRepository.buscarPorPrendaYSede(prendaId, sedeId)).thenReturn(Optional.empty());
        when(stockRepository.guardar(any(StockItem.class))).thenAnswer(inv -> inv.getArgument(0));

        StockItemResponseDTO response = useCase.ejecutar(command);

        assertThat(response.stockDisponible()).isEqualTo(0);
        assertThat(response.estado()).isEqualTo(EstadoItem.AGOTADA);
        verify(eventPublisher).publicar(any(StockAgotadoEvent.class));
    }

    @Test
    @DisplayName("AjustarStockUseCase actualiza unidades físicamente")
    void ajustarStockExistente() {
        AjustarStockUseCaseImpl useCase = new AjustarStockUseCaseImpl(stockRepository, eventPublisher, dtoMapper);
        StockItem item = StockItem.crear(prendaId, sedeId, TipoPrenda.ROPA, ahora, ahora);
        item.registrarTalla(TallaRopa.M, Cantidad.de(5), ahora);

        when(stockRepository.buscarPorId(item.getId())).thenReturn(Optional.of(item));
        when(stockRepository.guardar(any(StockItem.class))).thenAnswer(inv -> inv.getArgument(0));

        StockItemResponseDTO response = useCase.ejecutar(new AjustarStockCommand(item.getId(), "M", 12));
        assertThat(response.stockDisponible()).isEqualTo(12);

        when(stockRepository.buscarPorPrendaYSede(prendaId, sedeId)).thenReturn(Optional.of(item));
        StockItemResponseDTO resp2 = useCase.actualizarPorPrendaYSede(prendaId, sedeId, "M", 20);
        assertThat(resp2.stockDisponible()).isEqualTo(20);
    }

    @Test
    @DisplayName("GestionarReservaUseCase ciclo completo de reserva, confirmación y liberación")
    void gestionarReservaCiclo() {
        GestionarReservaUseCaseImpl useCase = new GestionarReservaUseCaseImpl(stockRepository, eventPublisher);
        StockItem item = StockItem.crear(prendaId, sedeId, TipoPrenda.ROPA, ahora, ahora);
        item.registrarTalla(TallaRopa.M, Cantidad.de(10), ahora);

        when(stockRepository.buscarPorPrendaYSede(prendaId, sedeId)).thenReturn(Optional.of(item));
        when(stockRepository.guardar(any(StockItem.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservaResponseDTO reserva = useCase.reservar(new ReservarStockCommand(prendaId, sedeId, "M", 2, UUID.randomUUID()));
        assertThat(reserva.reservaExitosa()).isTrue();
        assertThat(reserva.unidadesReservadas()).isEqualTo(2);

        useCase.confirmar(new ConfirmarReservaCommand(prendaId, sedeId, "M", 1));
        assertThat(item.obtenerVariante(TallaRopa.M).getStockReservado().valor()).isEqualTo(1);

        useCase.liberar(new LiberarReservaCommand(prendaId, sedeId, "M", 1));
        assertThat(item.obtenerVariante(TallaRopa.M).getStockReservado().valor()).isEqualTo(0);
        assertThat(item.obtenerVariante(TallaRopa.M).getStockDisponible().valor()).isEqualTo(9);
    }

    @Test
    @DisplayName("ConsultarStockUseCase verifica disponibilidad y lista sedes")
    void consultarStockVerificaciones() {
        ConsultarStockUseCaseImpl useCase = new ConsultarStockUseCaseImpl(stockRepository);
        StockItem item = StockItem.crear(prendaId, sedeId, TipoPrenda.ROPA, ahora, ahora);
        item.registrarTalla(TallaRopa.M, Cantidad.de(5), ahora);

        when(stockRepository.buscarPorPrenda(prendaId)).thenReturn(List.of(item));

        DisponibilidadPrendaResponseDTO resp = useCase.consultarPorPrenda(prendaId);
        assertThat(resp.stockDisponibleTotal()).isEqualTo(5);

        VerificacionDisponibilidadDTO verif = useCase.verificarDisponibilidad(prendaId);
        assertThat(verif.disponibleParaVenta()).isTrue();

        when(stockRepository.buscarDisponiblesPorSede(sedeId)).thenReturn(List.of(item));
        List<UUID> disponibles = useCase.listarPrendasDisponiblesPorSede(sedeId);
        assertThat(disponibles).contains(prendaId);
    }

    @Test
    @DisplayName("SuspenderInventarioUseCase suspende y reactiva por sede")
    void suspenderYReactivar() {
        SuspenderInventarioUseCaseImpl useCase = new SuspenderInventarioUseCaseImpl(stockRepository, eventPublisher);
        StockItem item = StockItem.crear(prendaId, sedeId, TipoPrenda.ROPA, ahora, ahora);

        when(stockRepository.buscarPorSede(sedeId)).thenReturn(List.of(item));
        when(stockRepository.guardar(any(StockItem.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.suspenderPorSede(sedeId);
        assertThat(item.isActivo()).isFalse();

        useCase.reactivarPorSede(sedeId);
        assertThat(item.isActivo()).isTrue();
    }
}
