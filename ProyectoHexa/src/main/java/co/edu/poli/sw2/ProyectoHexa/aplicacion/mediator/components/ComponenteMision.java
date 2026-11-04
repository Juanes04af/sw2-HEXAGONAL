package co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator.components;

import java.util.Objects;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator.DroneMediator;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator.EventoMision;

/**
 * Base de los componentes (colegas) del patron Mediator.
 *
 * <p>
 * Cada componente guarda una referencia al mediador y solo se comunica
 * con el. Ningun componente tiene referencias a los demas.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public abstract class ComponenteMision {

    /** Mediador al que el componente informa sus eventos. */
    protected DroneMediator mediator;

    /**
     * Asigna el mediador. Lo invoca el coordinador al registrarse.
     *
     * @param mediator mediador de la mision
     */
    public void setMediator(DroneMediator mediator) {
        this.mediator = Objects.requireNonNull(mediator, "El mediador es obligatorio.");
    }

    /**
     * Obtiene el mediador asignado.
     *
     * @return mediador, o {@code null} si aun no tiene
     */
    public DroneMediator getMediator() {
        return mediator;
    }

    /**
     * Obtiene el nombre del componente, para mostrarlo al usuario.
     *
     * @return nombre del sistema
     */
    public abstract String getNombre();

    /**
     * Informa un evento al mediador.
     *
     * @param evento  evento ocurrido
     * @param detalle descripcion del resultado
     * @throws IllegalStateException si el componente no tiene mediador
     */
    protected void informar(EventoMision evento,
                            String detalle) {
        if (mediator == null) {
            throw new IllegalStateException(getNombre() + " no tiene mediador asignado.");
        }
        mediator.notificar(this, evento, detalle);
    }
}
