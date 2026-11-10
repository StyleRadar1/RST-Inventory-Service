package edu.dosw.styleradar.inventario.application.mapper;

import edu.dosw.styleradar.inventario.application.dto.StockItemResponseDTO;
import edu.dosw.styleradar.inventario.domain.model.EstadoItem;
import edu.dosw.styleradar.inventario.domain.model.StockItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;

@Mapper(componentModel = "spring")
public interface StockItemDtoMapper {

    @Mapping(target = "id", source = "stockItem.id")
    @Mapping(target = "prendaId", source = "stockItem.prendaId")
    @Mapping(target = "sedeId", source = "stockItem.sedeId")
    @Mapping(target = "tipoPrenda", source = "stockItem.tipoPrenda")
    @Mapping(target = "talla", source = "talla")
    @Mapping(target = "stockDisponible", source = "stockDisponible")
    @Mapping(target = "stockReservado", source = "stockReservado")
    @Mapping(target = "estado", source = "estado")
    @Mapping(target = "activo", source = "stockItem.activo")
    @Mapping(target = "fechaActualizacion", source = "fechaActualizacion")
    StockItemResponseDTO toResponse(StockItem stockItem, String talla, int stockDisponible,
                                    int stockReservado, EstadoItem estado, Instant fechaActualizacion);
}
