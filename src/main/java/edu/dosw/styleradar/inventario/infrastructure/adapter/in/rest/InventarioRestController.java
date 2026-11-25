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
import edu.dosw.styleradar.inventario.application.dto.VerificacionDisponibilidadDTO;
import edu.dosw.styleradar.inventario.domain.port.in.AjustarStockUseCase;
import edu.dosw.styleradar.inventario.domain.port.in.ConsultarStockUseCase;
import edu.dosw.styleradar.inventario.domain.port.in.FiltrarPrendasPorTallaUseCase;
import edu.dosw.styleradar.inventario.domain.port.in.GestionarReservaUseCase;
import edu.dosw.styleradar.inventario.domain.port.in.RegistrarLoteStockUseCase;
import edu.dosw.styleradar.inventario.domain.port.in.RegistrarStockUseCase;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Inventory", description = "Endpoints for multi-branch inventory registration, adjustment, reservations, consultation, and stock filtering")
@RestController
@RequestMapping({"/api/v1/inventory", "/api/v1/inventario"})
public class InventarioRestController {

    private final RegistrarStockUseCase registrarStockUseCase;
    private final RegistrarLoteStockUseCase registrarLoteStockUseCase;
    private final AjustarStockUseCase ajustarStockUseCase;
    private final GestionarReservaUseCase gestionarReservaUseCase;
    private final ConsultarStockUseCase consultarStockUseCase;
    private final FiltrarPrendasPorTallaUseCase filtrarPrendasPorTallaUseCase;

    public InventarioRestController(RegistrarStockUseCase registrarStockUseCase,
                                    RegistrarLoteStockUseCase registrarLoteStockUseCase,
                                    AjustarStockUseCase ajustarStockUseCase,
                                    GestionarReservaUseCase gestionarReservaUseCase,
                                    ConsultarStockUseCase consultarStockUseCase,
                                    FiltrarPrendasPorTallaUseCase filtrarPrendasPorTallaUseCase) {
        this.registrarStockUseCase = registrarStockUseCase;
        this.registrarLoteStockUseCase = registrarLoteStockUseCase;
        this.ajustarStockUseCase = ajustarStockUseCase;
        this.gestionarReservaUseCase = gestionarReservaUseCase;
        this.consultarStockUseCase = consultarStockUseCase;
        this.filtrarPrendasPorTallaUseCase = filtrarPrendasPorTallaUseCase;
    }

