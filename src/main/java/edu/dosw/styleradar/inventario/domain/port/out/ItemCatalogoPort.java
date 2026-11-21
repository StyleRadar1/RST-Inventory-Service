package edu.dosw.styleradar.inventario.domain.port.out;

import java.util.Optional;
import java.util.UUID;

public interface ItemCatalogoPort {

    Optional<PrendaCatalogoInfo> obtenerPrendaPorId(UUID prendaId);
}
