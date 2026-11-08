package edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.inventario.domain.model.StockItem;
import edu.dosw.styleradar.inventario.domain.model.TipoPrenda;
import edu.dosw.styleradar.inventario.domain.valueobject.Cantidad;
import edu.dosw.styleradar.inventario.domain.valueobject.TallaRopa;
import edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence.entity.InventarioVarianteJpaEntity;
import edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence.entity.StockItemJpaEntity;
import edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence.repository.SpringDataStockItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostgreSqlStockRepositoryAdapterTest {

    @Mock
    private SpringDataStockItemRepository repository;

    @InjectMocks
    private PostgreSqlStockRepositoryAdapter adapter;

    private UUID itemId;
    private UUID prendaId;
    private UUID sedeId;
    private Instant ahora;

    @BeforeEach
    void setUp() {
        itemId = UUID.randomUUID();
        prendaId = UUID.randomUUID();
        sedeId = UUID.randomUUID();
        ahora = Instant.now();
    }

    @Test
    @DisplayName("buscarPorId mapea entidad a dominio correctamente")
    void buscarPorIdMapeaDominio() {
        StockItemJpaEntity entity = new StockItemJpaEntity();
        entity.setId(itemId);
        entity.setPrendaId(prendaId);
        entity.setSedeId(sedeId);
        entity.setTipoPrenda(TipoPrenda.ROPA);
        entity.setEstado(edu.dosw.styleradar.inventario.domain.model.EstadoItem.DISPONIBLE);
        entity.setUmbralUltimasUnidades(3);
        entity.setActivo(true);
        entity.setFechaPublicacion(ahora);
        entity.setFechaActualizacion(ahora);

        InventarioVarianteJpaEntity v = new InventarioVarianteJpaEntity();
        v.setId(UUID.randomUUID());
        v.setTalla("M");
        v.setUnidadesDisponibles(10);
        v.setUnidadesReservadas(2);
        entity.agregarVariante(v);

        when(repository.findById(itemId)).thenReturn(Optional.of(entity));

        Optional<StockItem> resultado = adapter.buscarPorId(itemId);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(itemId);
        assertThat(resultado.get().getVariantes()).containsKey("M");
    }

    @Test
    @DisplayName("buscarPorPrendaYSede y buscarPorPrenda / buscarPorSede")
    void consultasVarias() {
        StockItemJpaEntity entity = new StockItemJpaEntity();
        entity.setId(itemId);
        entity.setPrendaId(prendaId);
        entity.setSedeId(sedeId);
        entity.setTipoPrenda(TipoPrenda.ROPA);
        entity.setEstado(edu.dosw.styleradar.inventario.domain.model.EstadoItem.DISPONIBLE);
        entity.setUmbralUltimasUnidades(3);
        entity.setActivo(true);
        entity.setFechaPublicacion(ahora);
        entity.setFechaActualizacion(ahora);

        when(repository.findByPrendaIdAndSedeId(prendaId, sedeId)).thenReturn(Optional.of(entity));
        when(repository.findByPrendaId(prendaId)).thenReturn(List.of(entity));
        when(repository.findBySedeId(sedeId)).thenReturn(List.of(entity));

        assertThat(adapter.buscarPorPrendaYSede(prendaId, sedeId)).isPresent();
        assertThat(adapter.buscarPorPrenda(prendaId)).hasSize(1);
        assertThat(adapter.buscarPorSede(sedeId)).hasSize(1);
    }

    @Test
    @DisplayName("guardar sincroniza cabecera y variantes")
    void guardarSincroniza() {
        StockItem item = StockItem.crear(prendaId, sedeId, TipoPrenda.ROPA, ahora, ahora);
        item.registrarTalla(TallaRopa.M, Cantidad.de(10), ahora);

        when(repository.findById(item.getId())).thenReturn(Optional.empty());
        when(repository.save(any(StockItemJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        StockItem guardado = adapter.guardar(item);

        ArgumentCaptor<StockItemJpaEntity> captor = ArgumentCaptor.forClass(StockItemJpaEntity.class);
        verify(repository).save(captor.capture());

        assertThat(captor.getValue().getId()).isEqualTo(item.getId());
        assertThat(captor.getValue().getVariantes()).hasSize(1);
        assertThat(guardado.getId()).isEqualTo(item.getId());
    }
}
