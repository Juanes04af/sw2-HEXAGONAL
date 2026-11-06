package co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator.components.ComponenteMision;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ResultadoMision;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

/**
 * Mediador del patron <b>Mediator</b>.
 *
 * <p>
 * Centraliza la comunicacion entre los sistemas que preparan una
 * operacion de vigilancia (navegacion, bateria, sensores y
 * comunicacion). Los sistemas no se conocen entre si: cada uno solo
 * conoce al mediador y le informa sus eventos con
 * {@link #notificar(ComponenteMision, EventoMision, String)}.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public interface DroneMediator {

    /**
     * Inicia la coordinacion de una mision de vigilancia.
     *
     * @param drone        dron que hara la mision
     * @param nivelBateria nivel de bateria actual del dron, de 0 a 100 %
     * @return resultado con cada paso y si la mision pudo iniciar
     */
    ResultadoMision iniciarMision(Drone drone, int nivelBateria);

    /**
     * Recibe un evento de un componente y decide el siguiente paso.
     *
     * @param emisor  componente que informa el evento
     * @param evento  evento ocurrido
     * @param detalle descripcion del resultado del componente
     */
    void notificar(ComponenteMision emisor, EventoMision evento, String detalle);
}
