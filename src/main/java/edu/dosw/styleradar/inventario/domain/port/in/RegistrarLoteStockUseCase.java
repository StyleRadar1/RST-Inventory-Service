package edu.dosw.styleradar.inventario.domain.port.in;

import edu.dosw.styleradar.inventario.application.dto.DisponibilidadPrendaResponseDTO;
import edu.dosw.styleradar.inventario.application.dto.RegistrarLoteStockCommand;

public interface RegistrarLoteStockUseCase {
    DisponibilidadPrendaResponseDTO registrarLote(RegistrarLoteStockCommand command);
}
