package edu.dosw.styleradar.inventario.domain.exception;

public class StockInsuficienteException extends DomainException {

    public StockInsuficienteException(String message) {
        super(CodigoError.STOCK_INSUFICIENTE, message);
    }
}
