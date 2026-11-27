package edu.dosw.styleradar.inventario.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.inventario.domain.exception.AccesoDenegadoException;
import edu.dosw.styleradar.inventario.domain.exception.CantidadInvalidaException;
import edu.dosw.styleradar.inventario.domain.exception.CatalogoNoDisponibleException;
import edu.dosw.styleradar.inventario.domain.exception.CodigoError;
import edu.dosw.styleradar.inventario.domain.exception.DomainException;
import edu.dosw.styleradar.inventario.domain.exception.InventarioSuspendidoException;
import edu.dosw.styleradar.inventario.domain.exception.NoAutenticadoException;
import edu.dosw.styleradar.inventario.domain.exception.PrendaDeOtroAlmacenException;
import edu.dosw.styleradar.inventario.domain.exception.PrendaNoEncontradaEnCatalogoException;
import edu.dosw.styleradar.inventario.domain.exception.ResourceNotFoundException;
import edu.dosw.styleradar.inventario.domain.exception.StockInsuficienteException;
import edu.dosw.styleradar.inventario.domain.exception.TallaConUnidadesException;
import edu.dosw.styleradar.inventario.domain.exception.TallaInvalidaException;
import edu.dosw.styleradar.inventario.domain.exception.TallaNoRegistradaException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para la API REST de inventario según RFC 7807,
 * incluyendo las propiedades estándar {@code code} y {@code timestamp}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StockInsuficienteException.class)
    public ProblemDetail handleStockInsuficiente(StockInsuficienteException ex) {
        return problem(HttpStatus.CONFLICT, ex.getCodigoError(), "Stock insuficiente", ex.getMessage());
    }

    @ExceptionHandler(TallaInvalidaException.class)
    public ProblemDetail handleTallaInvalida(TallaInvalidaException ex) {
        return problem(HttpStatus.BAD_REQUEST, ex.getCodigoError(), "Talla inválida", ex.getMessage());
    }

    @ExceptionHandler(CantidadInvalidaException.class)
    public ProblemDetail handleCantidadInvalida(CantidadInvalidaException ex) {
        return problem(HttpStatus.BAD_REQUEST, ex.getCodigoError(), "Cantidad inválida", ex.getMessage());
    }

    @ExceptionHandler({
            TallaNoRegistradaException.class,
            ResourceNotFoundException.class,
            PrendaNoEncontradaEnCatalogoException.class
    })
    public ProblemDetail handleNotFound(DomainException ex) {
        return problem(HttpStatus.NOT_FOUND, ex.getCodigoError(), "Recurso no encontrado", ex.getMessage());
    }

    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ProblemDetail handleRouteNotFound(Exception ex) {
        return problem(HttpStatus.NOT_FOUND, CodigoError.RUTA_NO_ENCONTRADA,
                "Ruta no encontrada", "El recurso o ruta solicitada no existe");
    }

    @ExceptionHandler(TallaConUnidadesException.class)
    public ProblemDetail handleTallaConUnidades(TallaConUnidadesException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, ex.getCodigoError(),
                "No se puede eliminar la talla", ex.getMessage());
    }

    @ExceptionHandler(InventarioSuspendidoException.class)
    public ProblemDetail handleInventarioSuspendido(InventarioSuspendidoException ex) {
        return problem(HttpStatus.FORBIDDEN, ex.getCodigoError(), "Inventario suspendido", ex.getMessage());
    }

    @ExceptionHandler(PrendaDeOtroAlmacenException.class)
    public ProblemDetail handlePrendaDeOtroAlmacen(PrendaDeOtroAlmacenException ex) {
        return problem(HttpStatus.FORBIDDEN, ex.getCodigoError(),
                "Prenda no pertenece a la sede", ex.getMessage());
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ProblemDetail handleAccesoDenegado(AccesoDenegadoException ex) {
        return problem(HttpStatus.FORBIDDEN, ex.getCodigoError(), "Acceso denegado", ex.getMessage());
    }

    @ExceptionHandler(NoAutenticadoException.class)
    public ProblemDetail handleNoAutenticado(NoAutenticadoException ex) {
        return problem(HttpStatus.UNAUTHORIZED, ex.getCodigoError(), "No autenticado", ex.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return problem(HttpStatus.METHOD_NOT_ALLOWED, CodigoError.METODO_NO_PERMITIDO,
                "Método HTTP no permitido",
                "El método HTTP '" + ex.getMethod() + "' no está permitido para esta ruta");
    }

    @ExceptionHandler(edu.dosw.styleradar.inventario.domain.exception.TiendasNoDisponibleException.class)
    public ProblemDetail handleTiendasNoDisponible(
            edu.dosw.styleradar.inventario.domain.exception.TiendasNoDisponibleException ex) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, ex.getCodigoError(),
                "Servicio de tiendas no disponible", ex.getMessage());
    }

    @ExceptionHandler(CatalogoNoDisponibleException.class)
    public ProblemDetail handleCatalogoNoDisponible(CatalogoNoDisponibleException ex) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, ex.getCodigoError(),
                "Servicio de catálogo no disponible", ex.getMessage());
    }

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomain(DomainException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, ex.getCodigoError(),
                "Regla de negocio incumplida", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        ProblemDetail detail = problem(HttpStatus.BAD_REQUEST, CodigoError.DATOS_INVALIDOS,
                "Solicitud inválida", "La solicitud tiene campos inválidos");
        detail.setProperty("errors", errors);
        return detail;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadableBody(HttpMessageNotReadableException ex) {
        return problem(HttpStatus.BAD_REQUEST, CodigoError.DATOS_INVALIDOS,
                "Cuerpo de solicitud inválido",
                "El formato del JSON o alguno de los valores enviados no es válido");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return problem(HttpStatus.BAD_REQUEST, CodigoError.DATOS_INVALIDOS,
                "Parámetro inválido",
                "El valor del parámetro '" + ex.getName() + "' no tiene el formato esperado");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParam(MissingServletRequestParameterException ex) {
        return problem(HttpStatus.BAD_REQUEST, CodigoError.DATOS_INVALIDOS,
                "Parámetro faltante",
                "Falta el parámetro obligatorio '" + ex.getParameterName() + "'");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        return problem(HttpStatus.BAD_REQUEST, CodigoError.DATOS_INVALIDOS,
                "Solicitud inválida", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, CodigoError.ERROR_INTERNO,
                "Error interno", "Ocurrió un error inesperado");
    }

    private ProblemDetail problem(HttpStatus status, CodigoError code, String title, String message) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, message);
        detail.setTitle(title);
        detail.setProperty("code", code.name());
        detail.setProperty("timestamp", Instant.now().toString());
        return detail;
    }
}
