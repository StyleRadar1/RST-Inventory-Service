package edu.dosw.styleradar.inventario.application.port.in;

import edu.dosw.styleradar.inventario.application.dto.DisponibilidadPrendaResponseDTO;
import edu.dosw.styleradar.inventario.application.dto.VerificacionDisponibilidadDTO;

import java.util.List;
import java.util.UUID;

public interface ConsultarStockUseCase {

    DisponibilidadPrendaResponseDTO consultarPorPrenda(UUID prendaId);

    VerificacionDisponibilidadDTO verificarDisponibilidad(UUID prendaId);

    List<UUID> listarPrendasDisponiblesPorSede(UUID sedeId);
}
