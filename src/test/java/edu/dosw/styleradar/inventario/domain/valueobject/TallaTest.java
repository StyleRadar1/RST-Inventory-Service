package edu.dosw.styleradar.inventario.domain.valueobject;

import edu.dosw.styleradar.inventario.domain.exception.TallaInvalidaException;
import edu.dosw.styleradar.inventario.domain.model.TipoPrenda;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TallaTest {

    @ParameterizedTest
    @ValueSource(strings = {"XS", "S", "M", "L", "XL", "XXL", "m", "  l  "})
    @DisplayName("Acepta tallas de ropa válidas en mayúsculas o minúsculas")
    void tallasRopaValidas(String valor) {
        Talla talla = Talla.desde(valor, TipoPrenda.ROPA);
        assertThat(talla).isInstanceOf(TallaRopa.class);
        assertThat(talla.getTipoPrenda()).isEqualTo(TipoPrenda.ROPA);
    }

    @ParameterizedTest
    @ValueSource(strings = {"34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45"})
    @DisplayName("Acepta tallas de calzado válidas numéricas")
    void tallasCalzadoValidas(String valor) {
        Talla talla = Talla.desde(valor, TipoPrenda.CALZADO);
        assertThat(talla).isInstanceOf(TallaCalzado.class);
        assertThat(talla.getValor()).isEqualTo(valor);
        assertThat(talla.getTipoPrenda()).isEqualTo(TipoPrenda.CALZADO);
    }

    @Test
    @DisplayName("Rechaza calzado cuando se solicita tipo ROPA")
    void rechazaCalzadoEnRopa() {
        assertThatThrownBy(() -> Talla.desde("40", TipoPrenda.ROPA))
                .isInstanceOf(TallaInvalidaException.class)
                .hasMessageContaining("no es válida para ropa");
    }

    @Test
    @DisplayName("Rechaza letras cuando se solicita tipo CALZADO")
    void rechazaLetrasEnCalzado() {
        assertThatThrownBy(() -> Talla.desde("M", TipoPrenda.CALZADO))
                .isInstanceOf(TallaInvalidaException.class)
                .hasMessageContaining("no es válida para calzado");
    }

    @Test
    @DisplayName("Rechaza tallas vacías, nulas o tipos nulos")
    void rechazaEntradasNulasOVacias() {
        assertThatThrownBy(() -> Talla.desde(null, TipoPrenda.ROPA))
                .isInstanceOf(TallaInvalidaException.class);

        assertThatThrownBy(() -> Talla.desde("   ", TipoPrenda.CALZADO))
                .isInstanceOf(TallaInvalidaException.class);

        assertThatThrownBy(() -> Talla.desde("M", null))
                .isInstanceOf(TallaInvalidaException.class);
    }
}
