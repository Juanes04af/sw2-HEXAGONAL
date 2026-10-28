package co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada;

import java.util.List;

import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.strategy.ConsumoStrategy;
import co.edu.poli.sw2.ProyectoHexa.dominio.strategy.Ruta;

/**
 * Puerto de entrada para estimar la bateria que un dron necesita
 * para recorrer la ruta cargada, segun la estrategia de consumo
 * seleccionada.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public interface CalcularConsumoUseCase {

    /**
     * Obtiene la ruta cargada (origen y destino).
     *
     * @return ruta de vuelo
     */
    Ruta obtenerRuta();

    /**
     * Obtiene la distancia de la ruta cargada.
     *
     * @return distancia en kilometros
     */
    double obtenerDistanciaKm();

    /**
     * Lista las estrategias de consumo disponibles.
     *
     * @return estrategias en orden: ahorro, normal y alto rendimiento
     */
    List<ConsumoStrategy> estrategiasDisponibles();

    /**
     * Cambia la estrategia con la que se calculara el consumo.
     *
     * @param estrategia estrategia elegida
     */
    void setEstrategia(ConsumoStrategy estrategia);

    /**
     * Calcula la bateria que necesita un dron para la ruta, usando la
     * estrategia seleccionada.
     *
     * @param drone dron que hara el recorrido
     * @return resultado con distancia, estrategia y consumo
     */
    ResultadoConsumo calcular(Drone drone);
}
