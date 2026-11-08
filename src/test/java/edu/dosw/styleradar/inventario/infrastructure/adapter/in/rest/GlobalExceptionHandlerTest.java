package edu.dosw.styleradar.inventario.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.inventario.domain.exception.CantidadInvalidaException;
import edu.dosw.styleradar.inventario.domain.exception.DomainException;
import edu.dosw.styleradar.inventario.domain.exception.InventarioSuspendidoException;
import edu.dosw.styleradar.inventario.domain.exception.ResourceNotFoundException;
import edu.dosw.styleradar.inventario.domain.exception.StockInsuficienteException;
import edu.dosw.styleradar.inventario.domain.exception.TallaConUnidadesException;
import edu.dosw.styleradar.inventario.domain.exception.TallaInvalidaException;
import edu.dosw.styleradar.inventario.domain.exception.TallaNoRegistradaException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapeaStockInsuficiente() {
        ProblemDetail detail = handler.handleStockInsuficiente(new StockInsuficienteException("Sin stock"));
        assertThat(detail.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(detail.getTitle()).isEqualTo("Stock insuficiente");
    }

    @Test
    void mapeaTallaInvalida() {
        ProblemDetail detail = handler.handleTallaInvalida(new TallaInvalidaException("Talla"));
        assertThat(detail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void mapeaCantidadInvalida() {
        ProblemDetail detail = handler.handleCantidadInvalida(new CantidadInvalidaException("Negativo"));
        assertThat(detail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void mapeaNotFound() {
        ProblemDetail d1 = handler.handleNotFound(new TallaNoRegistradaException("No existe"));
        assertThat(d1.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());

        ProblemDetail d2 = handler.handleNotFound(new ResourceNotFoundException("Recurso"));
        assertThat(d2.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void mapeaTallaConUnidades() {
        ProblemDetail detail = handler.handleTallaConUnidades(new TallaConUnidadesException("Con unidades"));
        assertThat(detail.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }

    @Test
    void mapeaInventarioSuspendido() {
        ProblemDetail detail = handler.handleInventarioSuspendido(new InventarioSuspendidoException("Mora"));
        assertThat(detail.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void mapeaDomainExceptionGenerica() {
        ProblemDetail detail = handler.handleDomain(new DomainException("Generica") {});
        assertThat(detail.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }

    @Test
    void mapeaValidacion() {
        BeanPropertyBindingResult result = new BeanPropertyBindingResult(new Object(), "target");
        result.addError(new FieldError("target", "talla", "es obligatoria"));
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(result);

        ProblemDetail detail = handler.handleValidation(ex);
        assertThat(detail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(detail.getProperties()).containsEntry("errors", Map.of("talla", "es obligatoria"));
    }

    @Test
    void mapeaInesperado() {
        ProblemDetail detail = handler.handleUnexpected(new RuntimeException("Crash"));
        assertThat(detail.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}
