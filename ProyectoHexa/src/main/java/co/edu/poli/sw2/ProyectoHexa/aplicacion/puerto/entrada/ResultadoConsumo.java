package co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada;

import java.util.Locale;

import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.strategy.ConsumoStrategy;
import co.edu.poli.sw2.ProyectoHexa.dominio.strategy.Ruta;

/**
 * Resultado de un calculo de consumo: dron, ruta, distancia,
 * estrategia aplicada y bateria requerida.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public final class ResultadoConsumo {

    /** Dron que hara el recorrido. */
    private final Drone drone;

    /** Ruta recorrida. */
    private final Ruta ruta;

    /** Distancia de la ruta en km. */
    private final double distanciaKm;

    /** Estrategia aplicada. */
    private final ConsumoStrategy estrategia;

    /** Bateria requerida. */
    private final double consumo;

    /**
     * Crea el resultado.
     *
     * @param drone       dron que hara el recorrido
     * @param ruta        ruta recorrida
     * @param distanciaKm distancia en kilometros
     * @param estrategia  estrategia aplicada
     * @param consumo     bateria requerida
     */
    public ResultadoConsumo(Drone drone, Ruta ruta, double distanciaKm,
                            ConsumoStrategy estrategia, double consumo) {
        this.drone = drone;
        this.ruta = ruta;
        this.distanciaKm = distanciaKm;
        this.estrategia = estrategia;
        this.consumo = consumo;
    }

    /**
     * Obtiene el dron.
     *
     * @return dron
     */
    public Drone getDrone() {
        return drone;
    }

    /**
     * Obtiene la ruta.
     *
     * @return ruta
     */
    public Ruta getRuta() {
        return ruta;
    }

    /**
     * Obtiene la distancia.
     *
     * @return distancia en kilometros
     */
    public double getDistanciaKm() {
        return distanciaKm;
    }

    /**
     * Obtiene la estrategia aplicada.
     *
     * @return estrategia
     */
    public ConsumoStrategy getEstrategia() {
        return estrategia;
    }

    /**
     * Obtiene la bateria requerida.
     *
     * @return consumo en unidades de bateria
     */
    public double getConsumo() {
        return consumo;
    }

    /**
     * Describe la operacion realizada.
     *
     * @return por ejemplo "12.51 km x 0.5 = 6.26"
     */
    public String describirCalculo() {
        return String.format(Locale.US, "%.2f km x %.1f = %.2f",
                distanciaKm, estrategia.getFactor(), consumo);
    }
}
