package edu.dosw.styleradar.inventario.domain.exception;

public class TiendasNoDisponibleException extends DomainException {

    public TiendasNoDisponibleException(String message, Throwable cause) {
        super(CodigoError.TIENDAS_NO_DISPONIBLE, message, cause);
    }
}
