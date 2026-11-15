package edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence.entity.EventoProcesadoJpaEntity;
import edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence.repository.SpringDataEventoProcesadoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostgreSqlEventoProcesadoAdapterTest {

    @Mock
    private SpringDataEventoProcesadoRepository repository;

    @InjectMocks
    private PostgreSqlEventoProcesadoAdapter adapter;

    @Test
    @DisplayName("Verifica existencia y registra eventos procesados correctamente")
    void verificaYRegistraEventos() {
        when(repository.existsById("evt-1")).thenReturn(true);

        assertThat(adapter.yaFueProcesado("evt-1")).isTrue();
        assertThat(adapter.yaFueProcesado(null)).isFalse();
        assertThat(adapter.yaFueProcesado("   ")).isFalse();

        adapter.registrarProcesado("evt-2", "orden.pago.aprobado");
        verify(repository).save(any(EventoProcesadoJpaEntity.class));

        adapter.registrarProcesado("evt-3", null);
        verify(repository, org.mockito.Mockito.times(2)).save(any(EventoProcesadoJpaEntity.class));

        adapter.registrarProcesado(null, "tipo");
        verify(repository, org.mockito.Mockito.times(2)).save(any());
    }
}
