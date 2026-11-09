package edu.dosw.styleradar.inventario.application.mapper;

import edu.dosw.styleradar.inventario.application.dto.StockItemResponseDTO;
import edu.dosw.styleradar.inventario.domain.model.EstadoItem;
import edu.dosw.styleradar.inventario.domain.model.StockItem;
import org.mapstruct.Mapper;

import java.time.Instant;

@Mapper(componentModel = "spring")
public interface StockItemDtoMapper {

    default StockItemResponseDTO toResponse(StockItem stockItem, String talla, int stockDisponible,
                                            int stockReservado, EstadoItem estado, Instant fechaActualizacion) {
        if (stockItem == null) {
            return null;
        }
        return new StockItemResponseDTO(
                stockItem.getId(),
                stockItem.getPrendaId(),
                stockItem.getSedeId(),
                stockItem.getTipoPrenda(),
                talla,
                stockDisponible,
                stockReservado,
                estado,
                stockItem.isActivo(),
                fechaActualizacion
        );
    }
}
