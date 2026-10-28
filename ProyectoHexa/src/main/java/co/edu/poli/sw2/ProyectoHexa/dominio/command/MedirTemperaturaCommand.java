package co.edu.poli.sw2.ProyectoHexa.dominio.command;

import java.util.Objects;

import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

/**
 * Comando concreto <b>Medir temperatura</b>: mide la temperatura del ambiente.
 *
 * <p>
 * Guarda el receptor y el dron al crearse, y solo cuando se invoca
 * {@link #ejecutar()} delega la operacion en el receptor.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class MedirTemperaturaCommand implements DroneCommand {

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
    public MedirTemperaturaCommand(DroneOperacion operacion, Drone drone) {
        this.operacion = Objects.requireNonNull(operacion, "El receptor es obligatorio.");
        this.drone = Objects.requireNonNull(drone, "El drone es obligatorio.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String ejecutar() {
        return operacion.medirTemperatura(drone);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNombre() {
        return TipoTarea.MEDIR_TEMPERATURA.getNombre();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Drone getDrone() {
        return drone;
    }
}
