package edu.dosw.styleradar.inventario.domain.valueobject;

import edu.dosw.styleradar.inventario.domain.exception.TallaInvalidaException;
import edu.dosw.styleradar.inventario.domain.model.TipoPrenda;

/**
 * Tallas válidas para prendas de vestir.
 */
public enum TallaRopa implements Talla {
    XS,
    S,
    M,
    L,
    XL,
    XXL;

    @Override
    public String getValor() {
        return name();
    }

    @Override
    public TipoPrenda getTipoPrenda() {
        return TipoPrenda.ROPA;
    }

    public static TallaRopa desde(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new TallaInvalidaException("La talla de ropa es obligatoria");
        }
        String normalizada = valor.trim().toUpperCase(java.util.Locale.ROOT);
        for (TallaRopa talla : values()) {
            if (talla.name().equals(normalizada)) {
                return talla;
            }
        }
        throw new TallaInvalidaException(
                "La talla '" + valor + "' no es válida para ropa. Valores permitidos: XS, S, M, L, XL, XXL");
    }
}
