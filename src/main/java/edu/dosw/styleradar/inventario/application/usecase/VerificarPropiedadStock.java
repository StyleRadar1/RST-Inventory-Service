package edu.dosw.styleradar.inventario.application.usecase;

import edu.dosw.styleradar.inventario.domain.exception.AccesoDenegadoException;
import edu.dosw.styleradar.inventario.domain.model.StockItem;
import edu.dosw.styleradar.inventario.domain.port.out.StockRepositoryPort;
import edu.dosw.styleradar.inventario.domain.port.out.TiendasPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Una cuenta de almacén solo puede escribir el inventario de su propio almacén (la sede es el almacén).
 * Los administradores de StyleRadar no pasan por aquí.
 */
@Service
public class VerificarPropiedadStock {

    private static final String AJENO = "Solo puedes modificar el inventario de tu propia tienda";

    private final TiendasPort tiendas;
    private final StockRepositoryPort stock;
    private final boolean exigir;

    public VerificarPropiedadStock(TiendasPort tiendas, StockRepositoryPort stock,
                                   @Value("${inventario.propiedad.exigir:true}") boolean exigir) {
        this.tiendas = tiendas;
        this.stock = stock;
        this.exigir = exigir;
    }

    public void exigirSedePropia(String usuarioId, String roles, UUID sedeId) {
        if (!exigir) {
            return;
        }
        Long usuario = numero(usuarioId);
        boolean propia = usuario != null
                && tiendas.almacenDelUsuario(usuario, roles).map(almacen -> almacen.equals(sedeId)).orElse(false);
        if (!propia) {
            throw new AccesoDenegadoException(AJENO);
        }
    }

    /** Si el registro no existe no hay nada que proteger: el caso de uso responde 404. */
    public void exigirItemPropio(String usuarioId, String roles, UUID stockItemId) {
        if (!exigir) {
            return;
        }
        stock.buscarPorId(stockItemId).map(StockItem::getSedeId)
                .ifPresent(sede -> exigirSedePropia(usuarioId, roles, sede));
    }

    private static Long numero(String usuarioId) {
        try {
            return usuarioId == null ? null : Long.valueOf(usuarioId.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
