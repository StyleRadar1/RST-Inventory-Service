package edu.dosw.styleradar.inventario.domain.valueobject;

import edu.dosw.styleradar.inventario.domain.exception.TallaInvalidaException;
import edu.dosw.styleradar.inventario.domain.model.TipoPrenda;

/**
 * Tallas numéricas válidas para calzado (34 a 45).
 */
public enum TallaCalzado implements Talla {
    T34("34"),
    T35("35"),
    T36("36"),
    T37("37"),
    T38("38"),
    T39("39"),
    T40("40"),
    T41("41"),
    T42("42"),
    T43("43"),
    T44("44"),
    T45("45");

    private final String valor;

    TallaCalzado(String valor) {
        this.valor = valor;
    }

    @Override
    public String getValor() {
        return valor;
    }

    @Override
    public TipoPrenda getTipoPrenda() {
        return TipoPrenda.CALZADO;
    }

    public static TallaCalzado desde(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new TallaInvalidaException("La talla de calzado es obligatoria");
        }
        String normalizada = valor.trim();
        for (TallaCalzado talla : values()) {
            if (talla.valor.equals(normalizada)) {
                return talla;
            }
        }
        throw new TallaInvalidaException(
                "La talla '" + valor + "' no es válida para calzado. Valores permitidos: 34 a 45");
    }
}
