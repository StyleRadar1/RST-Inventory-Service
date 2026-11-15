package edu.dosw.styleradar.inventario.domain.port.out;

public interface EventoProcesadoPort {

    boolean yaFueProcesado(String eventId);

    void registrarProcesado(String eventId, String eventType);
}
