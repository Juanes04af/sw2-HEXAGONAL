package co.edu.poli.sw2.ProyectoHexa.dominio.state;

import java.util.Objects;

import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

/**
 * <b>Contexto</b> del patron State, implementado como <b>wrapper</b> de
 * {@link Drone}.
 *
 * <p>
 * La clase {@code Drone} no se modifica: este envoltorio la contiene,
 * mantiene el {@link DroneState} actual ({@code estadoActual}) y
 * delega en el cada operacion. Todo dron nuevo inicia APAGADO.
 * </p>
 *
 * <p>
 * Solo los estados concretos (mismo paquete) pueden cambiar el estado,
 * por lo que no existe forma de saltarse el ciclo operativo.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public final class DroneConEstado {

    /** Dron envuelto (entidad original, sin modificar). */
    private Drone drone;

    /** Estado operativo actual. */
    private DroneState estadoActual;

    /**
     * Envuelve un dron; queda APAGADO.
     *
     * @param drone dron a envolver
     * @throws NullPointerException si el dron es nulo
     */
    public DroneConEstado(Drone drone) {
        this.drone = Objects.requireNonNull(drone, "El drone es obligatorio.");
        this.estadoActual = new OffState(this);
    }

    /**
     * Obtiene el dron envuelto.
     *
     * @return dron original
     */
    public Drone getDrone() {
        return drone;
    }

    /**
     * Reemplaza el dron envuelto por una instancia mas reciente del
     * mismo dron (cada consulta a la BD crea objetos nuevos). El estado
     * no cambia.
     *
     * @param drone instancia actualizada
     * @throws NullPointerException si el dron es nulo
     */
    public void setDrone(Drone drone) {
        this.drone = Objects.requireNonNull(drone, "El drone es obligatorio.");
    }

    /**
     * Obtiene el estado operativo actual.
     *
     * @return estado actual
     */
    public DroneState getEstadoActual() {
        return estadoActual;
    }

    /**
     * Cambia el estado. Visibilidad de paquete: solo lo usan los estados
     * concretos al realizar una transicion permitida.
     *
     * @param nuevo estado al que pasa el dron
     */
    void cambiarEstado(DroneState nuevo) {
        this.estadoActual = Objects.requireNonNull(nuevo);
    }

    /** Enciende el dron; el estado actual decide si es posible. */
    public void encender() {
        estadoActual.encender(drone);
    }

    /** Apaga el dron; el estado actual decide si es posible. */
    public void apagar() {
        estadoActual.apagar(drone);
    }

    /** Despega el dron; el estado actual decide si es posible. */
    public void despegar() {
        estadoActual.despegar(drone);
    }

    /** Inicia el aterrizaje; el estado actual decide si es posible. */
    public void iniciarAterrizaje() {
        estadoActual.iniciarAterrizaje(drone);
    }

    /** Finaliza el aterrizaje; el estado actual decide si es posible. */
    public void finalizarAterrizaje() {
        estadoActual.finalizarAterrizaje(drone);
    }
}
