package edu.dosw.styleradar.inventario.domain.valueobject;

import edu.dosw.styleradar.inventario.domain.exception.CantidadInvalidaException;

import java.util.Objects;

/**
 * Value Object inmutable que representa una cantidad de unidades de inventario.
 */
public record Cantidad(int valor) {

    public Cantidad {
        if (valor < 0) {
            throw new CantidadInvalidaException("La cantidad no puede ser negativa: " + valor);
        }
    }

    public static Cantidad cero() {
        return new Cantidad(0);
    }

    public static Cantidad de(int valor) {
        return new Cantidad(valor);
    }

    public Cantidad sumar(Cantidad otra) {
        return new Cantidad(valor + requerir(otra).valor);
    }

    public Cantidad restar(Cantidad otra) {
        return new Cantidad(valor - requerir(otra).valor);
    }

    public boolean esMayorOIgualQue(Cantidad otra) {
        return valor >= requerir(otra).valor;
    }

    public boolean esCero() {
        return valor == 0;
    }

    public boolean menorOIgualQue(int otro) {
        return valor <= otro;
    }

    private static Cantidad requerir(Cantidad cantidad) {
        Objects.requireNonNull(cantidad, "La cantidad es obligatoria");
        return cantidad;
    }
}
