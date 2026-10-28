package co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.Suscripcion;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.SuscripcionClimaUseCase;
import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Weather;
import co.edu.poli.sw2.ProyectoHexa.dominio.command.DroneOperacion;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Vigilancia;

/**
 * <b>Receptor</b> del patron Command: realiza las operaciones del dron.
 *
 * <p>
 * Las operaciones son simuladas, porque no hay un dron fisico. Para que
 * los resultados tengan sentido se apoyan en los datos reales del
 * sistema:
 * </p>
 * <ul>
 *   <li>La fotografia y el escaneo usan camara termica si el dron de
 *       vigilancia la tiene.</li>
 *   <li>La temperatura usa el ultimo clima notificado al dron (patron
 *       Observer) si esta suscrito a una ciudad; si no, entrega una
 *       lectura simulada.</li>
 * </ul>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class DroneOperacionService implements DroneOperacion {

    /** Suscripciones al clima, para leer la temperatura notificada. */
    private final SuscripcionClimaUseCase suscripcionClima;

    /** Generador de valores simulados. */
    private final Random aleatorio;

    /** Fotografias tomadas por cada dron (por id). */
    private final Map<Integer, Integer> fotografias = new HashMap<>();

    /**
     * Construye el receptor.
     *
     * @param suscripcionClima caso de uso de suscripciones al clima
     */
    public DroneOperacionService(SuscripcionClimaUseCase suscripcionClima) {
        this(suscripcionClima, new Random());
    }

    /**
     * Construye el receptor con un generador propio. Uso principal:
     * pruebas con resultados repetibles.
     *
     * @param suscripcionClima caso de uso de suscripciones al clima
     * @param aleatorio        generador de valores simulados
     */
    public DroneOperacionService(SuscripcionClimaUseCase suscripcionClima, Random aleatorio) {
        this.suscripcionClima = Objects.requireNonNull(suscripcionClima);
        this.aleatorio = Objects.requireNonNull(aleatorio);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized String tomarFotografia(Drone drone) {
        int numero = fotografias.merge(drone.getId(), 1, Integer::sum);
        return String.format("Fotografia #%d tomada por %s%s.",
                numero, drone.getSerial(),
                tieneCamaraTermica(drone) ? " con camara termica" : "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized String escanearZona(Drone drone) {
        int radio = 200 + aleatorio.nextInt(9) * 100;    // 200 a 1000 m
        int anomalias = aleatorio.nextInt(4);            // 0 a 3
        return String.format("Zona escaneada por %s en un radio de %d m%s: %s.",
                drone.getSerial(), radio,
                tieneCamaraTermica(drone) ? " con camara termica" : "",
                anomalias == 0 ? "sin anomalias" : anomalias + " anomalia(s) detectada(s)");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized String medirTemperatura(Drone drone) {
        Weather clima = climaNotificado(drone);
        if (clima != null) {
            return String.format(Locale.US,
                    "Temperatura medida por %s: %.1f °C (clima notificado de su ciudad).",
                    drone.getSerial(), clima.getTemperatura());
        }
        double simulada = 10 + aleatorio.nextDouble() * 20;   // 10 a 30 C
        return String.format(Locale.US,
                "Temperatura medida por %s: %.1f °C (lectura simulada).",
                drone.getSerial(), simulada);
    }

    /**
     * Busca el ultimo clima notificado al dron en la pestana Clima.
     *
     * @param drone dron a buscar
     * @return clima notificado, o {@code null} si no tiene
     */
    private Weather climaNotificado(Drone drone) {
        for (Suscripcion suscripcion : suscripcionClima.listarSuscripciones()) {
            if (suscripcion.getDrone().equals(drone)) {
                return suscripcion.getDrone().getClimaActual();
            }
        }
        return null;
    }

    /**
     * Indica si el dron tiene camara termica.
     *
     * @param drone dron a revisar
     * @return {@code true} si es de vigilancia con deteccion termica
     */
    private static boolean tieneCamaraTermica(Drone drone) {
        return drone instanceof Vigilancia && ((Vigilancia) drone).isDeteccionTermica();
    }
}
