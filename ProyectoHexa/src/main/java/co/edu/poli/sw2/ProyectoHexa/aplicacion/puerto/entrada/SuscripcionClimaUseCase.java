package co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada;

import java.util.List;
import java.util.Optional;

import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Ciudad;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

/**
 * Puerto de entrada para suscribir o dar de baja drones en el
 * notificador del clima de cada ciudad.
 *
 * <p>
 * Un dron puede estar suscrito a una sola ciudad a la vez, y una
 * ciudad puede tener muchos drones. Las suscripciones se mantienen en
 * memoria mientras la aplicacion esta abierta.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public interface SuscripcionClimaUseCase {

    /**
     * Suscribe un dron al clima de una ciudad. Si ya estaba suscrito a
     * otra ciudad, se traslada a la nueva.
     *
     * @param drone  dron a suscribir
     * @param ciudad ciudad elegida
     */
    void suscribir(Drone drone, Ciudad ciudad);

    /**
     * Da de baja un dron de la ciudad a la que esta suscrito.
     *
     * @param drone dron a desuscribir
     */
    void desuscribir(Drone drone);

    /**
     * Retira un dron de cualquier suscripcion, sin error si no tenia
     * ninguna. Se usa cuando el dron se elimina del sistema.
     *
     * @param drone dron a retirar
     */
    void retirar(Drone drone);

    /**
     * Reemplaza la copia suscrita de un dron por una version con sus
     * datos actualizados, conservando la ciudad y el ultimo clima.
     * No hace nada si el dron no estaba suscrito.
     *
     * @param drone dron con los datos actualizados
     */
    void sincronizar(Drone drone);

    /**
     * Obtiene la ciudad a la que esta suscrito un dron.
     *
     * @param drone dron a consultar
     * @return la ciudad, o vacio si no esta suscrito
     */
    Optional<Ciudad> ciudadDe(Drone drone);

    /**
     * Lista todas las suscripciones actuales, agrupadas por ciudad en
     * el orden del enum {@link Ciudad}.
     *
     * @return suscripciones actuales
     */
    List<Suscripcion> listarSuscripciones();
}
