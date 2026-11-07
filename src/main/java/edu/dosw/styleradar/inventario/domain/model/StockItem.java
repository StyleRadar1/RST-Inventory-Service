package edu.dosw.styleradar.inventario.domain.model;

import edu.dosw.styleradar.inventario.domain.event.DomainEvent;
import edu.dosw.styleradar.inventario.domain.event.StockAgotadoEvent;
import edu.dosw.styleradar.inventario.domain.event.StockBajoEvent;
import edu.dosw.styleradar.inventario.domain.exception.CantidadInvalidaException;
import edu.dosw.styleradar.inventario.domain.exception.InventarioSuspendidoException;
import edu.dosw.styleradar.inventario.domain.exception.TallaConUnidadesException;
import edu.dosw.styleradar.inventario.domain.exception.TallaInvalidaException;
import edu.dosw.styleradar.inventario.domain.exception.TallaNoRegistradaException;
import edu.dosw.styleradar.inventario.domain.valueobject.Cantidad;
import edu.dosw.styleradar.inventario.domain.valueobject.Talla;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Agregado raíz del contexto de inventario. Identifica el stock de una prenda
 * en una sede concreta y encapsula todas las invariantes de stock, reservas,
 * suspensión y etiquetas automáticas.
 */
public class StockItem {

    public static final int UMBRAL_ULTIMAS_UNIDADES_POR_DEFECTO = 3;
    private static final Duration PERIODO_NUEVA_PRENDA = Duration.ofDays(7);

    private final UUID id;
    private final UUID prendaId;
    private final UUID sedeId;
    private final TipoPrenda tipoPrenda;
    private final Instant fechaPublicacion;
    private Instant fechaActualizacion;
    private final int umbralUltimasUnidades;
    private boolean activo;
    private final Map<String, InventarioVariante> variantes;
    private final List<DomainEvent> domainEvents;

    public static StockItem crear(UUID prendaId, UUID sedeId, TipoPrenda tipoPrenda,
                                  Instant fechaPublicacion, Instant ahora) {
        return new StockItem(UUID.randomUUID(), prendaId, sedeId, tipoPrenda, fechaPublicacion,
                ahora, UMBRAL_ULTIMAS_UNIDADES_POR_DEFECTO, true);
    }

    public static StockItem reconstruir(UUID id, UUID prendaId, UUID sedeId, TipoPrenda tipoPrenda,
                                        Instant fechaPublicacion, Instant fechaActualizacion,
                                        int umbralUltimasUnidades, boolean activo,
                                        Map<String, InventarioVariante> variantes) {
        StockItem stockItem = new StockItem(id, prendaId, sedeId, tipoPrenda, fechaPublicacion,
                fechaActualizacion, umbralUltimasUnidades, activo);
        if (variantes != null) {
            stockItem.variantes.putAll(variantes);
        }
        return stockItem;
    }

    public StockItem(UUID id, UUID prendaId, UUID sedeId, TipoPrenda tipoPrenda,
                     Instant fechaPublicacion, Instant fechaActualizacion,
                     int umbralUltimasUnidades, boolean activo) {
        this.id = Objects.requireNonNull(id, "El id del stock item es obligatorio");
        this.prendaId = Objects.requireNonNull(prendaId, "El prendaId es obligatorio");
        this.sedeId = Objects.requireNonNull(sedeId, "El sedeId es obligatorio");
        this.tipoPrenda = Objects.requireNonNull(tipoPrenda, "El tipo de prenda es obligatorio");
        this.fechaPublicacion = Objects.requireNonNull(fechaPublicacion, "La fecha de publicación es obligatoria");
        this.fechaActualizacion = Objects.requireNonNull(fechaActualizacion, "La fecha de actualización es obligatoria");
        if (umbralUltimasUnidades < 0) {
            throw new CantidadInvalidaException("El umbral de últimas unidades no puede ser negativo");
        }
        this.umbralUltimasUnidades = umbralUltimasUnidades;
        this.activo = activo;
        this.variantes = new LinkedHashMap<>();
        this.domainEvents = new ArrayList<>();
    }

    public void registrarTalla(Talla talla, Cantidad cantidadInicial, Instant ahora) {
        asegurarActivo();
        validarCompatibilidadTalla(talla);
        Cantidad cantidad = requerirCantidad(cantidadInicial);
        Instant momento = requerirAhora(ahora);

        InventarioVariante variante = variantes.get(talla.getValor());
        if (variante == null) {
            variante = new InventarioVariante(talla, cantidad, Cantidad.cero());
            variantes.put(talla.getValor(), variante);
        } else {
            variante.fijarStockDisponible(cantidad);
        }

        fechaActualizacion = momento;
        evaluarEventosDeStock(variante, momento);
    }

    public void actualizarStockFisico(Talla talla, Cantidad nuevaCantidad, Instant ahora) {
        asegurarActivo();
        validarCompatibilidadTalla(talla);
        Cantidad cantidad = requerirCantidad(nuevaCantidad);
        Instant momento = requerirAhora(ahora);

        InventarioVariante variante = obtenerVariante(talla);
        variante.fijarStockDisponible(cantidad);
        fechaActualizacion = momento;
        evaluarEventosDeStock(variante, momento);
    }

    public void reservarStock(Talla talla, Cantidad cantidad, Instant ahora) {
        asegurarActivo();
        validarCompatibilidadTalla(talla);
        Instant momento = requerirAhora(ahora);

        InventarioVariante variante = obtenerVariante(talla);
        variante.reservar(requerirCantidad(cantidad));
        fechaActualizacion = momento;
        evaluarEventosDeStock(variante, momento);
    }

