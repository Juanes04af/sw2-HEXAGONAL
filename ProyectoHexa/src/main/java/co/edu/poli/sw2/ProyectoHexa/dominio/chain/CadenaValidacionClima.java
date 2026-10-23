package co.edu.poli.sw2.ProyectoHexa.dominio.chain;

/**
 * Construye la cadena oficial de validacion climatica. El orden es una
 * regla de negocio, por eso se define en el dominio:
 * Temperature, Humidity, Wind, Pressure, Visibility.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public final class CadenaValidacionClima {

    /** Evita crear instancias de la utilidad. */
    private CadenaValidacionClima() {
    }

    /**
     * Crea la cadena completa.
     *
     * @return el primer eslabon ({@link TemperatureHandler})
     */
    public static WeatherHandler crear() {
        WeatherHandler primero = new TemperatureHandler();
        primero.setSiguiente(new HumidityHandler())
               .setSiguiente(new WindHandler())
               .setSiguiente(new PressureHandler())
               .setSiguiente(new VisibilityHandler());
        return primero;
    }
}
