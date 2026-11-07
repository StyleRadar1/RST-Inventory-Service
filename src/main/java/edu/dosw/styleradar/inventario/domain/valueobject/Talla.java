package edu.dosw.styleradar.inventario.domain.valueobject;

import edu.dosw.styleradar.inventario.domain.model.TipoPrenda;

/**
 * Talla tipada por tipo de prenda. Jerarquía cerrada con Java 21.
 */
public sealed interface Talla permits TallaRopa, TallaCalzado {

    String getValor();

    TipoPrenda getTipoPrenda();

    /**
     * Fábrica única que valida la talla contra el tipo de prenda (SR-RF-52).
     */
    static Talla desde(String valor, TipoPrenda tipoPrenda) {
        if (tipoPrenda == null) {
            throw new edu.dosw.styleradar.inventario.domain.exception.TallaInvalidaException(
                    "El tipo de prenda es obligatorio para interpretar la talla");
        }
        return switch (tipoPrenda) {
            case ROPA -> TallaRopa.desde(valor);
            case CALZADO -> TallaCalzado.desde(valor);
        };
    }
}
