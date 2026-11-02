package edu.dosw.styleradar.inventario.infrastructure.exception;

import edu.dosw.styleradar.inventario.domain.exception.DomainException;
import edu.dosw.styleradar.inventario.domain.exception.ResourceNotFoundException;
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
    void notFoundReturns404() {
        // Arrange
        ResourceNotFoundException ex = new ResourceNotFoundException("No existe");

        // Act
        ProblemDetail detail = handler.handleNotFound(ex);

        // Assert
        assertThat(detail.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(detail.getDetail()).isEqualTo("No existe");
    }

    @Test
    void domainErrorReturns422() {
        // Arrange
        DomainException ex = new DomainException("Regla incumplida");

        // Act
        ProblemDetail detail = handler.handleDomain(ex);

        // Assert
        assertThat(detail.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
        assertThat(detail.getTitle()).isEqualTo("Regla de negocio incumplida");
    }

    @Test
    void validationErrorReturns400WithFields() {
        // Arrange
        BeanPropertyBindingResult result = new BeanPropertyBindingResult(new Object(), "request");
        result.addError(new FieldError("request", "nombre", "no debe estar vacío"));
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(result);

        // Act
        ProblemDetail detail = handler.handleValidation(ex);

        // Assert
        assertThat(detail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(detail.getProperties()).containsEntry("errors", Map.of("nombre", "no debe estar vacío"));
    }

    @Test
    void unexpectedErrorReturns500() {
        // Arrange
        Exception ex = new IllegalStateException("fallo");

        // Act
        ProblemDetail detail = handler.handleUnexpected(ex);

        // Assert
        assertThat(detail.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(detail.getDetail()).isEqualTo("Ocurrió un error inesperado");
    }
}
