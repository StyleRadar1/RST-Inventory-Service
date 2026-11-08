package edu.dosw.styleradar.inventario.infrastructure.adapter.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dosw.styleradar.inventario.application.dto.AjustarStockCommand;
import edu.dosw.styleradar.inventario.application.dto.ConfirmarReservaCommand;
import edu.dosw.styleradar.inventario.application.dto.DisponibilidadPrendaResponseDTO;
import edu.dosw.styleradar.inventario.application.dto.LiberarReservaCommand;
import edu.dosw.styleradar.inventario.application.dto.RegistrarStockCommand;
import edu.dosw.styleradar.inventario.application.dto.ReservaResponseDTO;
import edu.dosw.styleradar.inventario.application.dto.ReservarStockCommand;
import edu.dosw.styleradar.inventario.application.dto.SedeStockDTO;
import edu.dosw.styleradar.inventario.application.dto.StockItemResponseDTO;
import edu.dosw.styleradar.inventario.application.dto.VarianteStockDTO;
import edu.dosw.styleradar.inventario.application.port.in.AjustarStockUseCase;
import edu.dosw.styleradar.inventario.application.port.in.ConsultarStockUseCase;
import edu.dosw.styleradar.inventario.application.port.in.GestionarReservaUseCase;
import edu.dosw.styleradar.inventario.application.port.in.RegistrarStockUseCase;
import edu.dosw.styleradar.inventario.domain.exception.CantidadInvalidaException;
import edu.dosw.styleradar.inventario.domain.exception.InventarioSuspendidoException;
import edu.dosw.styleradar.inventario.domain.exception.ResourceNotFoundException;
import edu.dosw.styleradar.inventario.domain.exception.StockInsuficienteException;
import edu.dosw.styleradar.inventario.domain.exception.TallaConUnidadesException;
import edu.dosw.styleradar.inventario.domain.exception.TallaInvalidaException;
import edu.dosw.styleradar.inventario.domain.exception.TallaNoRegistradaException;
import edu.dosw.styleradar.inventario.domain.model.EstadoItem;
import edu.dosw.styleradar.inventario.domain.model.TipoPrenda;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventarioRestController.class)
@Import(GlobalExceptionHandler.class)
class InventarioRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegistrarStockUseCase registrarStockUseCase;

    @MockBean
    private AjustarStockUseCase ajustarStockUseCase;

    @MockBean
    private GestionarReservaUseCase gestionarReservaUseCase;

    @MockBean
    private ConsultarStockUseCase consultarStockUseCase;

    @Test
    @DisplayName("GET /api/v1/inventario/health retorna 200 con estado UP")
    void healthCheckExitoso() throws Exception {
        mockMvc.perform(get("/api/v1/inventario/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("ms-inventario"));
    }

    @Test
    @DisplayName("GET /api/v1/inventario/prenda/{prendaId} retorna 200 con desglose")
    void consultarPrendaExitoso() throws Exception {
        UUID prendaId = UUID.randomUUID();
        DisponibilidadPrendaResponseDTO response = new DisponibilidadPrendaResponseDTO(
                prendaId, TipoPrenda.ROPA, EstadoItem.DISPONIBLE, 10,
                List.of(new SedeStockDTO(UUID.randomUUID(), true,
                        List.of(new VarianteStockDTO("M", 10, 0, EstadoItem.DISPONIBLE)))));

        when(consultarStockUseCase.consultarPorPrenda(prendaId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/inventario/prenda/{prendaId}", prendaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prendaId").value(prendaId.toString()))
                .andExpect(jsonPath("$.stockDisponibleTotal").value(10));
    }

    @Test
    @DisplayName("POST /api/v1/inventario registra existencias y retorna 201 Created")
    void registrarStockExitoso() throws Exception {
        UUID prendaId = UUID.randomUUID();
        UUID sedeId = UUID.randomUUID();
        RegistrarStockCommand command = new RegistrarStockCommand(
                prendaId, sedeId, TipoPrenda.ROPA, "M", 10, Instant.now());

        StockItemResponseDTO response = new StockItemResponseDTO(
                UUID.randomUUID(), prendaId, sedeId, TipoPrenda.ROPA, "M", 10, 0,
                EstadoItem.NUEVA_PRENDA, true, Instant.now());

        when(registrarStockUseCase.ejecutar(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/inventario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.talla").value("M"))
                .andExpect(jsonPath("$.stockDisponible").value(10));
    }

    @Test
    @DisplayName("PUT /api/v1/inventario/{id}/ajuste actualiza stock y retorna 200 OK")
    void ajustarStockExitoso() throws Exception {
        UUID itemId = UUID.randomUUID();
        AjustarStockCommand command = new AjustarStockCommand(itemId, "M", 15);
        StockItemResponseDTO response = new StockItemResponseDTO(
                itemId, UUID.randomUUID(), UUID.randomUUID(), TipoPrenda.ROPA, "M", 15, 0,
                EstadoItem.DISPONIBLE, true, Instant.now());

        when(ajustarStockUseCase.ejecutar(any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/inventario/{id}/ajuste", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockDisponible").value(15));
    }

    @Test
    @DisplayName("POST /api/v1/inventario/reserva retorna 200 OK con reserva exitosa")
    void reservarStockExitoso() throws Exception {
        UUID prendaId = UUID.randomUUID();
        UUID sedeId = UUID.randomUUID();
        ReservarStockCommand command = new ReservarStockCommand(prendaId, sedeId, "M", 2, UUID.randomUUID());
        ReservaResponseDTO response = new ReservaResponseDTO(true, prendaId, sedeId, "M", 2, 8, Instant.now());

        when(gestionarReservaUseCase.reservar(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/inventario/reserva")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservaExitosa").value(true))
                .andExpect(jsonPath("$.unidadesReservadas").value(2));
    }

    @Test
    @DisplayName("POST /api/v1/inventario/reserva con saldo insuficiente mapea a 409 Conflict")
    void reservarStockInsuficienteRetorna409() throws Exception {
        UUID prendaId = UUID.randomUUID();
        UUID sedeId = UUID.randomUUID();
        ReservarStockCommand command = new ReservarStockCommand(prendaId, sedeId, "M", 20, UUID.randomUUID());

        when(gestionarReservaUseCase.reservar(any()))
                .thenThrow(new StockInsuficienteException("Stock disponible insuficiente"));

        mockMvc.perform(post("/api/v1/inventario/reserva")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Stock insuficiente"));
    }

    @Test
    @DisplayName("POST /api/v1/inventario/reserva/confirmar retorna 200 OK")
    void confirmarReservaExitoso() throws Exception {
        ConfirmarReservaCommand command = new ConfirmarReservaCommand(UUID.randomUUID(), UUID.randomUUID(), "M", 2);

        mockMvc.perform(post("/api/v1/inventario/reserva/confirmar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk());

        verify(gestionarReservaUseCase).confirmar(any());
    }

    @Test
    @DisplayName("POST /api/v1/inventario/reserva/liberar retorna 200 OK")
    void liberarReservaExitoso() throws Exception {
        LiberarReservaCommand command = new LiberarReservaCommand(UUID.randomUUID(), UUID.randomUUID(), "M", 2);

        mockMvc.perform(post("/api/v1/inventario/reserva/liberar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk());

        verify(gestionarReservaUseCase).liberar(any());
    }

    @Test
    @DisplayName("Validación Bean Validation retorna 400 Bad Request con mapa de errores")
    void validacionCamposRetorna400() throws Exception {
        // Objeto sin prendaId ni sedeId
        RegistrarStockCommand commandInvalido = new RegistrarStockCommand(
                null, null, TipoPrenda.ROPA, "", -1, null);

        mockMvc.perform(post("/api/v1/inventario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commandInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @DisplayName("Talla no registrada o recurso inexistente mapea a 404 Not Found")
    void noEncontradoMapea404() throws Exception {
        UUID itemId = UUID.randomUUID();
        when(ajustarStockUseCase.ejecutar(any()))
                .thenThrow(new ResourceNotFoundException("No se encontró"));

        mockMvc.perform(put("/api/v1/inventario/{id}/ajuste", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"talla\":\"M\",\"nuevaCantidad\":5}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Talla incompatible retorna 400 Bad Request")
    void tallaInvalidaRetorna400() throws Exception {
        when(registrarStockUseCase.ejecutar(any()))
                .thenThrow(new TallaInvalidaException("Talla incompatible"));

        RegistrarStockCommand command = new RegistrarStockCommand(
                UUID.randomUUID(), UUID.randomUUID(), TipoPrenda.ROPA, "40", 5, Instant.now());

        mockMvc.perform(post("/api/v1/inventario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Talla inválida"));
    }

    @Test
    @DisplayName("Eliminación con unidades retorna 422 Unprocessable Entity")
    void tallaConUnidadesRetorna422() throws Exception {
        when(ajustarStockUseCase.ejecutar(any()))
                .thenThrow(new TallaConUnidadesException("Tiene existencias"));

        mockMvc.perform(put("/api/v1/inventario/{id}/ajuste", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"talla\":\"M\",\"nuevaCantidad\":5}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Inventario suspendido retorna 403 Forbidden")
    void inventarioSuspendidoRetorna403() throws Exception {
        when(registrarStockUseCase.ejecutar(any()))
                .thenThrow(new InventarioSuspendidoException("Suspendido por mora"));

        RegistrarStockCommand command = new RegistrarStockCommand(
                UUID.randomUUID(), UUID.randomUUID(), TipoPrenda.ROPA, "M", 5, Instant.now());

        mockMvc.perform(post("/api/v1/inventario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isForbidden());
    }
}
