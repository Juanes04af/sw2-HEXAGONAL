package co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator.components;

import java.util.Locale;
import java.util.Objects;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator.EventoMision;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.salida.CalculadorDistancia;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.salida.RutaLoader;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.strategy.Ruta;

/**
 * Componente de navegacion: prepara la ruta del dron.
 *
 * <p>
 * Usa la misma ruta del archivo JSON y el mismo calculo de Haversine
 * del patron Strategy, a traves de sus puertos de salida.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class NavigationSystem extends ComponenteMision {

    /** Fuente de la ruta. */
    private final RutaLoader rutaLoader;

    /** Calculo de la distancia. */
    private final CalculadorDistancia calculadorDistancia;

    /** Distancia de la ultima ruta preparada, en km. */
    private double distanciaKm;

    /**
     * Construye el sistema de navegacion.
     *
     * @param rutaLoader          puerto para cargar la ruta
     * @param calculadorDistancia puerto para calcular la distancia
     */
    public NavigationSystem(RutaLoader rutaLoader, CalculadorDistancia calculadorDistancia) {
        this.rutaLoader = Objects.requireNonNull(rutaLoader);
        this.calculadorDistancia = Objects.requireNonNull(calculadorDistancia);
    }

    /**
     * Prepara la ruta del dron e informa el evento al mediador.
     *
     * @param drone dron que hara la mision
     * @return descripcion de la ruta preparada
     */
    public String prepararRuta(Drone drone) {
        Ruta ruta = rutaLoader.cargarRuta();
        distanciaKm = calculadorDistancia.distanciaKm(ruta.getOrigen(), ruta.getDestino());

        String detalle = String.format(Locale.US, "Ruta de %s: %s -> %s (%.2f km).",
                drone.getSerial(), ruta.getOrigen().getNombre(),
                ruta.getDestino().getNombre(), distanciaKm);
        informar(EventoMision.RUTA_PREPARADA, detalle);
        return detalle;
    }

    /**
     * Obtiene la distancia de la ultima ruta preparada.
     *
     * @return distancia en kilometros
     */
    public double getDistanciaKm() {
        return distanciaKm;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNombre() {
        return "Sistema de navegacion";
    }
}
