package edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

/**
 * Representación relacional de una variante de talla del StockItem.
 */
@Entity
@Table(name = "inventario_tallas",
        uniqueConstraints = @UniqueConstraint(name = "uk_inventario_item_talla",
                columnNames = {"inventario_item_id", "talla"}))
public class InventarioVarianteJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventario_item_id", nullable = false)
    private StockItemJpaEntity inventarioItem;

    @Column(name = "talla", nullable = false, length = 5)
    private String talla;

    @Column(name = "unidades_disponibles", nullable = false)
    private int unidadesDisponibles;

    @Column(name = "unidades_reservadas", nullable = false)
    private int unidadesReservadas;

    public InventarioVarianteJpaEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public StockItemJpaEntity getInventarioItem() {
        return inventarioItem;
    }

    public void setInventarioItem(StockItemJpaEntity inventarioItem) {
        this.inventarioItem = inventarioItem;
    }

    public String getTalla() {
        return talla;
    }

    public void setTalla(String talla) {
        this.talla = talla;
    }

    public int getUnidadesDisponibles() {
        return unidadesDisponibles;
    }

    public void setUnidadesDisponibles(int unidadesDisponibles) {
        this.unidadesDisponibles = unidadesDisponibles;
    }

    public int getUnidadesReservadas() {
        return unidadesReservadas;
    }

    public void setUnidadesReservadas(int unidadesReservadas) {
        this.unidadesReservadas = unidadesReservadas;
    }
}
