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

@Tag(name = "Health", description = "Service health check for ms-inventario")
public interface HealthApi {

    @Operation(summary = "Check service health status",
            description = "Indicates whether ms-inventario is running and responding on port 8200.")
    @ApiResponse(responseCode = "200", description = "Service is running",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"status\":\"UP\",\"service\":\"ms-inventario\",\"port\":8200}")))
    @ApiResponse(responseCode = "500", description = "Internal service error (ERROR_INTERNO)",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Internal error\",\"status\":500,\"detail\":\"An unexpected error occurred\",\"instance\":\"/api/v1/health\",\"code\":\"ERROR_INTERNO\",\"timestamp\":\"2026-11-20T22:58:00Z\"}")))
    ResponseEntity<Map<String, Object>> health();
}
