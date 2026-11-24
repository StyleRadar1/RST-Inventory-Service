package edu.dosw.styleradar.inventario.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.inventario.domain.exception.CantidadInvalidaException;
import edu.dosw.styleradar.inventario.domain.exception.CatalogoNoDisponibleException;
import edu.dosw.styleradar.inventario.domain.exception.DomainException;
import edu.dosw.styleradar.inventario.domain.exception.InventarioSuspendidoException;
import edu.dosw.styleradar.inventario.domain.exception.PrendaDeOtroAlmacenException;
import edu.dosw.styleradar.inventario.domain.exception.PrendaNoEncontradaEnCatalogoException;
import edu.dosw.styleradar.inventario.domain.exception.ResourceNotFoundException;
import edu.dosw.styleradar.inventario.domain.exception.StockInsuficienteException;
import edu.dosw.styleradar.inventario.domain.exception.TallaConUnidadesException;
import edu.dosw.styleradar.inventario.domain.exception.TallaInvalidaException;
import edu.dosw.styleradar.inventario.domain.exception.TallaNoRegistradaException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapeaStockInsuficiente() {
        ProblemDetail detail = handler.handleStockInsuficiente(new StockInsuficienteException("Sin stock"));
        assertCodigoYTimestamp(detail, HttpStatus.CONFLICT, "STOCK_INSUFICIENTE");
    }

    @Test
    void mapeaTallaInvalida() {
        ProblemDetail detail = handler.handleTallaInvalida(new TallaInvalidaException("Talla"));
        assertCodigoYTimestamp(detail, HttpStatus.BAD_REQUEST, "TALLA_INVALIDA");
    }

    @Test
    void mapeaCantidadInvalida() {
        ProblemDetail detail = handler.handleCantidadInvalida(new CantidadInvalidaException("Negativo"));
        assertCodigoYTimestamp(detail, HttpStatus.BAD_REQUEST, "CANTIDAD_INVALIDA");
    }

    @Test
    void mapeaNotFound() {
        assertCodigoYTimestamp(handler.handleNotFound(new TallaNoRegistradaException("No existe")),
                HttpStatus.NOT_FOUND, "TALLA_NO_REGISTRADA");

        assertCodigoYTimestamp(handler.handleNotFound(new ResourceNotFoundException("Recurso")),
                HttpStatus.NOT_FOUND, "INVENTARIO_NO_ENCONTRADO");

        assertCodigoYTimestamp(handler.handleNotFound(new PrendaNoEncontradaEnCatalogoException("Prenda")),
                HttpStatus.NOT_FOUND, "PRENDA_NO_ENCONTRADA");
    }

    @Test
    void mapeaRutasInexistentesA404() {
        assertCodigoYTimestamp(handler.handleRouteNotFound(new NoResourceFoundException(HttpMethod.GET, "/api/v1")),
                HttpStatus.NOT_FOUND, "RUTA_NO_ENCONTRADA");

        assertCodigoYTimestamp(handler.handleRouteNotFound(new NoHandlerFoundException("GET", "/no-existe", HttpHeaders.EMPTY)),
                HttpStatus.NOT_FOUND, "RUTA_NO_ENCONTRADA");
    }

    @Test
    void mapeaTallaConUnidades() {
        ProblemDetail detail = handler.handleTallaConUnidades(new TallaConUnidadesException("Con unidades"));
        assertCodigoYTimestamp(detail, HttpStatus.UNPROCESSABLE_ENTITY, "TALLA_CON_UNIDADES");
    }

    @Test
    void mapeaInventarioSuspendidoYPrendaDeOtroAlmacen() {
        assertCodigoYTimestamp(handler.handleInventarioSuspendido(new InventarioSuspendidoException("Mora")),
                HttpStatus.FORBIDDEN, "INVENTARIO_SUSPENDIDO");

        assertCodigoYTimestamp(handler.handlePrendaDeOtroAlmacen(new PrendaDeOtroAlmacenException("Otra sede")),
                HttpStatus.FORBIDDEN, "PRENDA_DE_OTRO_ALMACEN");
    }

    @Test
    void mapeaCatalogoNoDisponible503() {
        ProblemDetail detail = handler.handleCatalogoNoDisponible(new CatalogoNoDisponibleException("Timeout"));
        assertCodigoYTimestamp(detail, HttpStatus.SERVICE_UNAVAILABLE, "CATALOGO_NO_DISPONIBLE");
    }

    @Test
    void mapeaDomainExceptionGenerica() {
        ProblemDetail detail = handler.handleDomain(new DomainException("Generica") {});
        assertCodigoYTimestamp(detail, HttpStatus.UNPROCESSABLE_ENTITY, "REGLA_NEGOCIO_INCUMPLIDA");
    }

    @Test
    void mapeaValidacionYSolicitudInvalida400() {
        BeanPropertyBindingResult result = new BeanPropertyBindingResult(new Object(), "target");
        result.addError(new FieldError("target", "talla", "es obligatoria"));
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(result);

        ProblemDetail detail = handler.handleValidation(ex);
        assertCodigoYTimestamp(detail, HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS");
        assertThat(detail.getProperties()).containsEntry("errors", Map.of("talla", "es obligatoria"));

        HttpMessageNotReadableException unreadable = mock(HttpMessageNotReadableException.class);
        assertCodigoYTimestamp(handler.handleUnreadableBody(unreadable), HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS");

        MethodArgumentTypeMismatchException mismatch = mock(MethodArgumentTypeMismatchException.class);
        when(mismatch.getName()).thenReturn("prendaId");
        assertCodigoYTimestamp(handler.handleTypeMismatch(mismatch), HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS");

        var missing = new MissingServletRequestParameterException("talla", "String");
        assertCodigoYTimestamp(handler.handleMissingParam(missing), HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS");

        var violation = new ConstraintViolationException("Inválido", Set.of());
        assertCodigoYTimestamp(handler.handleConstraintViolation(violation), HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS");
    }

    @Test
    void mapeaInesperado() {
        ProblemDetail detail = handler.handleUnexpected(new RuntimeException("Crash"));
        assertCodigoYTimestamp(detail, HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO");
    }

    private void assertCodigoYTimestamp(ProblemDetail detail, HttpStatus esperado, String codigoEsperado) {
        assertThat(detail.getStatus()).isEqualTo(esperado.value());
        assertThat(detail.getProperties()).containsEntry("code", codigoEsperado);
        assertThat(detail.getProperties()).containsKey("timestamp");
        Instant parsed = Instant.parse(detail.getProperties().get("timestamp").toString());
        assertThat(parsed).isNotNull();
    }
}
