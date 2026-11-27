package edu.dosw.styleradar.inventario.application.usecase;

import edu.dosw.styleradar.inventario.domain.exception.AccesoDenegadoException;
import edu.dosw.styleradar.inventario.domain.port.out.StockRepositoryPort;
import edu.dosw.styleradar.inventario.domain.port.out.TiendasPort;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class VerificarPropiedadStockTest {

    private final TiendasPort tiendas = mock(TiendasPort.class);
    private final StockRepositoryPort stock = mock(StockRepositoryPort.class);
    private final VerificarPropiedadStock propiedad = new VerificarPropiedadStock(tiendas, stock, true);
    private final UUID propia = UUID.randomUUID();

    @Test
    void permiteLaPropiaYRechazaLaAjena() {
        when(tiendas.almacenDelUsuario(7L, "STORE_ADMIN")).thenReturn(Optional.of(propia));

        assertThatCode(() -> propiedad.exigirSedePropia("7", "STORE_ADMIN", propia)).doesNotThrowAnyException();
        assertThatThrownBy(() -> propiedad.exigirSedePropia("7", "STORE_ADMIN", UUID.randomUUID()))
                .isInstanceOf(AccesoDenegadoException.class);
    }

    @Test
    void rechazaSinUsuarioOSinTienda() {
        when(tiendas.almacenDelUsuario(8L, "STORE_ADMIN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> propiedad.exigirSedePropia(null, "STORE_ADMIN", propia))
                .isInstanceOf(AccesoDenegadoException.class);
        assertThatThrownBy(() -> propiedad.exigirSedePropia("abc", "STORE_ADMIN", propia))
                .isInstanceOf(AccesoDenegadoException.class);
        assertThatThrownBy(() -> propiedad.exigirSedePropia("8", "STORE_ADMIN", propia))
                .isInstanceOf(AccesoDenegadoException.class);
    }

    @Test
    void unRegistroInexistenteLoResuelveElCasoDeUso() {
        UUID item = UUID.randomUUID();
        when(stock.buscarPorId(item)).thenReturn(Optional.empty());

        assertThatCode(() -> propiedad.exigirItemPropio("7", "STORE_ADMIN", item)).doesNotThrowAnyException();
        verifyNoInteractions(tiendas);
    }

    @Test
    void apagadoNoConsulta() {
        new VerificarPropiedadStock(tiendas, stock, false).exigirSedePropia("7", "STORE_ADMIN", propia);

        verifyNoInteractions(tiendas);
    }
}
