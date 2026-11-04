package co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator.components;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator.EventoMision;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Vigilancia;

/**
 * Componente de sensores: activa los sensores necesarios para la
 * vigilancia. Si el dron tiene deteccion termica, tambien activa la
 * camara termica.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class SensorSystem extends ComponenteMision {

    /** Sensores que se activan en toda mision. */
    private static final String SENSORES_BASE = "GPS, altimetro y camara HD";

    /**
     * Activa los sensores e informa el evento al mediador.
     *
     * @param drone dron que hara la mision
     * @return descripcion de los sensores activados
     */
    public String activarSensores(Drone drone) {
        boolean termica = drone instanceof Vigilancia && ((Vigilancia) drone).isDeteccionTermica();
        String detalle = "Sensores de " + drone.getSerial() + ": " + SENSORES_BASE
                + (termica ? " + camara termica." : ".");
        informar(EventoMision.SENSORES_ACTIVADOS, detalle);
        return detalle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNombre() {
        return "Sistema de sensores";
    }
}
