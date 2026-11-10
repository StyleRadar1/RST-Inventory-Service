package edu.dosw.styleradar.inventario.domain.port.in;

import edu.dosw.styleradar.inventario.application.dto.ConfirmarReservaCommand;
import edu.dosw.styleradar.inventario.application.dto.LiberarReservaCommand;
import edu.dosw.styleradar.inventario.application.dto.ReservaResponseDTO;
import edu.dosw.styleradar.inventario.application.dto.ReservarStockCommand;

public interface GestionarReservaUseCase {
    ReservaResponseDTO reservar(ReservarStockCommand command);
    void confirmar(ConfirmarReservaCommand command);
    void liberar(LiberarReservaCommand command);
}
