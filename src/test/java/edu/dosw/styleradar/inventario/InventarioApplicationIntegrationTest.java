package edu.dosw.styleradar.inventario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class InventarioApplicationIntegrationTest {

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @Test
    @DisplayName("El contexto de Spring Boot arranca limpiamente con Flyway y base de datos H2")
    void contextLoads() {
        assertThat(rabbitTemplate).isNotNull();
    }
}
