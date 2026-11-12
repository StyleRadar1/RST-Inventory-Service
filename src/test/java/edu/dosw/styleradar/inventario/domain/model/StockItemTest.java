package edu.dosw.styleradar.inventario.domain.model;

import edu.dosw.styleradar.inventario.domain.event.DomainEvent;
import edu.dosw.styleradar.inventario.domain.event.StockActualizadoEvent;
import edu.dosw.styleradar.inventario.domain.event.StockAgotadoEvent;
import edu.dosw.styleradar.inventario.domain.event.StockBajoEvent;
import edu.dosw.styleradar.inventario.domain.exception.CantidadInvalidaException;
import edu.dosw.styleradar.inventario.domain.exception.InventarioSuspendidoException;
import edu.dosw.styleradar.inventario.domain.exception.StockInsuficienteException;
import edu.dosw.styleradar.inventario.domain.exception.TallaConUnidadesException;
import edu.dosw.styleradar.inventario.domain.exception.TallaInvalidaException;
import edu.dosw.styleradar.inventario.domain.exception.TallaNoRegistradaException;
import edu.dosw.styleradar.inventario.domain.valueobject.Cantidad;
import edu.dosw.styleradar.inventario.domain.valueobject.TallaCalzado;
import edu.dosw.styleradar.inventario.domain.valueobject.TallaRopa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StockItemTest {

    private static final Instant FECHA_PUBLICACION = Instant.parse("2026-11-01T10:00:00Z");
    private UUID prendaId;
    private UUID sedeId;
    private StockItem stockItem;

    @BeforeEach
    void setUp() {
        prendaId = UUID.randomUUID();
        sedeId = UUID.randomUUID();
        stockItem = StockItem.crear(prendaId, sedeId, TipoPrenda.ROPA, FECHA_PUBLICACION, FECHA_PUBLICACION);
    }

    @Test
    @DisplayName("SR-RF-52: Registro de talla válido vs talla incompatible")
    void registrarTallaValidaVsIncompatible() {
        stockItem.registrarTalla(TallaRopa.M, Cantidad.de(10), FECHA_PUBLICACION);

        assertThat(stockItem.getVariantes()).containsKey("M");
        assertThat(stockItem.obtenerVariante(TallaRopa.M).getStockDisponible().valor()).isEqualTo(10);

        assertThatThrownBy(() -> stockItem.registrarTalla(TallaCalzado.T40, Cantidad.de(5), FECHA_PUBLICACION))
                .isInstanceOf(TallaInvalidaException.class)
                .hasMessageContaining("no es compatible");
    }

    @Test
    @DisplayName("RES-6 / SR-RF-52: Registrar lote de tallas válidas en StockItem")
    void registrarTallasLoteValido() {
        stockItem.registrarTallasLote(Map.of(
                TallaRopa.S, Cantidad.de(5),
                TallaRopa.M, Cantidad.de(10)
        ), FECHA_PUBLICACION);

        assertThat(stockItem.getVariantes()).hasSize(2);
        assertThat(stockItem.obtenerVariante(TallaRopa.S).getStockDisponible().valor()).isEqualTo(5);
        assertThat(stockItem.obtenerVariante(TallaRopa.M).getStockDisponible().valor()).isEqualTo(10);
    }

    @Test
    @DisplayName("SR-RF-59: Cálculo de AGOTADA cuando no hay unidades")
    void calcularEstadoAgotada() {
        stockItem.registrarTalla(TallaRopa.S, Cantidad.cero(), FECHA_PUBLICACION);

        assertThat(stockItem.calcularEstado(FECHA_PUBLICACION.plus(Duration.ofDays(2))))
                .isEqualTo(EstadoItem.AGOTADA);
    }

    @Test
    @DisplayName("SR-RF-59: Cálculo de ULTIMAS_UNIDADES cuando stock <= 3")
    void calcularEstadoUltimasUnidades() {
        stockItem.registrarTalla(TallaRopa.L, Cantidad.de(3), FECHA_PUBLICACION);

        Instant fechaLejana = FECHA_PUBLICACION.plus(Duration.ofDays(20));
        assertThat(stockItem.calcularEstado(fechaLejana)).isEqualTo(EstadoItem.ULTIMAS_UNIDADES);
    }

    @Test
    @DisplayName("SR-RF-60: Cálculo de NUEVA_PRENDA si publicación <= 7 días y stock > 3")
    void calcularEstadoNuevaPrenda() {
        stockItem.registrarTalla(TallaRopa.XL, Cantidad.de(10), FECHA_PUBLICACION);

        Instant cincoDiasDespues = FECHA_PUBLICACION.plus(Duration.ofDays(5));
        assertThat(stockItem.calcularEstado(cincoDiasDespues)).isEqualTo(EstadoItem.NUEVA_PRENDA);

        Instant ochoDiasDespues = FECHA_PUBLICACION.plus(Duration.ofDays(8));
        assertThat(stockItem.calcularEstado(ochoDiasDespues)).isEqualTo(EstadoItem.DISPONIBLE);
    }

    @Test
    @DisplayName("SR-RF-53: Descuento directo y error ante saldo insuficiente")
    void descontarStockYValidarInsuficiente() {
        stockItem.registrarTalla(TallaRopa.M, Cantidad.de(5), FECHA_PUBLICACION);
        stockItem.pullDomainEvents();

        stockItem.descontarStock(TallaRopa.M, Cantidad.de(3), FECHA_PUBLICACION.plusSeconds(60));
        assertThat(stockItem.obtenerVariante(TallaRopa.M).getStockDisponible().valor()).isEqualTo(2);

        assertThatThrownBy(() -> stockItem.descontarStock(TallaRopa.M, Cantidad.de(5), FECHA_PUBLICACION.plusSeconds(120)))
                .isInstanceOf(StockInsuficienteException.class)
                .hasMessageContaining("Stock disponible insuficiente");
    }

    @Test
    @DisplayName("Ciclo completo de reserva: reservar, confirmar y liberar")
    void cicloReservaStock() {
        stockItem.registrarTalla(TallaRopa.M, Cantidad.de(10), FECHA_PUBLICACION);

        stockItem.reservarStock(TallaRopa.M, Cantidad.de(4), FECHA_PUBLICACION.plusSeconds(30));
        InventarioVariante variante = stockItem.obtenerVariante(TallaRopa.M);
        assertThat(variante.getStockDisponible().valor()).isEqualTo(6);
        assertThat(variante.getStockReservado().valor()).isEqualTo(4);

        stockItem.confirmarVenta(TallaRopa.M, Cantidad.de(2), FECHA_PUBLICACION.plusSeconds(60));
        assertThat(variante.getStockReservado().valor()).isEqualTo(2);
        assertThat(variante.getStockDisponible().valor()).isEqualTo(6);

        stockItem.liberarReserva(TallaRopa.M, Cantidad.de(2), FECHA_PUBLICACION.plusSeconds(90));
        assertThat(variante.getStockReservado().valor()).isEqualTo(0);
        assertThat(variante.getStockDisponible().valor()).isEqualTo(8);
    }

    @Test
    @DisplayName("Prohibición de eliminar tallas con existencias")
    void eliminarTallaConUnidadesFalla() {
        stockItem.registrarTalla(TallaRopa.S, Cantidad.de(5), FECHA_PUBLICACION);

        assertThatThrownBy(() -> stockItem.eliminarTalla(TallaRopa.S))
                .isInstanceOf(TallaConUnidadesException.class)
                .hasMessageContaining("tiene unidades en inventario");

        stockItem.actualizarStockFisico(TallaRopa.S, Cantidad.cero(), FECHA_PUBLICACION.plusSeconds(10));
        stockItem.eliminarTalla(TallaRopa.S);

        assertThat(stockItem.getVariantes()).doesNotContainKey("S");
    }

    @Test
    @DisplayName("Suspensión por mora inhabilita operaciones y fija estado SUSPENDIDO")
    void suspensionPorMora() {
        stockItem.registrarTalla(TallaRopa.M, Cantidad.de(5), FECHA_PUBLICACION);
        stockItem.suspenderPorMora();

        assertThat(stockItem.isActivo()).isFalse();
        assertThat(stockItem.calcularEstado(FECHA_PUBLICACION)).isEqualTo(EstadoItem.SUSPENDIDO);

        assertThatThrownBy(() -> stockItem.registrarTalla(TallaRopa.L, Cantidad.de(2), FECHA_PUBLICACION))
                .isInstanceOf(InventarioSuspendidoException.class);

        stockItem.reactivarSuscripcion();
        assertThat(stockItem.isActivo()).isTrue();
        stockItem.actualizarStockFisico(TallaRopa.M, Cantidad.de(4), FECHA_PUBLICACION);
        assertThat(stockItem.obtenerVariante(TallaRopa.M).getStockDisponible().valor()).isEqualTo(4);
    }

    @Test
    @DisplayName("Disparo y extracción de eventos de dominio StockActualizado, StockAgotado y StockBajo")
    void disparoDeEventosDeDominio() {
        stockItem.registrarTalla(TallaRopa.XS, Cantidad.de(2), FECHA_PUBLICACION);
        List<DomainEvent> eventosBajo = stockItem.pullDomainEvents();
        assertThat(eventosBajo).hasSize(2);
        assertThat(eventosBajo).anyMatch(e -> e instanceof StockBajoEvent);
        assertThat(eventosBajo).anyMatch(e -> e instanceof StockActualizadoEvent);
        assertThat(stockItem.pullDomainEvents()).isEmpty();

        stockItem.actualizarStockFisico(TallaRopa.XS, Cantidad.cero(), FECHA_PUBLICACION.plusSeconds(10));
        List<DomainEvent> eventosAgotado = stockItem.pullDomainEvents();
        assertThat(eventosAgotado).hasSize(2);
        assertThat(eventosAgotado).anyMatch(e -> e instanceof StockAgotadoEvent);
        assertThat(eventosAgotado).anyMatch(e -> e instanceof StockActualizadoEvent);
    }

    @Test
    @DisplayName("Validaciones de errores sobre tallas no registradas o argumentos inválidos")
    void erroresOperacionesIncompatibles() {
        assertThatThrownBy(() -> stockItem.obtenerVariante(TallaRopa.XXL))
                .isInstanceOf(TallaNoRegistradaException.class);

        assertThatThrownBy(() -> stockItem.buscarVariante(null))
                .isInstanceOf(TallaInvalidaException.class);

        assertThatThrownBy(() -> new StockItem(UUID.randomUUID(), prendaId, sedeId,
                TipoPrenda.ROPA, FECHA_PUBLICACION, FECHA_PUBLICACION, -1, true))
                .isInstanceOf(CantidadInvalidaException.class);
    }
}
