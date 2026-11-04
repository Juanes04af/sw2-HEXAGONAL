package co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator.components;

import java.util.Locale;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator.EventoMision;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.strategy.ConsumoStrategy;
import co.edu.poli.sw2.ProyectoHexa.dominio.strategy.NormalStrategy;

/**
 * Componente de bateria: verifica la disponibilidad energetica.
 *
 * <p>
 * La bateria es suficiente si cubre el consumo del recorrido en modo
 * normal (patron Strategy) mas una reserva de seguridad del
 * {@value #RESERVA_PORCENTAJE} %.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class BatterySystem extends ComponenteMision {

    /** Reserva minima que debe quedar al terminar el recorrido, en %. */
    public static final double RESERVA_PORCENTAJE = 20.0;

    /** Estrategia con la que se estima el consumo del recorrido. */
    private final ConsumoStrategy consumo = new NormalStrategy();

    /**
     * Verifica si la bateria alcanza para el recorrido e informa el
     * resultado al mediador.
     *
     * @param drone        dron que hara la mision
     * @param nivelBateria bateria actual, de 0 a 100 %
     * @param distanciaKm  distancia de la ruta en km
     * @return {@code true} si la bateria es suficiente
     */
    public boolean verificarBateria(Drone drone, int nivelBateria, double distanciaKm) {
        double requerida = consumo.calcularConsumo(distanciaKm) + RESERVA_PORCENTAJE;
        boolean suficiente = nivelBateria >= requerida;

        String detalle = String.format(Locale.US,
                "Bateria de %s: %d %% %s %.1f %% requerido (consumo %.1f %% + reserva %.0f %%).",
                drone.getSerial(), nivelBateria, suficiente ? ">=" : "<",
                requerida, requerida - RESERVA_PORCENTAJE, RESERVA_PORCENTAJE);

        informar(suficiente ? EventoMision.BATERIA_SUFICIENTE : EventoMision.BATERIA_INSUFICIENTE,
                 detalle);
        return suficiente;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNombre() {
        return "Sistema de bateria";
    }
}
