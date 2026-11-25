package edu.dosw.styleradar.inventario.domain.exception;

public class AccesoDenegadoException extends DomainException {

    public AccesoDenegadoException(String message) {
        super(CodigoError.ACCESO_DENEGADO, message);
    }
}
