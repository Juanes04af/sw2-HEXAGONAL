package co.edu.poli.sw2.ProyectoHexa.dominio.clima;

/**
 * Ciudades disponibles para consultar el clima.
 *
 * <p>
 * Los valores estan definidos directamente en el codigo. Cada ciudad
 * guarda el nombre con el que se busca en la API de geocodificacion y
 * el codigo ISO de su pais, que evita confundirla con otra ciudad del
 * mismo nombre.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public enum Ciudad {

    /** Bogota, Colombia. */
    BOGOTA("Bogota", "CO", "Bogota (Colombia)"),

    /** Zagreb, Croacia. */
    ZAGREB("Zagreb", "HR", "Zagreb (Croacia)"),

    /** Madrid, Espana. */
    MADRID("Madrid", "ES", "Madrid (Espana)"),

    /** Tokio, Japon. */
    TOKIO("Tokyo", "JP", "Tokio (Japon)"),

    /** Buenos Aires, Argentina. */
    BUENOS_AIRES("Buenos Aires", "AR", "Buenos Aires (Argentina)");

    /** Nombre usado en la consulta de geocodificacion. */
    private final String nombreConsulta;

    /** Codigo ISO 3166-1 alfa-2 del pais. */
    private final String codigoPais;

    /** Nombre que se muestra en la interfaz. */
    private final String nombreVisible;

    /**
     * Crea una ciudad.
     *
     * @param nombreConsulta nombre usado en la consulta a la API
     * @param codigoPais     codigo ISO del pais
     * @param nombreVisible  nombre mostrado al usuario
     */
    Ciudad(String nombreConsulta, String codigoPais, String nombreVisible) {
        this.nombreConsulta = nombreConsulta;
        this.codigoPais = codigoPais;
        this.nombreVisible = nombreVisible;
    }

    /**
     * Obtiene el nombre usado en la consulta de geocodificacion.
     *
     * @return nombre de busqueda
     */
    public String getNombreConsulta() {
        return nombreConsulta;
    }

    /**
     * Obtiene el codigo ISO del pais.
     *
     * @return codigo de pais de dos letras
     */
    public String getCodigoPais() {
        return codigoPais;
    }

    /**
     * Obtiene el nombre mostrado al usuario.
     *
     * @return nombre visible
     */
    public String getNombreVisible() {
        return nombreVisible;
    }

    /**
     * Devuelve el nombre visible, usado por los ComboBox de JavaFX.
     *
     * @return nombre visible de la ciudad
     */
    @Override
    public String toString() {
        return nombreVisible;
    }
}
