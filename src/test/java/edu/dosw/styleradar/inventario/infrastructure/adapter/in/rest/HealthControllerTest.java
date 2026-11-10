package edu.dosw.styleradar.inventario.infrastructure.adapter.in.rest;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HealthControllerTest {

    @Test
    void healthReturnsServiceUpOnPort8200() {
        HealthController controller = new HealthController();
        ResponseEntity<Map<String, Object>> response = controller.health();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .containsEntry("status", "UP")
                .containsEntry("service", "ms-inventario")
                .containsEntry("port", 8200);
    }
}
