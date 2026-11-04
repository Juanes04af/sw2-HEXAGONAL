package co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator.components.BatterySystem;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator.components.CommunicationSystem;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator.components.ComponenteMision;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator.components.NavigationSystem;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator.components.SensorSystem;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.PasoMision;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ResultadoMision;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronValidacionException;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

/**
 * Mediador concreto: <b>coordinador de mision</b>.
 *
 * <p>
 * Conoce los cuatro sistemas y controla la secuencia. Cada sistema
 * hace su trabajo y le informa un evento; el coordinador decide que
 * sistema actua despues:
 * </p>
 * <ol>
 *   <li>Navegacion prepara la ruta.</li>
 *   <li>Con la distancia de la ruta, bateria verifica la energia. Si no
 *       alcanza, la mision se cancela aqui.</li>
 *   <li>Sensores activa los sensores.</li>
 *   <li>Comunicacion establece el enlace y la mision queda iniciada.</li>
 * </ol>
 *
 * <p>
 * Los sistemas nunca se llaman entre si: por ejemplo, la distancia que
 * calcula navegacion llega a bateria a traves del coordinador.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public final class MissionCoordinator implements DroneMediator {

    /** Sistema de navegacion. */
    private final NavigationSystem navigationSystem;

    /** Sistema de sensores. */
    private final SensorSystem sensorSystem;

    /** Sistema de bateria. */
    private final BatterySystem batterySystem;

    /** Sistema de comunicacion. */
    private final CommunicationSystem communicationSystem;

    /** Dron de la mision en curso. */
    private Drone drone;

    /** Bateria del dron de la mision en curso. */
    private int nivelBateria;

    /** Pasos registrados en la mision en curso. */
    private List<PasoMision> pasos;

    /** Indica si la mision en curso ya inicio. */
    private boolean iniciada;

    /** Motivo de cancelacion de la mision en curso. */
    private String motivo;

    /**
     * Construye el coordinador y se registra como mediador de cada
     * componente.
     *
     * @param navigationSystem    sistema de navegacion
     * @param sensorSystem        sistema de sensores
     * @param batterySystem       sistema de bateria
     * @param communicationSystem sistema de comunicacion
     */
    public MissionCoordinator(NavigationSystem navigationSystem, SensorSystem sensorSystem,
                              BatterySystem batterySystem,
                              CommunicationSystem communicationSystem) {
        this.navigationSystem = Objects.requireNonNull(navigationSystem);
        this.sensorSystem = Objects.requireNonNull(sensorSystem);
        this.batterySystem = Objects.requireNonNull(batterySystem);
        this.communicationSystem = Objects.requireNonNull(communicationSystem);

        navigationSystem.setMediator(this);
        sensorSystem.setMediator(this);
        batterySystem.setMediator(this);
        communicationSystem.setMediator(this);
    }

    /**
     * {@inheritDoc}
     *
     * @throws DronValidacionException si falta el dron o el nivel de
     *         bateria no esta entre 0 y 100
     */
    @Override
    public synchronized ResultadoMision iniciarMision(Drone drone, int nivelBateria) {
        if (drone == null) {
            throw new DronValidacionException("La mision necesita un drone.");
        }
        if (nivelBateria < 0 || nivelBateria > 100) {
            throw new DronValidacionException("El nivel de bateria debe estar entre 0 y 100 %.");
        }

        this.drone = drone;
        this.nivelBateria = nivelBateria;
        this.pasos = new ArrayList<>();
        this.iniciada = false;
        this.motivo = null;

        try {
            // Primer paso: el resto de la secuencia la dirigen los eventos.
            navigationSystem.prepararRuta(drone);
        } catch (RuntimeException e) {
            registrar("Coordinador de mision", "Error", e.getMessage(), false);
            cancelar(e.getMessage());
        }

        return new ResultadoMision(drone, pasos, iniciada, motivo);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Aqui se decide la secuencia de la mision segun el evento.</p>
     */
    @Override
    public void notificar(ComponenteMision emisor, EventoMision evento, String detalle) {
        boolean exitoso = evento != EventoMision.BATERIA_INSUFICIENTE;
        registrar(emisor.getNombre(), evento.getDescripcion(), detalle, exitoso);

        switch (evento) {
            case RUTA_PREPARADA:
                batterySystem.verificarBateria(drone, nivelBateria,
                        navigationSystem.getDistanciaKm());
                break;
            case BATERIA_SUFICIENTE:
                sensorSystem.activarSensores(drone);
                break;
            case BATERIA_INSUFICIENTE:
                cancelar("bateria insuficiente para el recorrido.");
                break;
            case SENSORES_ACTIVADOS:
                communicationSystem.establecerComunicacion(drone);
                break;
            case ENLACE_ESTABLECIDO:
                iniciada = true;
                registrar("Coordinador de mision", "Mision iniciada",
                        "Todas las condiciones se cumplieron.", true);
                break;
            default:
                break;
        }
    }

    /**
     * Agrega un paso a la mision en curso.
     *
     * @param componente quien informa
     * @param evento     evento
     * @param detalle    descripcion
     * @param exitoso    {@code true} si fue exitoso
     */
    private void registrar(String componente, String evento, String detalle, boolean exitoso) {
        if (pasos != null) {
            pasos.add(new PasoMision(pasos.size() + 1, componente, evento, detalle, exitoso));
        }
    }

    /**
     * Marca la mision como cancelada.
     *
     * @param razon motivo de la cancelacion
     */
    private void cancelar(String razon) {
        iniciada = false;
        motivo = razon;
    }
}
