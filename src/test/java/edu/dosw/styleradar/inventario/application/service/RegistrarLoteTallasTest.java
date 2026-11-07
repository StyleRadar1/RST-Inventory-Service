package edu.dosw.styleradar.inventario.application.service;

import edu.dosw.styleradar.inventario.application.dto.ItemTallaCantidadDTO;
import edu.dosw.styleradar.inventario.application.dto.RegistrarLoteStockCommand;
import edu.dosw.styleradar.inventario.application.port.out.StockEventPublisherPort;
import edu.dosw.styleradar.inventario.application.port.out.StockRepositoryPort;
import edu.dosw.styleradar.inventario.domain.exception.TallaInvalidaException;
import edu.dosw.styleradar.inventario.domain.model.StockItem;
import edu.dosw.styleradar.inventario.domain.model.TipoPrenda;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrarLoteTallasTest {

    @Mock
    private StockRepositoryPort stockRepository;

    @Mock
    private StockEventPublisherPort eventPublisher;

    @InjectMocks
    private InventarioApplicationService service;

    @Test
    @DisplayName("RES-6 / SR-RF-52: Asocia exitosamente lote de tallas textiles a prenda tipo ROPA")
    void asociaLoteTallasRopaExitoso() {
        UUID prendaId = UUID.randomUUID();
        UUID sedeId = UUID.randomUUID();
        Instant ahora = Instant.now();

        RegistrarLoteStockCommand command = new RegistrarLoteStockCommand(
                prendaId, sedeId, TipoPrenda.ROPA,
                List.of(new ItemTallaCantidadDTO("S", 5),
                        new ItemTallaCantidadDTO("M", 10),
                        new ItemTallaCantidadDTO("L", 8)),
                ahora
        );

        when(stockRepository.buscarPorPrendaYSede(prendaId, sedeId)).thenReturn(Optional.empty());
        when(stockRepository.guardar(any(StockItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stockRepository.buscarPorPrenda(prendaId)).thenAnswer(inv ->
                List.of(StockItem.crear(prendaId, sedeId, TipoPrenda.ROPA, ahora, ahora)));

        var response = service.registrarLote(command);

        assertThat(response.prendaId()).isEqualTo(prendaId);
        verify(stockRepository).guardar(any(StockItem.class));
    }

    @Test
    @DisplayName("RES-6 / SR-RF-52: Falla con 400 Bad Request si se envían tallas de calzado a prenda de tipo ROPA")
    void rechazaTallaCalzadoEnPrendaRopa() {
        UUID prendaId = UUID.randomUUID();
        UUID sedeId = UUID.randomUUID();

        RegistrarLoteStockCommand commandInvalido = new RegistrarLoteStockCommand(
                prendaId, sedeId, TipoPrenda.ROPA,
                List.of(new ItemTallaCantidadDTO("40", 5)),
                Instant.now()
        );

        assertThatThrownBy(() -> service.registrarLote(commandInvalido))
                .isInstanceOf(TallaInvalidaException.class)
                .hasMessageContaining("no es válida para ropa");
    }
}
