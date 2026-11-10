package edu.dosw.styleradar.inventario.application.usecase;

import edu.dosw.styleradar.inventario.domain.model.StockItem;
import edu.dosw.styleradar.inventario.domain.model.TipoPrenda;
import edu.dosw.styleradar.inventario.domain.port.out.StockRepositoryPort;
import edu.dosw.styleradar.inventario.domain.valueobject.Cantidad;
import edu.dosw.styleradar.inventario.domain.valueobject.TallaRopa;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FiltrarPrendasPorTallaTest {

    @Mock
    private StockRepositoryPort stockRepository;

    @InjectMocks
    private FiltrarPrendasPorTallaUseCaseImpl useCase;

    @Test
    @DisplayName("RES-24 / SR-RF-10: Retorna IDs de prendas que tienen existencias disponibles en la talla seleccionada")
    void filtraPrendasDisponiblesPorTalla() {
        UUID prenda1 = UUID.randomUUID();
        UUID prenda2 = UUID.randomUUID();
        UUID sedeId = UUID.randomUUID();
        Instant ahora = Instant.now();

        StockItem item1 = StockItem.crear(prenda1, sedeId, TipoPrenda.ROPA, ahora, ahora);
        item1.registrarTalla(TallaRopa.M, Cantidad.de(5), ahora);

        StockItem item2 = StockItem.crear(prenda2, sedeId, TipoPrenda.ROPA, ahora, ahora);
        item2.registrarTalla(TallaRopa.M, Cantidad.de(12), ahora);

        when(stockRepository.buscarPrendasDisponiblesPorTalla("M", sedeId))
                .thenReturn(List.of(item1, item2));

        List<UUID> resultado = useCase.filtrarPrendasDisponiblesPorTalla("M", sedeId);

        assertThat(resultado).hasSize(2).containsExactlyInAnyOrder(prenda1, prenda2);
    }

    @Test
    @DisplayName("RES-24 / SR-RF-10: Retorna lista vacía ante talla nula o en blanco")
    void retornaVacioAnteTallaInvalida() {
        assertThat(useCase.filtrarPrendasDisponiblesPorTalla(null, null)).isEmpty();
        assertThat(useCase.filtrarPrendasDisponiblesPorTalla("   ", null)).isEmpty();
    }
}
