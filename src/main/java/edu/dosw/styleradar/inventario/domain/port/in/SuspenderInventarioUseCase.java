package edu.dosw.styleradar.inventario.domain.port.in;

import java.util.UUID;

public interface SuspenderInventarioUseCase {
    void suspenderPorSede(UUID sedeId);
    void reactivarPorSede(UUID sedeId);
}
