package co.edu.poli.sw2.ProyectoHexa.dominio.command;

import java.util.Objects;

import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

/**
 * Comando concreto <b>Tomar fotografia</b>: captura una imagen de la zona.
 *
 * <p>
 * Guarda el receptor y el dron al crearse, y solo cuando se invoca
 * {@link #ejecutar()} delega la operacion en el receptor.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class TomarFotografiaCommand implements DroneCommand {

    /** Receptor que sabe realizar la operacion. */
    private final DroneOperacion operacion;

    /** Dron que ejecutara la tarea. */
    private final Drone drone;

    /**
     * Crea el comando.
     *
     * @param operacion receptor de la operacion
     * @param drone     dron que ejecutara la tarea
     * @throws NullPointerException si algun parametro es nulo
     */
    public TomarFotografiaCommand(DroneOperacion operacion, Drone drone) {
        this.operacion = Objects.requireNonNull(operacion, "El receptor es obligatorio.");
        this.drone = Objects.requireNonNull(drone, "El drone es obligatorio.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String ejecutar() {
        return operacion.tomarFotografia(drone);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNombre() {
        return TipoTarea.TOMAR_FOTOGRAFIA.getNombre();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Drone getDrone() {
        return drone;
    }
}
