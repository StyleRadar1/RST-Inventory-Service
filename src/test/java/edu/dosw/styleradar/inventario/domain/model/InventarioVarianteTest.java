package edu.dosw.styleradar.inventario.domain.model;

import edu.dosw.styleradar.inventario.domain.exception.CantidadInvalidaException;
import edu.dosw.styleradar.inventario.domain.exception.StockInsuficienteException;
import edu.dosw.styleradar.inventario.domain.valueobject.Cantidad;
import edu.dosw.styleradar.inventario.domain.valueobject.TallaRopa;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventarioVarianteTest {

    @Test
    @DisplayName("Constructor por defecto y getters")
    void constructorPorDefecto() {
        InventarioVariante variante = new InventarioVariante(TallaRopa.M);

        assertThat(variante.getTalla()).isEqualTo(TallaRopa.M);
        assertThat(variante.getStockDisponible()).isEqualTo(Cantidad.cero());
        assertThat(variante.getStockReservado()).isEqualTo(Cantidad.cero());
        assertThat(variante.getStockTotal()).isEqualTo(Cantidad.cero());
        assertThat(variante.tieneExistencias()).isFalse();
    }

    @Test
    @DisplayName("Incrementar y fijar stock disponible")
    void incrementarYFijarStock() {
        InventarioVariante variante = new InventarioVariante(TallaRopa.S, Cantidad.de(5), Cantidad.cero());
        variante.incrementarStock(Cantidad.de(3));

        assertThat(variante.getStockDisponible().valor()).isEqualTo(8);
        assertThat(variante.tieneExistencias()).isTrue();

        variante.fijarStockDisponible(Cantidad.de(2));
        assertThat(variante.getStockDisponible().valor()).isEqualTo(2);
    }

    @Test
    @DisplayName("Decrementar stock y validar saldo insuficiente")
    void decrementarStock() {
        InventarioVariante variante = new InventarioVariante(TallaRopa.L, Cantidad.de(5), Cantidad.cero());
        variante.decrementarStock(Cantidad.de(3));
        assertThat(variante.getStockDisponible().valor()).isEqualTo(2);

        assertThatThrownBy(() -> variante.decrementarStock(Cantidad.de(4)))
                .isInstanceOf(StockInsuficienteException.class)
                .hasMessageContaining("Stock disponible insuficiente");
    }

    @Test
    @DisplayName("Ciclo de reserva: reservar, confirmar y liberar con validación de saldo")
    void cicloReserva() {
        InventarioVariante variante = new InventarioVariante(TallaRopa.M, Cantidad.de(10), Cantidad.cero());

        variante.reservar(Cantidad.de(4));
        assertThat(variante.getStockDisponible().valor()).isEqualTo(6);
        assertThat(variante.getStockReservado().valor()).isEqualTo(4);

        assertThatThrownBy(() -> variante.reservar(Cantidad.de(7)))
                .isInstanceOf(StockInsuficienteException.class);

        variante.confirmarReserva(Cantidad.de(2));
        assertThat(variante.getStockReservado().valor()).isEqualTo(2);

        assertThatThrownBy(() -> variante.confirmarReserva(Cantidad.de(5)))
                .isInstanceOf(StockInsuficienteException.class)
                .hasMessageContaining("Stock reservado insuficiente");

        variante.liberarReserva(Cantidad.de(2));
        assertThat(variante.getStockReservado().valor()).isEqualTo(0);
        assertThat(variante.getStockDisponible().valor()).isEqualTo(8);

        assertThatThrownBy(() -> variante.liberarReserva(Cantidad.de(1)))
                .isInstanceOf(StockInsuficienteException.class);
    }

    @Test
    @DisplayName("Validación de argumentos nulos lanza excepciones de dominio")
    void argumentosNulos() {
        assertThatThrownBy(() -> new InventarioVariante(null))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new InventarioVariante(TallaRopa.M, null, Cantidad.cero()))
                .isInstanceOf(CantidadInvalidaException.class);

        InventarioVariante variante = new InventarioVariante(TallaRopa.M);
        assertThatThrownBy(() -> variante.fijarStockDisponible(null))
                .isInstanceOf(CantidadInvalidaException.class);
        assertThatThrownBy(() -> variante.incrementarStock(null))
                .isInstanceOf(CantidadInvalidaException.class);
        assertThatThrownBy(() -> variante.decrementarStock(null))
                .isInstanceOf(CantidadInvalidaException.class);
        assertThatThrownBy(() -> variante.reservar(null))
                .isInstanceOf(CantidadInvalidaException.class);
        assertThatThrownBy(() -> variante.confirmarReserva(null))
                .isInstanceOf(CantidadInvalidaException.class);
        assertThatThrownBy(() -> variante.liberarReserva(null))
                .isInstanceOf(CantidadInvalidaException.class);
    }
}
