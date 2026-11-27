package edu.dosw.styleradar.inventario.domain.port.out;

import java.util.Optional;
import java.util.UUID;

public interface TiendasPort {

    /** Id del almacén que administra el usuario; vacío si no tiene ninguno. */
    Optional<UUID> almacenDelUsuario(Long usuarioId, String roles);
}
