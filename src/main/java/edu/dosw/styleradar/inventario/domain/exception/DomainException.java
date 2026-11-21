package edu.dosw.styleradar.inventario.domain.exception;

import java.util.Objects;

/**
 * Excepción base para todas las reglas de negocio del dominio de inventario.
 * Pertenece al núcleo y no depende de frameworks.
 */
public abstract class DomainException extends RuntimeException {

    private final CodigoError codigoError;

    protected DomainException(CodigoError codigoError, String message) {
        super(message);
        this.codigoError = Objects.requireNonNull(codigoError, "El código de error es obligatorio");
    }

    protected DomainException(CodigoError codigoError, String message, Throwable cause) {
        super(message, cause);
        this.codigoError = Objects.requireNonNull(codigoError, "El código de error es obligatorio");
    }

    protected DomainException(String message) {
        this(CodigoError.REGLA_NEGOCIO_INCUMPLIDA, message);
    }

    public CodigoError getCodigoError() {
        return codigoError;
    }
}
