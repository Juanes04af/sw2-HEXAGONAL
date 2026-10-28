package co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.salida;

import co.edu.poli.sw2.ProyectoHexa.dominio.strategy.Ruta;

/**
 * Puerto de salida para obtener la ruta de vuelo (origen y destino).
 *
 * <p>
 * La aplicacion no sabe de donde viene la ruta; la implementacion
 * concreta (lectura de un archivo JSON) vive en la infraestructura.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public interface RutaLoader {

    /**
     * Carga la ruta de vuelo.
     *
     * @return ruta con origen y destino
     * @throws co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.RutaException
     *         si no es posible cargarla
     */
    Ruta cargarRuta();
}
