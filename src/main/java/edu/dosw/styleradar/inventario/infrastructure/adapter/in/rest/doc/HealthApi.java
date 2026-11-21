package edu.dosw.styleradar.inventario.infrastructure.adapter.in.rest.doc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@Tag(name = "Estado", description = "Verificación del estado de ms-inventario")
public interface HealthApi {

    @Operation(summary = "Consultar el estado del servicio",
            description = "Indica si ms-inventario está en ejecución y responde en el puerto 8200.")
    @ApiResponse(responseCode = "200", description = "Servicio en ejecución",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"status\":\"UP\",\"service\":\"ms-inventario\",\"port\":8200}")))
    @ApiResponse(responseCode = "500", description = "Error interno del servicio (ERROR_INTERNO)",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Error interno\",\"status\":500,\"detail\":\"Ocurrió un error inesperado\",\"instance\":\"/api/v1/health\",\"code\":\"ERROR_INTERNO\",\"timestamp\":\"2026-11-20T22:58:00Z\"}")))
    ResponseEntity<Map<String, Object>> health();
}
