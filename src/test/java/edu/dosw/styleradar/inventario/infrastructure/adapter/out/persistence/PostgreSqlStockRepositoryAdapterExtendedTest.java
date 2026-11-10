package edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.inventario.domain.model.StockItem;
import edu.dosw.styleradar.inventario.domain.model.TipoPrenda;
import edu.dosw.styleradar.inventario.domain.valueobject.Cantidad;
import edu.dosw.styleradar.inventario.domain.valueobject.TallaRopa;
import edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence.entity.InventarioVarianteJpaEntity;
import edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence.entity.StockItemJpaEntity;
import edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence.mapper.StockItemEntityMapper;
import edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence.mapper.StockItemEntityMapperImpl;
import edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence.repository.SpringDataStockItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostgreSqlStockRepositoryAdapterExtendedTest {

    @Mock
    private SpringDataStockItemRepository repository;

    @Spy
    private StockItemEntityMapper entityMapper = new StockItemEntityMapperImpl();

    @InjectMocks
    private PostgreSqlStockRepositoryAdapter adapter;

    @Test
    @DisplayName("guardar actualiza variante existente y elimina las no presentes en el dominio")
    void guardarActualizaYRemueveVariantes() {
        UUID prendaId = UUID.randomUUID();
        UUID sedeId = UUID.randomUUID();
        Instant ahora = Instant.now();

        StockItem item = StockItem.crear(prendaId, sedeId, TipoPrenda.ROPA, ahora, ahora);
        item.registrarTalla(TallaRopa.M, Cantidad.de(15), ahora);

        StockItemJpaEntity entidadExistente = new StockItemJpaEntity();
        entidadExistente.setId(item.getId());
        entidadExistente.setPrendaId(prendaId);
        entidadExistente.setSedeId(sedeId);
        entidadExistente.setTipoPrenda(TipoPrenda.ROPA);

        InventarioVarianteJpaEntity varM = new InventarioVarianteJpaEntity();
        varM.setTalla("M");
        varM.setUnidadesDisponibles(5);
        entidadExistente.agregarVariante(varM);

        InventarioVarianteJpaEntity varS = new InventarioVarianteJpaEntity();
        varS.setTalla("S");
        varS.setUnidadesDisponibles(0);
        entidadExistente.agregarVariante(varS);

        when(repository.findById(item.getId())).thenReturn(Optional.of(entidadExistente));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StockItem guardado = adapter.guardar(item);

        assertThat(entidadExistente.getVariantes()).hasSize(1);
        assertThat(entidadExistente.getVariantes().getFirst().getTalla()).isEqualTo("M");
        assertThat(entidadExistente.getVariantes().getFirst().getUnidadesDisponibles()).isEqualTo(15);
        assertThat(guardado.getVariantes()).containsKey("M");
    }
}
