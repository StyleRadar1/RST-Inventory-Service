package edu.dosw.styleradar.inventario.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.inventario.application.dto.ActualizarDisponibilidadDTO;
import edu.dosw.styleradar.inventario.application.dto.AjustarStockCommand;
import edu.dosw.styleradar.inventario.application.dto.ConfirmarReservaCommand;
import edu.dosw.styleradar.inventario.application.dto.DisponibilidadPrendaResponseDTO;
import edu.dosw.styleradar.inventario.application.dto.LiberarReservaCommand;
import edu.dosw.styleradar.inventario.application.dto.RegistrarLoteStockCommand;
import edu.dosw.styleradar.inventario.application.dto.RegistrarStockCommand;
import edu.dosw.styleradar.inventario.application.dto.ReservaResponseDTO;
import edu.dosw.styleradar.inventario.application.dto.ReservarStockCommand;
import edu.dosw.styleradar.inventario.application.dto.StockItemResponseDTO;
import edu.dosw.styleradar.inventario.application.port.in.AjustarStockUseCase;
import edu.dosw.styleradar.inventario.application.port.in.ConsultarStockUseCase;
import edu.dosw.styleradar.inventario.application.port.in.GestionarReservaUseCase;
import edu.dosw.styleradar.inventario.application.port.in.RegistrarLoteStockUseCase;
import edu.dosw.styleradar.inventario.application.port.in.RegistrarStockUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * Adaptador primario REST para la gestión de inventario y reservas de StyleRadar.
 */
@Tag(name = "Inventario", description = "Endpoints para registro, ajuste, reservas y consulta de existencias multisede")
@RestController
@RequestMapping("/api/v1/inventario")
public class InventarioRestController {

    private final RegistrarStockUseCase registrarStockUseCase;
    private final RegistrarLoteStockUseCase registrarLoteStockUseCase;
    private final AjustarStockUseCase ajustarStockUseCase;
    private final GestionarReservaUseCase gestionarReservaUseCase;
    private final ConsultarStockUseCase consultarStockUseCase;

    public InventarioRestController(RegistrarStockUseCase registrarStockUseCase,
                                    RegistrarLoteStockUseCase registrarLoteStockUseCase,
                                    AjustarStockUseCase ajustarStockUseCase,
                                    GestionarReservaUseCase gestionarReservaUseCase,
                                    ConsultarStockUseCase consultarStockUseCase) {
        this.registrarStockUseCase = registrarStockUseCase;
        this.registrarLoteStockUseCase = registrarLoteStockUseCase;
        this.ajustarStockUseCase = ajustarStockUseCase;
        this.gestionarReservaUseCase = gestionarReservaUseCase;
        this.consultarStockUseCase = consultarStockUseCase;
    }

