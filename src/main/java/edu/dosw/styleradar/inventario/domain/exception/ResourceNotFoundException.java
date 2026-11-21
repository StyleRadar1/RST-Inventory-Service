package edu.dosw.styleradar.inventario.domain.exception;

public class ResourceNotFoundException extends DomainException {

    public ResourceNotFoundException(String message) {
        super(CodigoError.INVENTARIO_NO_ENCONTRADO, message);
    }
}
