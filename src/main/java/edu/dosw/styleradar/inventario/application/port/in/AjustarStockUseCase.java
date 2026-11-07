package edu.dosw.styleradar.inventario.application.port.in;

import edu.dosw.styleradar.inventario.application.dto.AjustarStockCommand;
import edu.dosw.styleradar.inventario.application.dto.StockItemResponseDTO;

import java.util.UUID;

public interface AjustarStockUseCase {

    StockItemResponseDTO ejecutar(AjustarStockCommand command);

    StockItemResponseDTO actualizarPorPrendaYSede(UUID prendaId, UUID sedeId, String talla, Integer nuevasUnidades);
}