    public void confirmarVenta(Talla talla, Cantidad cantidad, Instant ahora) {
        asegurarActivo();
        validarCompatibilidadTalla(talla);
        Instant momento = requerirAhora(ahora);

        InventarioVariante variante = obtenerVariante(talla);
        variante.confirmarReserva(requerirCantidad(cantidad));
        fechaActualizacion = momento;
        evaluarEventosDeStock(variante, momento);
    }

    public void liberarReserva(Talla talla, Cantidad cantidad, Instant ahora) {
        asegurarActivo();
        validarCompatibilidadTalla(talla);
        Instant momento = requerirAhora(ahora);

        InventarioVariante variante = obtenerVariante(talla);
        variante.liberarReserva(requerirCantidad(cantidad));
        fechaActualizacion = momento;
        evaluarEventosDeStock(variante, momento);
    }

    public void descontarStock(Talla talla, Cantidad cantidad, Instant ahora) {
        asegurarActivo();
        validarCompatibilidadTalla(talla);
        Instant momento = requerirAhora(ahora);

        InventarioVariante variante = obtenerVariante(talla);
        variante.decrementarStock(requerirCantidad(cantidad));
        fechaActualizacion = momento;
        evaluarEventosDeStock(variante, momento);
    }

    public void eliminarTalla(Talla talla) {
        asegurarActivo();
        validarCompatibilidadTalla(talla);
        InventarioVariante variante = obtenerVariante(talla);
        if (variante.tieneExistencias()) {
            throw new TallaConUnidadesException(
                    "No se puede eliminar la talla " + talla.getValor() + " porque tiene unidades en inventario");
        }
        variantes.remove(talla.getValor());
    }

    public void suspenderPorMora() {
        this.activo = false;
    }

    public void reactivarSuscripcion() {
        this.activo = true;
    }

    public EstadoItem calcularEstado(Instant ahora) {
        if (!activo) {
            return EstadoItem.SUSPENDIDO;
        }
        int stockDisponibleTotal = getStockDisponibleTotal();
        if (stockDisponibleTotal == 0) {
            return EstadoItem.AGOTADA;
        }
        if (stockDisponibleTotal <= umbralUltimasUnidades) {
            return EstadoItem.ULTIMAS_UNIDADES;
        }
        if (fechaPublicacion.plus(PERIODO_NUEVA_PRENDA).isAfter(ahora)) {
            return EstadoItem.NUEVA_PRENDA;
        }
        return EstadoItem.DISPONIBLE;
    }

    public void validarCompatibilidadTalla(Talla talla) {
        if (talla == null) {
            throw new TallaInvalidaException("La talla es obligatoria");
        }
        if (talla.getTipoPrenda() != tipoPrenda) {
            throw new TallaInvalidaException(
                    "La talla " + talla.getValor() + " es de tipo " + talla.getTipoPrenda()
                            + " y no es compatible con la prenda de tipo " + tipoPrenda);
        }
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> eventos = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return eventos;
    }

    public InventarioVariante obtenerVariante(Talla talla) {
        if (talla == null) {
            throw new TallaInvalidaException("La talla es obligatoria");
        }
        InventarioVariante variante = variantes.get(talla.getValor());
        if (variante == null) {
            throw new TallaNoRegistradaException(
                    "La talla " + talla.getValor() + " no está registrada para la prenda "
                            + prendaId + " en la sede " + sedeId);
        }
        return variante;
    }

    public Optional<InventarioVariante> buscarVariante(Talla talla) {
        if (talla == null) {
            throw new IllegalArgumentException("La talla no puede ser nula");
        }
        return Optional.ofNullable(variantes.get(talla.getValor()));
    }

    public int getStockDisponibleTotal() {
        return variantes.values().stream()
                .mapToInt(variante -> variante.getStockDisponible().valor())
                .sum();
    }

    public int getStockReservadoTotal() {
        return variantes.values().stream()
                .mapToInt(variante -> variante.getStockReservado().valor())
                .sum();
    }

    public UUID getId() {
        return id;
    }

    public UUID getPrendaId() {
        return prendaId;
    }

    public UUID getSedeId() {
        return sedeId;
    }

    public TipoPrenda getTipoPrenda() {
        return tipoPrenda;
    }

    public Instant getFechaPublicacion() {
        return fechaPublicacion;
    }

    public Instant getFechaActualizacion() {
        return fechaActualizacion;
    }

    public int getUmbralUltimasUnidades() {
        return umbralUltimasUnidades;
    }

    public boolean isActivo() {
        return activo;
    }

    public Map<String, InventarioVariante> getVariantes() {
        return Collections.unmodifiableMap(variantes);
    }

    private void asegurarActivo() {
        if (!activo) {
            throw new InventarioSuspendidoException(
                    "El inventario de la sede " + sedeId + " está suspendido por mora");
        }
    }

    private void evaluarEventosDeStock(InventarioVariante variante, Instant ahora) {
        int stockRestante = variante.getStockDisponible().valor();
        if (stockRestante == 0) {
            domainEvents.add(new StockAgotadoEvent(UUID.randomUUID(), prendaId, sedeId,
                    variante.getTalla().getValor(), stockRestante, ahora));
        } else if (stockRestante <= umbralUltimasUnidades) {
            domainEvents.add(new StockBajoEvent(UUID.randomUUID(), prendaId, sedeId,
                    variante.getTalla().getValor(), stockRestante, ahora));
        }
    }

    private static Cantidad requerirCantidad(Cantidad cantidad) {
        if (cantidad == null) {
            throw new CantidadInvalidaException("La cantidad es obligatoria");
        }
        return cantidad;
    }

    private static Instant requerirAhora(Instant ahora) {
        return Objects.requireNonNull(ahora, "La fecha de operación es obligatoria");
    }
}
