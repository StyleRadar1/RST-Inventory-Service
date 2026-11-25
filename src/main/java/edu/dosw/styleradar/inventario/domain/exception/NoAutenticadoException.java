package edu.dosw.styleradar.inventario.domain.exception;

public class NoAutenticadoException extends DomainException {

    public NoAutenticadoException(String message) {
        super(CodigoError.NO_AUTENTICADO, message);
    }
}
