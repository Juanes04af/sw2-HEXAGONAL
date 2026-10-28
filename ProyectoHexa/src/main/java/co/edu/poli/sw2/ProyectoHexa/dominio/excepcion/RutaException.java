package co.edu.poli.sw2.ProyectoHexa.dominio.excepcion;

/**
 * Excepcion lanzada cuando la ruta de vuelo no es valida o no se
 * puede cargar, por ejemplo si el archivo JSON falta, esta mal
 * formado o trae coordenadas fuera de rango.
 *
 * <p>
 * Hereda de {@link DronException} para que la interfaz la maneje con
 * el mismo bloque {@code catch} que el resto de errores del dominio.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class RutaException extends DronException {

    private static final long serialVersionUID = 1L;

    /**
     * Construye la excepcion con mensaje.
     *
     * @param mensaje descripcion del error
     */
    public RutaException(String mensaje) {
        super(mensaje);
    }

    /**
     * Construye la excepcion con mensaje y causa.
     *
     * @param mensaje descripcion del error
     * @param causa   causa original
     */
    public RutaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
