package edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.inventario.domain.model.EstadoItem;
import edu.dosw.styleradar.inventario.domain.model.StockItem;
import edu.dosw.styleradar.inventario.domain.model.TipoPrenda;
import edu.dosw.styleradar.inventario.domain.valueobject.Cantidad;
import edu.dosw.styleradar.inventario.domain.valueobject.TallaRopa;
import edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence.entity.InventarioVarianteJpaEntity;
import edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence.entity.StockItemJpaEntity;
import edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence.mapper.StockItemEntityMapper;
import edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence.mapper.StockItemEntityMapperImpl;
import edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence.repository.SpringDataStockItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
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

    @Spy
    private StockItemEntityMapper entityMapper = new StockItemEntityMapperImpl();

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
    @DisplayName("buscarPorId mapea entidad a dominio correctamente con MapStruct")
    void buscarPorIdMapeaDominio() {
        StockItemJpaEntity entity = new StockItemJpaEntity();
        entity.setId(itemId);
        entity.setPrendaId(prendaId);
        entity.setSedeId(sedeId);
        entity.setTipoPrenda(TipoPrenda.ROPA);
        entity.setEstado(EstadoItem.DISPONIBLE);
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
        entity.setEstado(EstadoItem.DISPONIBLE);
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
    @DisplayName("buscarPorPrendaYSedeConBloqueo, buscarDisponiblesPorSede y buscarPrendasDisponiblesPorTalla")
    void consultasDeBloqueoYDisponibilidad() {
        StockItemJpaEntity activoConStock = new StockItemJpaEntity();
        activoConStock.setId(itemId);
        activoConStock.setPrendaId(prendaId);
        activoConStock.setSedeId(sedeId);
        activoConStock.setTipoPrenda(TipoPrenda.ROPA);
        activoConStock.setEstado(EstadoItem.DISPONIBLE);
        activoConStock.setUmbralUltimasUnidades(3);
        activoConStock.setActivo(true);
        activoConStock.setFechaPublicacion(ahora);
        activoConStock.setFechaActualizacion(ahora);

        InventarioVarianteJpaEntity varM = new InventarioVarianteJpaEntity();
        varM.setId(UUID.randomUUID());
        varM.setTalla("M");
        varM.setUnidadesDisponibles(4);
        varM.setUnidadesReservadas(0);
        activoConStock.agregarVariante(varM);

        StockItemJpaEntity suspendido = new StockItemJpaEntity();
        suspendido.setId(UUID.randomUUID());
        suspendido.setPrendaId(UUID.randomUUID());
        suspendido.setSedeId(sedeId);
        suspendido.setTipoPrenda(TipoPrenda.ROPA);
        suspendido.setEstado(EstadoItem.SUSPENDIDO);
        suspendido.setUmbralUltimasUnidades(3);
        suspendido.setActivo(false);
        suspendido.setFechaPublicacion(ahora);
        suspendido.setFechaActualizacion(ahora);

        when(repository.buscarPorPrendaYSedeConBloqueo(prendaId, sedeId)).thenReturn(Optional.of(activoConStock));
        when(repository.findBySedeId(sedeId)).thenReturn(List.of(activoConStock, suspendido));
        when(repository.buscarPrendasDisponiblesPorTalla("M", sedeId)).thenReturn(List.of(activoConStock));

        assertThat(adapter.buscarPorPrendaYSedeConBloqueo(prendaId, sedeId)).isPresent();
        assertThat(adapter.buscarDisponiblesPorSede(sedeId)).hasSize(1);
        assertThat(adapter.buscarPrendasDisponiblesPorTalla("M", sedeId)).hasSize(1);
    }

    @Test
    @DisplayName("guardar sincroniza cabecera y variantes a través de MapStruct")
    void guardarSincroniza() {
        StockItem item = StockItem.crear(prendaId, sedeId, TipoPrenda.ROPA, ahora, ahora);
        item.registrarTalla(TallaRopa.M, Cantidad.de(10), ahora);

        when(repository.findById(item.getId())).thenReturn(Optional.empty());
        when(repository.save(any(StockItemJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        StockItem guardado = adapter.guardar(item);

        verify(repository).save(any(StockItemJpaEntity.class));
        assertThat(guardado.getId()).isEqualTo(item.getId());
    }
}
