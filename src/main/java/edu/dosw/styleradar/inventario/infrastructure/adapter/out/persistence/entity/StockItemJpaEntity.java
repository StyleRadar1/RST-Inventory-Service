package edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence.entity;

import edu.dosw.styleradar.inventario.domain.model.EstadoItem;
import edu.dosw.styleradar.inventario.domain.model.TipoPrenda;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Representación relacional del agregado StockItem.
 */
@Entity
@Table(name = "inventario_items",
        uniqueConstraints = @UniqueConstraint(name = "uk_inventario_prenda_sede",
                columnNames = {"prenda_id", "sede_id"}))
public class StockItemJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "prenda_id", nullable = false)
    private UUID prendaId;

    @Column(name = "sede_id", nullable = false)
    private UUID sedeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_prenda", nullable = false, length = 20)
    private TipoPrenda tipoPrenda;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoItem estado;

    @Column(name = "umbral_ultimas_unidades", nullable = false)
    private int umbralUltimasUnidades;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    @Column(name = "fecha_publicacion", nullable = false)
    private Instant fechaPublicacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private Instant fechaActualizacion;

    @OneToMany(mappedBy = "inventarioItem", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private List<InventarioVarianteJpaEntity> variantes = new ArrayList<>();

    public StockItemJpaEntity() {
    }

    public void agregarVariante(InventarioVarianteJpaEntity variante) {
        variante.setInventarioItem(this);
        this.variantes.add(variante);
    }

    public void eliminarVariante(InventarioVarianteJpaEntity variante) {
        this.variantes.remove(variante);
        variante.setInventarioItem(null);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPrendaId() {
        return prendaId;
    }

    public void setPrendaId(UUID prendaId) {
        this.prendaId = prendaId;
    }

    public UUID getSedeId() {
        return sedeId;
    }

    public void setSedeId(UUID sedeId) {
        this.sedeId = sedeId;
    }

    public TipoPrenda getTipoPrenda() {
        return tipoPrenda;
    }

    public void setTipoPrenda(TipoPrenda tipoPrenda) {
        this.tipoPrenda = tipoPrenda;
    }

    public EstadoItem getEstado() {
        return estado;
    }

    public void setEstado(EstadoItem estado) {
        this.estado = estado;
    }

    public int getUmbralUltimasUnidades() {
        return umbralUltimasUnidades;
    }

    public void setUmbralUltimasUnidades(int umbralUltimasUnidades) {
        this.umbralUltimasUnidades = umbralUltimasUnidades;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Instant getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(Instant fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public Instant getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(Instant fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public List<InventarioVarianteJpaEntity> getVariantes() {
        return variantes;
    }

    public void setVariantes(List<InventarioVarianteJpaEntity> variantes) {
        this.variantes = variantes == null ? new ArrayList<>() : variantes;
    }
}
