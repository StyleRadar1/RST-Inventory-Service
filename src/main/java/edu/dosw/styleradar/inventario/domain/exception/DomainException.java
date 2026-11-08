package edu.dosw.styleradar.inventario.domain.exception;

/**
 * Excepción base para todas las reglas de negocio del dominio de inventario.
 * Pertenece al núcleo y no depende de frameworks.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}
