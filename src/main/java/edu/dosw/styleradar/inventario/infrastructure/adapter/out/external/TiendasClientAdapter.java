package edu.dosw.styleradar.inventario.infrastructure.adapter.out.external;

import feign.FeignException;
import edu.dosw.styleradar.inventario.domain.exception.TiendasNoDisponibleException;
import edu.dosw.styleradar.inventario.domain.model.IdSede;
import edu.dosw.styleradar.inventario.domain.port.out.TiendasPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class TiendasClientAdapter implements TiendasPort {

    private static final Logger log = LoggerFactory.getLogger(TiendasClientAdapter.class);

    private final TiendasFeignClient client;

    public TiendasClientAdapter(TiendasFeignClient client) {
        this.client = client;
    }

    @Override
    public Optional<UUID> almacenDelUsuario(Long usuarioId, String roles) {
        try {
            AlmacenDelUsuarioDto almacen = client.almacenDelUsuario(usuarioId, usuarioId, roles);
            return Optional.ofNullable(almacen).map(AlmacenDelUsuarioDto::nit)
                    .filter(nit -> !nit.isBlank()).map(IdSede::desdeTexto);
        } catch (FeignException.NotFound e) {
            return Optional.empty();
        } catch (FeignException e) {
            // Sin respuesta de ms-tiendas no se puede saber de quién es el stock: no se escribe
            log.warn("No se pudo consultar en ms-tiendas el almacen del usuario {}: {}", usuarioId, e.status());
            throw new TiendasNoDisponibleException("No fue posible verificar tu tienda en este momento", e);
        }
    }
}