    @Operation(summary = "Check garment availability",
            description = "Returns the consolidated and branch/variant-broken-down stock for a specific garment")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Availability successfully retrieved",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DisponibilidadPrendaResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Garment not found in inventory (INVENTARIO_NO_ENCONTRADO)",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Recurso no encontrado\",\"status\":404,\"detail\":\"No se encontró inventario para la prenda ...\",\"instance\":\"/api/v1/inventory/garments/{garmentId}\",\"code\":\"INVENTARIO_NO_ENCONTRADO\",\"timestamp\":\"2026-11-20T22:58:00Z\"}")))
    })
    @GetMapping({"/garments/{garmentId}", "/prenda/{garmentId}"})
    public ResponseEntity<DisponibilidadPrendaResponseDTO> getByGarmentId(
            @Parameter(description = "Garment UUID identifier", required = true)
            @PathVariable UUID garmentId) {
        DisponibilidadPrendaResponseDTO response = consultarStockUseCase.consultarPorPrenda(garmentId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Verify availability for catalog and search (SR-RF-57 / RES-8)",
            description = "Indicates whether the garment should be shown or hidden from available results based on historical stock")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verification successful",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = VerificacionDisponibilidadDTO.class))),
            @ApiResponse(responseCode = "404", description = "Garment not found (INVENTARIO_NO_ENCONTRADO)",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping({"/garments/{garmentId}/availability", "/prenda/{garmentId}/disponibilidad"})
    public ResponseEntity<VerificacionDisponibilidadDTO> verifyAvailability(
            @Parameter(description = "Garment UUID identifier", required = true)
            @PathVariable UUID garmentId) {
        VerificacionDisponibilidadDTO response = consultarStockUseCase.verificarDisponibilidad(garmentId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "List IDs of available garments with stock per branch (SR-RF-57 / RES-8)",
            description = "Returns exclusively non-sold-out garments that must be displayed on the branch radar")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of available garments returned")
    })
    @GetMapping({"/branches/{branchId}/available", "/sedes/{branchId}/disponibles"})
    public ResponseEntity<List<UUID>> listAvailableGarmentsByBranch(
            @Parameter(description = "Branch UUID identifier", required = true)
            @PathVariable UUID branchId) {
        List<UUID> disponibles = consultarStockUseCase.listarPrendasDisponiblesPorSede(branchId);
        return ResponseEntity.ok(disponibles);
    }

    @Operation(summary = "Filter available garments by size (SR-RF-10 / RES-24)",
            description = "Returns the list of garment UUID identifiers that have physical stock available in the specified size. Used by ms-item-catalogo to resolve filters.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of garments available in the selected size",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @GetMapping({"/sizes/{size}/garments", "/tallas/{size}/prendas"})
    public ResponseEntity<List<UUID>> filterGarmentsBySize(
            @Parameter(description = "Typified size to query (e.g., 'M', 'L', '40')", required = true)
            @PathVariable String size,
            @Parameter(description = "Optional physical branch UUID identifier to narrow down the geographic filter")
            @RequestParam(required = false) UUID branchId,
            @Parameter(description = "Legacy Spanish query parameter alias for branchId", hidden = true)
            @RequestParam(required = false) UUID sedeId) {
        UUID resolvedBranchId = branchId != null ? branchId : sedeId;
        List<UUID> prendas = filtrarPrendasPorTallaUseCase.filtrarPrendasDisponiblesPorTalla(size, resolvedBranchId);
        return ResponseEntity.ok(prendas);
    }

    @Operation(summary = "Register initial stock for a size",
            description = "Registers or updates the initial availability of a size for a garment at a physical branch (SR-RF-50 / SR-RF-52)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Stock successfully registered",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = StockItemResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload or incompatible size (DATOS_INVALIDOS / TALLA_INVALIDA / CANTIDAD_INVALIDA)",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Branch inventory suspended due to arrears (INVENTARIO_SUSPENDIDO)",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<StockItemResponseDTO> registerStock(
            @Valid @RequestBody RegistrarStockCommand command) {
        StockItemResponseDTO response = registrarStockUseCase.ejecutar(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Register stock batch of available sizes (SR-RF-52 / RES-6)",
            description = "Validates garment existence and store ownership against ms-item-catalogo and associates sizes and initial stock")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Sizes successfully associated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DisponibilidadPrendaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data or size incompatible with garment type (DATOS_INVALIDOS / TALLA_INVALIDA / CANTIDAD_INVALIDA)",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Inventory suspended (INVENTARIO_SUSPENDIDO) or garment belongs to another store (PRENDA_DE_OTRO_ALMACEN)",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Prenda no pertenece a la sede\",\"status\":403,\"detail\":\"La prenda ... no pertenece al almacén de la sede ...\",\"instance\":\"/api/v1/inventory/batch\",\"code\":\"PRENDA_DE_OTRO_ALMACEN\",\"timestamp\":\"2026-11-20T22:58:00Z\"}"))),
            @ApiResponse(responseCode = "404", description = "Garment does not exist in ms-item-catalogo (PRENDA_NO_ENCONTRADA)",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Recurso no encontrado\",\"status\":404,\"detail\":\"La prenda ... no existe en ms-item-catalogo\",\"instance\":\"/api/v1/inventory/batch\",\"code\":\"PRENDA_NO_ENCONTRADA\",\"timestamp\":\"2026-11-20T22:58:00Z\"}"))),
            @ApiResponse(responseCode = "503", description = "ms-item-catalogo service unavailable (CATALOGO_NO_DISPONIBLE)",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Servicio de catálogo no disponible\",\"status\":503,\"detail\":\"El servicio de catálogo (ms-item-catalogo) no está disponible\",\"instance\":\"/api/v1/inventory/batch\",\"code\":\"CATALOGO_NO_DISPONIBLE\",\"timestamp\":\"2026-11-20T22:58:00Z\"}")))
    })
    @PostMapping({"/batch", "/lote"})
    public ResponseEntity<DisponibilidadPrendaResponseDTO> registerBatch(
            @Valid @RequestBody RegistrarLoteStockCommand command) {
        DisponibilidadPrendaResponseDTO response = registrarLoteStockUseCase.registrarLote(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Adjust physical stock of a size by item ID",
            description = "Manually sets the available units of an existing variant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock successfully adjusted",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = StockItemResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data (DATOS_INVALIDOS / TALLA_INVALIDA / CANTIDAD_INVALIDA)",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Item or size not found (INVENTARIO_NO_ENCONTRADO / TALLA_NO_REGISTRADA)",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping({"/{id}/adjustment", "/{id}/ajuste"})
    public ResponseEntity<StockItemResponseDTO> adjustStock(
            @Parameter(description = "StockItem UUID identifier", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody AjustarStockCommand command) {
        AjustarStockCommand adjustedCommand = new AjustarStockCommand(id, command.talla(), command.nuevaCantidad());
        StockItemResponseDTO response = ajustarStockUseCase.ejecutar(adjustedCommand);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Independently modify availability by size (SR-RF-56 / RES-7)",
            description = "Allows the administrator to directly modify the units of a size for a garment at their branch")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Availability successfully updated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = StockItemResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Negative units or invalid size (DATOS_INVALIDOS / TALLA_INVALIDA / CANTIDAD_INVALIDA)",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Branch suspended due to arrears (INVENTARIO_SUSPENDIDO)",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Garment, branch, or size not registered (INVENTARIO_NO_ENCONTRADO / TALLA_NO_REGISTRADA)",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PatchMapping({"/garments/{garmentId}/branches/{branchId}/sizes/{size}", "/prenda/{garmentId}/sede/{branchId}/tallas/{size}"})
    public ResponseEntity<StockItemResponseDTO> updateSizeAvailability(
            @Parameter(description = "Garment UUID identifier", required = true)
            @PathVariable UUID garmentId,
            @Parameter(description = "Branch UUID identifier", required = true)
            @PathVariable UUID branchId,
            @Parameter(description = "Typified size to modify", required = true)
            @PathVariable String size,
            @Valid @RequestBody ActualizarDisponibilidadDTO request) {
        StockItemResponseDTO response = ajustarStockUseCase.actualizarPorPrendaYSede(
                garmentId, branchId, size, request.unidades());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Reserve stock for checkout",
            description = "Temporarily blocks available units for a purchase process (SR-RF-53)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful reservation",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ReservaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid reservation parameters (DATOS_INVALIDOS / TALLA_INVALIDA)",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Inventory not found (INVENTARIO_NO_ENCONTRADO / TALLA_NO_REGISTRADA)",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Insufficient stock to satisfy the reservation (STOCK_INSUFICIENTE)",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping({"/reservations", "/reserva"})
    public ResponseEntity<ReservaResponseDTO> reserveStock(
            @Valid @RequestBody ReservarStockCommand command) {
        ReservaResponseDTO response = gestionarReservaUseCase.reservar(command);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Confirm definitive reservation",
            description = "Definitively deducts reserved units after payment approval")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation confirmed"),
            @ApiResponse(responseCode = "400", description = "Invalid data (DATOS_INVALIDOS)",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Inventory not found (INVENTARIO_NO_ENCONTRADO / TALLA_NO_REGISTRADA)",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Insufficient reserved stock (STOCK_INSUFICIENTE)",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping({"/reservations/confirm", "/reserva/confirmar"})
    public ResponseEntity<Void> confirmReservation(@Valid @RequestBody ConfirmarReservaCommand command) {
        gestionarReservaUseCase.confirmar(command);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Release reserved units",
            description = "Returns reserved units to available stock due to rejection, cancellation, or expiration")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation released"),
            @ApiResponse(responseCode = "400", description = "Invalid data (DATOS_INVALIDOS)",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Inventory not found (INVENTARIO_NO_ENCONTRADO / TALLA_NO_REGISTRADA)",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping({"/reservations/release", "/reserva/liberar"})
    public ResponseEntity<Void> releaseReservation(@Valid @RequestBody LiberarReservaCommand command) {
        gestionarReservaUseCase.liberar(command);
        return ResponseEntity.ok().build();
    }
}
