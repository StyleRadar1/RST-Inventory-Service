package edu.dosw.styleradar.inventario.domain.model;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class IdSedeTest {

    @Test
    void laSedeSeIdentificaPorUuidOPorNit() {
        UUID id = UUID.randomUUID();
        UUID desdeNit = UUID.nameUUIDFromBytes("ms-tiendas:almacen:900555777-1".getBytes(StandardCharsets.UTF_8));

        assertThat(IdSede.desdeTexto(" " + id + " ")).isEqualTo(id);
        assertThat(IdSede.desdeTexto("900555777-1")).isEqualTo(desdeNit);
        assertThat(IdSede.desdeTexto(" ")).isNull();
        assertThat(IdSede.desdeTexto(null)).isNull();
    }
}
