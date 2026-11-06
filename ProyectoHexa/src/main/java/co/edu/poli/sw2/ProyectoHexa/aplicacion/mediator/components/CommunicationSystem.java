package co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator.components;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator.EventoMision;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

/**
 * Componente de comunicacion: establece el enlace entre el dron y la
 * estacion de control.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class CommunicationSystem extends ComponenteMision {

    /** Canal usado para el enlace. */
    private static final String CANAL = "canal cifrado 2.4 GHz";

    /**
     * Establece el enlace e informa el evento al mediador.
     *
     * @param drone dron que hara la mision
     * @return descripcion del enlace establecido
     */
    public String establecerComunicacion(Drone drone) {
        String detalle = "Enlace con " + drone.getSerial() + " establecido por " + CANAL + ".";
        informar(EventoMision.ENLACE_ESTABLECIDO, detalle);
        return detalle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNombre() {
        return "Sistema de comunicacion";
    }
}
