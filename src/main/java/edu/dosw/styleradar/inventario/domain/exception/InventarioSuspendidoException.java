package edu.dosw.styleradar.inventario.domain.exception;

public class InventarioSuspendidoException extends DomainException {

    public InventarioSuspendidoException(String message) {
        super(CodigoError.INVENTARIO_SUSPENDIDO, message);
    }
}