    @Operation(summary = "Health check del microservicio de inventario",
            description = "Retorna el estado operativo del servicio ms-inventario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Servicio activo",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = "{\"status\":\"UP\",\"service\":\"ms-inventario\"}")))
    })
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "ms-inventario"));
    }

    @Operation(summary = "Consultar disponibilidad de una prenda",
            description = "Retorna el stock consolidado y desglosado por sedes y variantes para una prenda")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disponibilidad consultada con éxito",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DisponibilidadPrendaResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Prenda no encontrada en inventario",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/prenda/{prendaId}")
    public ResponseEntity<DisponibilidadPrendaResponseDTO> consultarPorPrenda(
            @Parameter(description = "Identificador UUID de la prenda", required = true)
            @PathVariable UUID prendaId) {
        DisponibilidadPrendaResponseDTO response = consultarStockUseCase.consultarPorPrenda(prendaId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Registrar stock inicial de una talla",
            description = "Registra o actualiza la disponibilidad inicial de una talla para una prenda en una sede física (SR-RF-50 / SR-RF-52)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Stock registrado exitosamente",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = StockItemResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Talla incompatible con tipo de prenda o cantidad negativa",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Inventario de la sede suspendido por mora",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<StockItemResponseDTO> registrarStock(
            @Valid @RequestBody RegistrarStockCommand command) {
        StockItemResponseDTO response = registrarStockUseCase.ejecutar(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Registrar lote de tallas disponibles (SR-RF-52 / RES-6)",
            description = "Permite al administrador asociar el conjunto de tallas y stock inicial ofrecido por su establecimiento")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tallas asociadas exitosamente",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DisponibilidadPrendaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Talla incompatible con el tipo de prenda o datos inválidos (SR-RF-52)",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
            @ApiResponse(responseCode = "403", description = "Inventario suspendido por mora en suscripción",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    @PostMapping("/lote")
    public ResponseEntity<DisponibilidadPrendaResponseDTO> registrarLote(
            @Valid @RequestBody RegistrarLoteStockCommand command) {
        DisponibilidadPrendaResponseDTO response = registrarLoteStockUseCase.registrarLote(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Ajustar stock físico de una talla por ID de item",
            description = "Fija manualmente las unidades disponibles de una variante existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock ajustado exitosamente",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = StockItemResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "Item o talla no encontrada",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    @PutMapping("/{id}/ajuste")
    public ResponseEntity<StockItemResponseDTO> ajustarStock(
            @Parameter(description = "Identificador UUID del StockItem", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody AjustarStockCommand command) {
        AjustarStockCommand comandoAjustado = new AjustarStockCommand(id, command.talla(), command.nuevaCantidad());
        StockItemResponseDTO response = ajustarStockUseCase.ejecutar(comandoAjustado);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Modificar de forma independiente la disponibilidad por talla (SR-RF-56 / RES-7)",
            description = "Permite al administrador modificar directamente las unidades de una talla para una prenda en su sede")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disponibilidad actualizada exitosamente",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = StockItemResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Unidades negativas o talla inválida",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
            @ApiResponse(responseCode = "403", description = "Sede suspendida por mora",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "Prenda, sede o talla no registrada",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    @PatchMapping("/prenda/{prendaId}/sede/{sedeId}/tallas/{talla}")
    public ResponseEntity<StockItemResponseDTO> actualizarDisponibilidadTalla(
            @Parameter(description = "Identificador UUID de la prenda", required = true)
            @PathVariable UUID prendaId,
            @Parameter(description = "Identificador UUID de la sede", required = true)
            @PathVariable UUID sedeId,
            @Parameter(description = "Talla tipificada a modificar", required = true)
            @PathVariable String talla,
            @Valid @RequestBody ActualizarDisponibilidadDTO request) {
        StockItemResponseDTO response = ajustarStockUseCase.actualizarPorPrendaYSede(
                prendaId, sedeId, talla, request.unidades());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Reservar stock para checkout",
            description = "Bloquea temporalmente unidades disponibles para un proceso de compra (SR-RF-53)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva exitosa",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ReservaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Parámetros de reserva inválidos",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "Inventario no encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
            @ApiResponse(responseCode = "409", description = "Stock insuficiente para satisfacer la reserva",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/reserva")
    public ResponseEntity<ReservaResponseDTO> reservarStock(
            @Valid @RequestBody ReservarStockCommand command) {
        ReservaResponseDTO response = gestionarReservaUseCase.reservar(command);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Confirmar reserva definitiva",
            description = "Deduce definitivamente las unidades reservadas tras la aprobación del pago")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva confirmada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "Inventario no encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
            @ApiResponse(responseCode = "409", description = "Stock reservado insuficiente",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    @PostMapping("/reserva/confirmar")
    public ResponseEntity<Void> confirmarReserva(@Valid @RequestBody ConfirmarReservaCommand command) {
        gestionarReservaUseCase.confirmar(command);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Liberar unidades reservadas",
            description = "Devuelve las unidades reservadas al stock disponible por rechazo, cancelación o expiración")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva liberada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "Inventario no encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
    })
    @PostMapping("/reserva/liberar")
    public ResponseEntity<Void> liberarReserva(@Valid @RequestBody LiberarReservaCommand command) {
        gestionarReservaUseCase.liberar(command);
        return ResponseEntity.ok().build();
    }
}
