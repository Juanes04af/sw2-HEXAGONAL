package co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio;

import java.util.Objects;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator.DroneMediator;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ExecuteMissionUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ResultadoMision;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Vigilancia;

/**
 * Servicio que atiende la solicitud de iniciar una operacion de
 * vigilancia y la entrega al mediador.
 *
 * <p>
 * Crea el dron solo en memoria (no lo guarda ni lo consulta en la
 * base de datos), porque esta funcionalidad no debe usar el CRUD.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class MissionService implements ExecuteMissionUseCase {

    /** Fabricante asignado a los drones de demostracion. */
    private static final String FABRICANTE = "Simulado";

    /** Peso asignado a los drones de demostracion, en kg. */
    private static final double PESO = 1.0;

    /** Mediador que coordina la mision. */
    private final DroneMediator mediator;

    /**
     * Construye el servicio.
     *
     * @param mediator mediador de la mision
     */
    public MissionService(DroneMediator mediator) {
        this.mediator = Objects.requireNonNull(mediator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResultadoMision ejecutarMision(String serial, String modelo,
                                          boolean deteccionTermica, int nivelBateria) {
        ValidadorDrone.validarSerial(serial);

        Drone drone = new Vigilancia(0, serial.trim(), FABRICANTE,
                modelo == null || modelo.isBlank() ? null : modelo.trim(),
                PESO, deteccionTermica);

        return mediator.iniciarMision(drone, nivelBateria);
    }
}
