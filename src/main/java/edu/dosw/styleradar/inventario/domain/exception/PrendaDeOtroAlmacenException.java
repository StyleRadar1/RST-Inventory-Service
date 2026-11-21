package edu.dosw.styleradar.inventario.domain.exception;

public class PrendaDeOtroAlmacenException extends DomainException {

    public PrendaDeOtroAlmacenException(String message) {
        super(CodigoError.PRENDA_DE_OTRO_ALMACEN, message);
    }
}
