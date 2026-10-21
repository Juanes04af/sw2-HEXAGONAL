package co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.salida;

import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Ciudad;
import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Weather;

/**
 * Puerto de salida para obtener el clima actual de una ciudad.
 *
 * <p>
 * La aplicacion solo conoce esta interfaz; la implementacion concreta
 * (las APIs de Open-Meteo, HTTP y JSON) vive en la capa de
 * infraestructura.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public interface WeatherProvider {

    /**
     * Obtiene las condiciones climaticas actuales de una ciudad.
     *
     * @param ciudad ciudad a consultar
     * @return clima actual con los cinco aspectos
     * @throws co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.ClimaException
     *         si no es posible obtener el clima
     */
    Weather obtenerClima(Ciudad ciudad);
}
