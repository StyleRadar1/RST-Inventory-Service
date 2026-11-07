package edu.dosw.styleradar.inventario.domain.valueobject;

import edu.dosw.styleradar.inventario.domain.exception.CantidadInvalidaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CantidadTest {

    @Test
    @DisplayName("Creación válida de cantidades no negativas")
    void creacionValida() {
        Cantidad c0 = Cantidad.cero();
        Cantidad c5 = Cantidad.de(5);

        assertThat(c0.valor()).isEqualTo(0);
        assertThat(c0.esCero()).isTrue();
        assertThat(c5.valor()).isEqualTo(5);
        assertThat(c5.esCero()).isFalse();
    }

    @Test
    @DisplayName("Rechazo de cantidad negativa")
    void rechazaCantidadNegativa() {
        assertThatThrownBy(() -> Cantidad.de(-1))
                .isInstanceOf(CantidadInvalidaException.class)
                .hasMessageContaining("no puede ser negativa");
    }

    @Test
    @DisplayName("Operaciones de suma y resta")
    void sumaYResta() {
        Cantidad a = Cantidad.de(10);
        Cantidad b = Cantidad.de(4);

        assertThat(a.sumar(b)).isEqualTo(Cantidad.de(14));
        assertThat(a.restar(b)).isEqualTo(Cantidad.de(6));

        assertThatThrownBy(() -> b.restar(a))
                .isInstanceOf(CantidadInvalidaException.class);
    }

    @Test
    @DisplayName("Comparaciones mayor o igual y menor o igual")
    void comparaciones() {
        Cantidad diez = Cantidad.de(10);
        Cantidad cinco = Cantidad.de(5);

        assertThat(diez.esMayorOIgualQue(cinco)).isTrue();
        assertThat(cinco.esMayorOIgualQue(diez)).isFalse();
        assertThat(cinco.menorOIgualQue(5)).isTrue();
        assertThat(cinco.menorOIgualQue(3)).isFalse();
    }
}
