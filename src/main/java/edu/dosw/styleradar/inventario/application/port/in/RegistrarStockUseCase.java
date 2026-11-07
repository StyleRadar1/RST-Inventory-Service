package edu.dosw.styleradar.inventario.application.port.in;

import edu.dosw.styleradar.inventario.application.dto.RegistrarStockCommand;
import edu.dosw.styleradar.inventario.application.dto.StockItemResponseDTO;

public interface RegistrarStockUseCase {

    StockItemResponseDTO ejecutar(RegistrarStockCommand command);
}
