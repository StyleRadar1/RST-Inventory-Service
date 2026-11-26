package edu.dosw.styleradar.inventario.domain.model;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class IdSede {

    private IdSede() {
    }

    public static UUID desdeTexto(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String limpio = valor.trim();
        try {
            return UUID.fromString(limpio);
        } catch (IllegalArgumentException e) {
            return UUID.nameUUIDFromBytes(("ms-tiendas:almacen:" + limpio).getBytes(StandardCharsets.UTF_8));
        }
    }
}
