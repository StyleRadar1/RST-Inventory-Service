package edu.dosw.styleradar.inventario.application.port.in;

import edu.dosw.styleradar.inventario.application.dto.DisponibilidadPrendaResponseDTO;

import java.util.UUID;

public interface ConsultarStockUseCase {

    DisponibilidadPrendaResponseDTO consultarPorPrenda(UUID prendaId);
}
