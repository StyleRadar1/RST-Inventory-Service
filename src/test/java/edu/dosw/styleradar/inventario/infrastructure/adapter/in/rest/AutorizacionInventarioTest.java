package edu.dosw.styleradar.inventario.infrastructure.adapter.in.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AutorizacionInventarioTest {

    @Test
    @DisplayName("resolverSedeDesdeTexto acepta UUID válido y NIT determinístico y rechaza vacío/nulo")
    void resolverSedeDesdeTexto() {
        UUID uuid = UUID.randomUUID();
        UUID esperadoDesdeNit = UUID.nameUUIDFromBytes(
                "ms-tiendas:almacen:900123456-1".getBytes(StandardCharsets.UTF_8));

        assertThat(AutorizacionInventario.resolverSedeDesdeTexto(uuid.toString())).isEqualTo(uuid);
        assertThat(AutorizacionInventario.resolverSedeDesdeTexto("  900123456-1  ")).isEqualTo(esperadoDesdeNit);

        assertThatThrownBy(() -> AutorizacionInventario.resolverSedeDesdeTexto(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AutorizacionInventario.resolverSedeDesdeTexto("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("resolverSede acepta branchId como NIT o UUID y hace fallback a storeId y headers")
    void resolverSedeConNitUuidYHeaders() {
        UUID uuid = UUID.randomUUID();
        UUID esperadoDesdeNit = UUID.nameUUIDFromBytes(
                "ms-tiendas:almacen:900123456-1".getBytes(StandardCharsets.UTF_8));

        assertThat(AutorizacionInventario.resolverSede("900123456-1", null, null, null))
                .isEqualTo(esperadoDesdeNit);
        assertThat(AutorizacionInventario.resolverSede(uuid.toString(), null, null, null))
                .isEqualTo(uuid);
        assertThat(AutorizacionInventario.resolverSede(null, "900123456-1", null, null))
                .isEqualTo(esperadoDesdeNit);
        assertThat(AutorizacionInventario.resolverSede(null, null, "900123456-1", null))
                .isEqualTo(esperadoDesdeNit);
        assertThat(AutorizacionInventario.resolverSede(null, null, null, uuid.toString()))
                .isEqualTo(uuid);
        assertThat(AutorizacionInventario.resolverSede(null, null, null, null)).isNull();
    }
}
