package edu.dosw.styleradar.inventario.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "eventos_procesados")
public class EventoProcesadoJpaEntity {

    @Id
    @Column(name = "event_id", nullable = false, length = 100, updatable = false)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "procesado_en", nullable = false)
    private Instant procesadoEn;

    public EventoProcesadoJpaEntity() {
    }

    public EventoProcesadoJpaEntity(String eventId, String eventType, Instant procesadoEn) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.procesadoEn = procesadoEn;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Instant getProcesadoEn() {
        return procesadoEn;
    }

    public void setProcesadoEn(Instant procesadoEn) {
        this.procesadoEn = procesadoEn;
    }
}
