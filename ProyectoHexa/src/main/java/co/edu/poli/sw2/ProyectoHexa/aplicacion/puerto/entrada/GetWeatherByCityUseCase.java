package co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada;

import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Ciudad;
import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Weather;

/**
 * Puerto de entrada para consultar el clima actual de una ciudad.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public interface GetWeatherByCityUseCase {

    /**
     * Consulta el clima actual de una ciudad.
     *
     * @param ciudad ciudad a consultar
     * @return clima actual
     */
    Weather consultar(Ciudad ciudad);
}
