package edu.dosw.styleradar.inventario.application.service;

import edu.dosw.styleradar.inventario.application.dto.VerificacionDisponibilidadDTO;
import edu.dosw.styleradar.inventario.application.port.out.StockEventPublisherPort;
import edu.dosw.styleradar.inventario.application.port.out.StockRepositoryPort;
import edu.dosw.styleradar.inventario.domain.event.StockAgotadoEvent;
import edu.dosw.styleradar.inventario.domain.model.EstadoItem;
import edu.dosw.styleradar.inventario.domain.model.StockItem;
import edu.dosw.styleradar.inventario.domain.model.TipoPrenda;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OcultarPrendaAgotadaTest {

    @Mock
    private StockRepositoryPort stockRepository;

    @Mock
    private StockEventPublisherPort eventPublisher;

    @InjectMocks
    private InventarioApplicationService service;

    @Test
    @DisplayName("RES-8 / SR-RF-57: Prenda sin unidades en ninguna talla se reporta como no disponible para venta")
    void prendaAgotadaNoDisponibleParaVenta() {
        UUID prendaId = UUID.randomUUID();
        UUID sedeId = UUID.randomUUID();
        Instant ahora = Instant.now();

        StockItem item = StockItem.crear(prendaId, sedeId, TipoPrenda.ROPA, ahora, ahora);
        item.registrarTalla(TallaRopa.M, Cantidad.cero(), ahora);
        item.registrarTalla(TallaRopa.L, Cantidad.cero(), ahora);

        when(stockRepository.buscarPorPrenda(prendaId)).thenReturn(List.of(item));

        VerificacionDisponibilidadDTO verificacion = service.verificarDisponibilidad(prendaId);

        assertThat(verificacion.disponibleParaVenta()).isFalse();
        assertThat(verificacion.estado()).isEqualTo(EstadoItem.AGOTADA);
        assertThat(verificacion.stockDisponibleTotal()).isEqualTo(0);
        assertThat(item.estaAgotada()).isTrue();
    }

    @Test
    @DisplayName("RES-8 / SR-RF-57: Filtro por sede excluye prendas agotadas y retorna solo activas disponibles")
    void listarPrendasDisponiblesExcluyeAgotadas() {
        UUID sedeId = UUID.randomUUID();
        UUID prendaDisponible = UUID.randomUUID();

        StockItem itemDisponible = StockItem.crear(prendaDisponible, sedeId, TipoPrenda.ROPA, Instant.now(), Instant.now());
        itemDisponible.registrarTalla(TallaRopa.M, Cantidad.de(5), Instant.now());

        when(stockRepository.buscarDisponiblesPorSede(sedeId)).thenReturn(List.of(itemDisponible));

        List<UUID> disponibles = service.listarPrendasDisponiblesPorSede(sedeId);

        assertThat(disponibles).hasSize(1);
        assertThat(disponibles.getFirst()).isEqualTo(prendaDisponible);
    }
}
