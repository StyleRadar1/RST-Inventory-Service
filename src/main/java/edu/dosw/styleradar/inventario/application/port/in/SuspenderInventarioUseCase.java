package edu.dosw.styleradar.inventario.application.port.in;

import java.util.UUID;

public interface SuspenderInventarioUseCase {

    void suspenderPorSede(UUID sedeId);

    void reactivarPorSede(UUID sedeId);
}
