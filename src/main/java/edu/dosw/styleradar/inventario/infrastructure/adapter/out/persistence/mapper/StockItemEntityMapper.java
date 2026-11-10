package edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence.mapper;

import edu.dosw.styleradar.inventario.domain.model.InventarioVariante;
import edu.dosw.styleradar.inventario.domain.model.StockItem;
import edu.dosw.styleradar.inventario.domain.valueobject.Cantidad;
import edu.dosw.styleradar.inventario.domain.valueobject.Talla;
import edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence.entity.InventarioVarianteJpaEntity;
import edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence.entity.StockItemJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface StockItemEntityMapper {

    /**
     * Método abstracto generado por MapStruct: mapea y copia los campos de cabecera hacia la entidad JPA.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "variantes", ignore = true)
    @Mapping(target = "estado", expression = "java(domain.calcularEstado(java.time.Instant.now()))")
    void copiarCabecera(StockItem domain, @MappingTarget StockItemJpaEntity entity);

    default StockItem toDomain(StockItemJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        Map<String, InventarioVariante> variantes = new LinkedHashMap<>();
        if (entity.getVariantes() != null) {
            for (InventarioVarianteJpaEntity ve : entity.getVariantes()) {
                Talla talla = Talla.desde(ve.getTalla(), entity.getTipoPrenda());
                variantes.put(talla.getValor(), new InventarioVariante(talla,
                        Cantidad.de(ve.getUnidadesDisponibles()),
                        Cantidad.de(ve.getUnidadesReservadas())));
            }
        }
        return StockItem.reconstruir(entity.getId(), entity.getPrendaId(), entity.getSedeId(),
                entity.getTipoPrenda(), entity.getFechaPublicacion(), entity.getFechaActualizacion(),
                entity.getUmbralUltimasUnidades(), entity.isActivo(), variantes);
    }

    default void actualizarEntidad(StockItemJpaEntity entity, StockItem domain) {
        if (entity == null || domain == null) {
            return;
        }
        // Delegación en el método abstracto generado por MapStruct
        copiarCabecera(domain, entity);

        Map<String, InventarioVariante> variantesDominio = domain.getVariantes();
        Map<String, InventarioVarianteJpaEntity> existentes = new LinkedHashMap<>();
        entity.getVariantes().forEach(v -> existentes.put(v.getTalla(), v));

        entity.getVariantes().removeIf(v -> !variantesDominio.containsKey(v.getTalla()));

        for (Map.Entry<String, InventarioVariante> entrada : variantesDominio.entrySet()) {
            InventarioVariante varianteDominio = entrada.getValue();
            InventarioVarianteJpaEntity varianteEntity = existentes.get(entrada.getKey());
            if (varianteEntity == null) {
                varianteEntity = new InventarioVarianteJpaEntity();
                varianteEntity.setId(idVariante(domain.getId(), entrada.getKey()));
                varianteEntity.setTalla(entrada.getKey());
                entity.agregarVariante(varianteEntity);
            }
            varianteEntity.setUnidadesDisponibles(varianteDominio.getStockDisponible().valor());
            varianteEntity.setUnidadesReservadas(varianteDominio.getStockReservado().valor());
        }
    }

    private static UUID idVariante(UUID stockItemId, String talla) {
        return UUID.nameUUIDFromBytes((stockItemId + ":" + talla).getBytes(StandardCharsets.UTF_8));
    }
}
