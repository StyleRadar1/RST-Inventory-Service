package edu.dosw.styleradar.inventario.domain.model;

import edu.dosw.styleradar.inventario.domain.exception.CantidadInvalidaException;
import edu.dosw.styleradar.inventario.domain.exception.StockInsuficienteException;
import edu.dosw.styleradar.inventario.domain.valueobject.Cantidad;
import edu.dosw.styleradar.inventario.domain.valueobject.Talla;

import java.util.Objects;

/**
 * Entidad interna del agregado StockItem. Representa el stock de una talla
 * concreta separando unidades físicas disponibles de unidades reservadas.
 */
public class InventarioVariante {

    private final Talla talla;
    private Cantidad stockDisponible;
    private Cantidad stockReservado;

    public InventarioVariante(Talla talla) {
        this(talla, Cantidad.cero(), Cantidad.cero());
    }

    public InventarioVariante(Talla talla, Cantidad stockDisponible, Cantidad stockReservado) {
        this.talla = Objects.requireNonNull(talla, "La talla es obligatoria");
        this.stockDisponible = requerir(stockDisponible, "stockDisponible");
        this.stockReservado = requerir(stockReservado, "stockReservado");
    }

    public void fijarStockDisponible(Cantidad cantidad) {
        this.stockDisponible = requerir(cantidad, "stockDisponible");
    }

    public void incrementarStock(Cantidad cantidad) {
        this.stockDisponible = stockDisponible.sumar(requerir(cantidad, "cantidad"));
    }

    public void decrementarStock(Cantidad cantidad) {
        Cantidad solicitada = requerir(cantidad, "cantidad");
        asegurarDisponible(solicitada);
        this.stockDisponible = stockDisponible.restar(solicitada);
    }

    public void reservar(Cantidad cantidad) {
        Cantidad solicitada = requerir(cantidad, "cantidad");
        asegurarDisponible(solicitada);
        this.stockDisponible = stockDisponible.restar(solicitada);
        this.stockReservado = stockReservado.sumar(solicitada);
    }

    public void confirmarReserva(Cantidad cantidad) {
        Cantidad solicitada = requerir(cantidad, "cantidad");
        asegurarReservado(solicitada);
        this.stockReservado = stockReservado.restar(solicitada);
    }

    public void liberarReserva(Cantidad cantidad) {
        Cantidad solicitada = requerir(cantidad, "cantidad");
        asegurarReservado(solicitada);
        this.stockReservado = stockReservado.restar(solicitada);
        this.stockDisponible = stockDisponible.sumar(solicitada);
    }

    public Cantidad getStockTotal() {
        return stockDisponible.sumar(stockReservado);
    }

    public boolean tieneExistencias() {
        return !getStockTotal().esCero();
    }

    public Talla getTalla() {
        return talla;
    }

    public Cantidad getStockDisponible() {
        return stockDisponible;
    }

    public Cantidad getStockReservado() {
        return stockReservado;
    }

    private void asegurarDisponible(Cantidad cantidad) {
        if (!stockDisponible.esMayorOIgualQue(cantidad)) {
            throw new StockInsuficienteException(
                    "Stock disponible insuficiente para la talla " + talla.getValor()
                            + ". Disponible: " + stockDisponible.valor()
                            + ", solicitado: " + cantidad.valor());
        }
    }

    private void asegurarReservado(Cantidad cantidad) {
        if (!stockReservado.esMayorOIgualQue(cantidad)) {
            throw new StockInsuficienteException(
                    "Stock reservado insuficiente para la talla " + talla.getValor()
                            + ". Reservado: " + stockReservado.valor()
                            + ", solicitado: " + cantidad.valor());
        }
    }

    private static Cantidad requerir(Cantidad cantidad, String nombre) {
        if (cantidad == null) {
            throw new CantidadInvalidaException("La cantidad '" + nombre + "' es obligatoria");
        }
        return cantidad;
    }
}
