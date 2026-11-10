package edu.dosw.styleradar.inventario.domain.port.in;

import java.util.List;
import java.util.UUID;

public interface FiltrarPrendasPorTallaUseCase {

    List<UUID> filtrarPrendasDisponiblesPorTalla(String talla, UUID sedeId);
}
