package edu.dosw.styleradar.inventario.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.inventario.domain.exception.CantidadInvalidaException;
import edu.dosw.styleradar.inventario.domain.exception.DomainException;
import edu.dosw.styleradar.inventario.domain.exception.InventarioSuspendidoException;
import edu.dosw.styleradar.inventario.domain.exception.ResourceNotFoundException;
import edu.dosw.styleradar.inventario.domain.exception.StockInsuficienteException;
import edu.dosw.styleradar.inventario.domain.exception.TallaConUnidadesException;
import edu.dosw.styleradar.inventario.domain.exception.TallaInvalidaException;
import edu.dosw.styleradar.inventario.domain.exception.TallaNoRegistradaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para la API REST de inventario según RFC 7807.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StockInsuficienteException.class)
    public ProblemDetail handleStockInsuficiente(StockInsuficienteException ex) {
        return problem(HttpStatus.CONFLICT, "Stock insuficiente", ex.getMessage());
    }

    @ExceptionHandler(TallaInvalidaException.class)
    public ProblemDetail handleTallaInvalida(TallaInvalidaException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Talla inválida", ex.getMessage());
    }

    @ExceptionHandler(CantidadInvalidaException.class)
    public ProblemDetail handleCantidadInvalida(CantidadInvalidaException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Cantidad inválida", ex.getMessage());
    }

    @ExceptionHandler({TallaNoRegistradaException.class, ResourceNotFoundException.class})
    public ProblemDetail handleNotFound(RuntimeException ex) {
        return problem(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage());
    }

    @ExceptionHandler(TallaConUnidadesException.class)
    public ProblemDetail handleTallaConUnidades(TallaConUnidadesException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "No se puede eliminar la talla", ex.getMessage());
    }

    @ExceptionHandler(InventarioSuspendidoException.class)
    public ProblemDetail handleInventarioSuspendido(InventarioSuspendidoException ex) {
        return problem(HttpStatus.FORBIDDEN, "Inventario suspendido", ex.getMessage());
    }

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomain(DomainException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Regla de negocio incumplida", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        ProblemDetail detail = problem(HttpStatus.BAD_REQUEST, "Solicitud inválida", "La solicitud tiene campos inválidos");
        detail.setProperty("errors", errors);
        return detail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", "Ocurrió un error inesperado");
    }

    private ProblemDetail problem(HttpStatus status, String title, String message) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, message);
        detail.setTitle(title);
        return detail;
    }
}
