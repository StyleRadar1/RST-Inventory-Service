package edu.dosw.styleradar.inventario.domain.exception;

public class CatalogoNoDisponibleException extends DomainException {

    public CatalogoNoDisponibleException(String message) {
        super(CodigoError.CATALOGO_NO_DISPONIBLE, message);
    }

    public CatalogoNoDisponibleException(String message, Throwable cause) {
        super(CodigoError.CATALOGO_NO_DISPONIBLE, message, cause);
    }
}
