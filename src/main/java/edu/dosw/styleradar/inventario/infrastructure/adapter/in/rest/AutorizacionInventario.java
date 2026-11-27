package edu.dosw.styleradar.inventario.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.inventario.domain.exception.AccesoDenegadoException;
import edu.dosw.styleradar.inventario.domain.exception.NoAutenticadoException;
import edu.dosw.styleradar.inventario.domain.model.IdSede;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

final class AutorizacionInventario {

    static final String HEADER_USER_ID = "X-User-Id";
    static final String HEADER_USER_ROLE = "X-User-Role";
    static final String HEADER_USER_ROLES = "X-User-Roles";
    static final String HEADER_STORE_ID = "X-Store-Id";
    static final String HEADER_ALMACEN_ID = "X-Almacen-Id";

    private static final Set<String> ROLES_ALMACEN_O_ADMIN = Set.of(
            "STORE_ADMIN", "ADMIN_ALMACEN", "ADMIN", "ADMIN_STYLERADAR", "STYLERADAR_ADMIN");

    private AutorizacionInventario() {
    }

    static void exigirRolAlmacenOAdmin(String rol, String roles) {
        if (!tieneRolPermitido(rol, roles)) {
            throw new AccesoDenegadoException("No tienes permisos suficientes para modificar el inventario");
        }
    }

    static void exigirAutenticado(String usuarioId, String rol, String roles) {
        boolean sinUsuario = usuarioId == null || usuarioId.isBlank();
        boolean sinRol = rol == null || rol.isBlank();
        boolean sinRoles = roles == null || roles.isBlank();
        if (sinUsuario && sinRol && sinRoles) {
            throw new NoAutenticadoException("Se requiere autenticación para gestionar reservas de inventario");
        }
    }

    private static final Set<String> ROLES_ADMIN = Set.of("ADMIN", "ADMIN_STYLERADAR", "STYLERADAR_ADMIN");

    static boolean esAdmin(String rol, String roles) {
        return tieneAlguno(ROLES_ADMIN, rol, roles);
    }

    /** Roles tal como se reenvian a ms-tiendas. */
    static String rolesCombinados(String rol, String roles) {
        return roles == null || roles.isBlank() ? rol : roles;
    }

    static UUID resolverSede(UUID branchId, String storeIdParam, String storeIdHeader, String almacenIdHeader) {
        if (branchId != null) {
            return branchId;
        }
        return IdSede.desdeTexto(firstNonBlank(storeIdParam, storeIdHeader, almacenIdHeader));
    }

    private static boolean tieneRolPermitido(String rol, String roles) {
        return tieneAlguno(ROLES_ALMACEN_O_ADMIN, rol, roles);
    }

    private static boolean tieneAlguno(Set<String> permitidos, String rol, String roles) {
        String combinados = (rol == null ? "" : rol) + "," + (roles == null ? "" : roles);
        return Arrays.stream(combinados.split("[,\\s]+"))
                .map(r -> r.trim().toUpperCase(Locale.ROOT))
                .map(r -> r.startsWith("ROLE_") ? r.substring(5) : r)
                .anyMatch(permitidos::contains);
    }

    private static String firstNonBlank(String... valores) {
        for (String valor : valores) {
            if (valor != null && !valor.isBlank()) {
                return valor;
            }
        }
        return null;
    }
}
