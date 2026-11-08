package edu.dosw.styleradar.inventario.application.port.in;

import edu.dosw.styleradar.inventario.application.dto.AjustarStockCommand;
import edu.dosw.styleradar.inventario.application.dto.StockItemResponseDTO;

public interface AjustarStockUseCase {

    StockItemResponseDTO ejecutar(AjustarStockCommand command);
}
