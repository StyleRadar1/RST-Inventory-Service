package edu.dosw.styleradar.inventario.application.usecase;

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
import edu.dosw.styleradar.inventario.domain.event.StockActualizadoEvent;
import edu.dosw.styleradar.inventario.domain.event.StockAgotadoEvent;
import edu.dosw.styleradar.inventario.domain.exception.PrendaDeOtroAlmacenException;
import edu.dosw.styleradar.inventario.domain.exception.PrendaNoEncontradaEnCatalogoException;
import edu.dosw.styleradar.inventario.domain.exception.ResourceNotFoundException;
import edu.dosw.styleradar.inventario.domain.model.EstadoItem;
import edu.dosw.styleradar.inventario.domain.model.StockItem;
import edu.dosw.styleradar.inventario.domain.model.TipoPrenda;
import edu.dosw.styleradar.inventario.domain.port.in.ConsultarStockUseCase;
import edu.dosw.styleradar.inventario.domain.port.out.ItemCatalogoPort;
import edu.dosw.styleradar.inventario.domain.port.out.PrendaCatalogoInfo;
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
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UseCasesTest {

    @Mock
    private StockRepositoryPort stockRepository;

    @Mock
    private StockEventPublisherPort eventPublisher;

    @Mock
    private ItemCatalogoPort itemCatalogoPort;

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
    @DisplayName("RegistrarLoteStockUseCase valida prenda y almacén en catálogo antes de registrar")
    void registrarLoteStockValido() {
        ConsultarStockUseCase consultarUseCase = new ConsultarStockUseCaseImpl(stockRepository);
        RegistrarLoteStockUseCaseImpl useCase = new RegistrarLoteStockUseCaseImpl(
                stockRepository, eventPublisher, consultarUseCase, itemCatalogoPort);

        RegistrarLoteStockCommand command = new RegistrarLoteStockCommand(
                prendaId, sedeId, TipoPrenda.ROPA,
                List.of(new ItemTallaCantidadDTO("S", 4), new ItemTallaCantidadDTO("M", 6)),
                ahora);

        AtomicReference<StockItem> guardadoRef = new AtomicReference<>();
        when(itemCatalogoPort.obtenerPrendaPorId(prendaId))
                .thenReturn(Optional.of(new PrendaCatalogoInfo(prendaId, sedeId)));
        when(stockRepository.buscarPorPrendaYSede(prendaId, sedeId)).thenReturn(Optional.empty());
        when(stockRepository.guardar(any(StockItem.class))).thenAnswer(inv -> {
            StockItem guardado = inv.getArgument(0);
            guardadoRef.set(guardado);
            return guardado;
        });
        when(stockRepository.buscarPorPrenda(prendaId)).thenAnswer(inv -> List.of(guardadoRef.get()));

        DisponibilidadPrendaResponseDTO response = useCase.registrarLote(command);

        assertThat(response.prendaId()).isEqualTo(prendaId);
        assertThat(response.stockDisponibleTotal()).isEqualTo(10);
        assertThat(response.sedes()).hasSize(1);
        assertThat(response.sedes().getFirst().variantes()).hasSize(2);
        verify(eventPublisher, times(2)).publicar(any(StockActualizadoEvent.class));
    }

    @Test
    @DisplayName("RegistrarLoteStockUseCase falla con 404 si prenda no existe en catálogo o 403 si pertenece a otra sede")
    void registrarLoteStockValidacionesCatalogo() {
        ConsultarStockUseCase consultarUseCase = new ConsultarStockUseCaseImpl(stockRepository);
        RegistrarLoteStockUseCaseImpl useCase = new RegistrarLoteStockUseCaseImpl(
                stockRepository, eventPublisher, consultarUseCase, itemCatalogoPort);

        RegistrarLoteStockCommand command = new RegistrarLoteStockCommand(
                prendaId, sedeId, TipoPrenda.ROPA,
                List.of(new ItemTallaCantidadDTO("M", 5)),
                ahora);

        when(itemCatalogoPort.obtenerPrendaPorId(prendaId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.registrarLote(command))
                .isInstanceOf(PrendaNoEncontradaEnCatalogoException.class);

        when(itemCatalogoPort.obtenerPrendaPorId(prendaId))
                .thenReturn(Optional.of(new PrendaCatalogoInfo(prendaId, UUID.randomUUID())));
        assertThatThrownBy(() -> useCase.registrarLote(command))
                .isInstanceOf(PrendaDeOtroAlmacenException.class);

        when(itemCatalogoPort.obtenerPrendaPorId(prendaId))
                .thenReturn(Optional.of(new PrendaCatalogoInfo(prendaId, null)));
        assertThatThrownBy(() -> useCase.registrarLote(command))
                .isInstanceOf(PrendaDeOtroAlmacenException.class);

        verify(stockRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("AjustarStockUseCase actualiza unidades físicamente y lanza 404 si no existe")
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

        UUID idInexistente = UUID.randomUUID();
        when(stockRepository.buscarPorId(idInexistente)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.ejecutar(new AjustarStockCommand(idInexistente, "M", 5)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("GestionarReservaUseCase ciclo completo con bloqueo pesimista")
    void gestionarReservaCiclo() {
        GestionarReservaUseCaseImpl useCase = new GestionarReservaUseCaseImpl(stockRepository, eventPublisher);
        StockItem item = StockItem.crear(prendaId, sedeId, TipoPrenda.ROPA, ahora, ahora);
        item.registrarTalla(TallaRopa.M, Cantidad.de(10), ahora);

        when(stockRepository.buscarPorPrendaYSedeConBloqueo(prendaId, sedeId)).thenReturn(Optional.of(item));
        when(stockRepository.guardar(any(StockItem.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservaResponseDTO reserva = useCase.reservar(new ReservarStockCommand(prendaId, sedeId, "M", 2, UUID.randomUUID()));
        assertThat(reserva.reservaExitosa()).isTrue();
        assertThat(reserva.unidadesReservadas()).isEqualTo(2);

        useCase.confirmar(new ConfirmarReservaCommand(prendaId, sedeId, "M", 1));
        assertThat(item.obtenerVariante(TallaRopa.M).getStockReservado().valor()).isEqualTo(1);

        useCase.liberar(new LiberarReservaCommand(prendaId, sedeId, "M", 1));
        assertThat(item.obtenerVariante(TallaRopa.M).getStockReservado().valor()).isEqualTo(0);
        assertThat(item.obtenerVariante(TallaRopa.M).getStockDisponible().valor()).isEqualTo(9);

        UUID otraPrenda = UUID.randomUUID();
        when(stockRepository.buscarPorPrendaYSedeConBloqueo(otraPrenda, sedeId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.reservar(new ReservarStockCommand(otraPrenda, sedeId, "M", 1, UUID.randomUUID())))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("ConsultarStockUseCase verifica disponibilidad, sede suspendida y lista sedes")
    void consultarStockVerificaciones() {
        ConsultarStockUseCaseImpl useCase = new ConsultarStockUseCaseImpl(stockRepository);
        StockItem item = StockItem.crear(prendaId, sedeId, TipoPrenda.ROPA, ahora, ahora);
        item.registrarTalla(TallaRopa.M, Cantidad.de(5), ahora);

        when(stockRepository.buscarPorPrenda(prendaId)).thenReturn(List.of(item));

        DisponibilidadPrendaResponseDTO resp = useCase.consultarPorPrenda(prendaId);
        assertThat(resp.stockDisponibleTotal()).isEqualTo(5);

        VerificacionDisponibilidadDTO verif = useCase.verificarDisponibilidad(prendaId);
        assertThat(verif.disponibleParaVenta()).isTrue();

        item.suspenderPorMora();
        VerificacionDisponibilidadDTO verifSuspendida = useCase.verificarDisponibilidad(prendaId);
        assertThat(verifSuspendida.disponibleParaVenta()).isFalse();
        assertThat(verifSuspendida.estado()).isEqualTo(EstadoItem.SUSPENDIDO);

        when(stockRepository.buscarDisponiblesPorSede(sedeId)).thenReturn(List.of(item));
        List<UUID> disponibles = useCase.listarPrendasDisponiblesPorSede(sedeId);
        assertThat(disponibles).contains(prendaId);

        UUID otraPrenda = UUID.randomUUID();
        when(stockRepository.buscarPorPrenda(otraPrenda)).thenReturn(List.of());
        assertThatThrownBy(() -> useCase.consultarPorPrenda(otraPrenda))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> useCase.verificarDisponibilidad(otraPrenda))
                .isInstanceOf(ResourceNotFoundException.class);
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
