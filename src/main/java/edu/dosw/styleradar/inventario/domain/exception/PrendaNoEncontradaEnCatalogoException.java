package edu.dosw.styleradar.inventario.domain.exception;

public class PrendaNoEncontradaEnCatalogoException extends DomainException {

    public PrendaNoEncontradaEnCatalogoException(String message) {
        super(CodigoError.PRENDA_NO_ENCONTRADA, message);
    }
}
