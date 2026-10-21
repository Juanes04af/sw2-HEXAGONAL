package co.edu.poli.sw2.ProyectoHexa.dominio.modelo;

import java.io.Serializable;

import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Weather;
import co.edu.poli.sw2.ProyectoHexa.dominio.clima.WeatherObserver;

/**
 * Entidad abstracta que representa un dron dentro del dominio.
 * Contiene los atributos comunes compartidos por los diferentes
 * tipos de drones.
 *
 * <p>
 * Cumple el rol de <b>observador</b> en el patron Observer: al
 * suscribirse al notificador de una ciudad, recibe automaticamente
 * cada actualizacion del clima mediante {@link #actualizarClima(Weather)}.
 * El ultimo clima recibido se conserva solo en memoria; no se guarda
 * en la base de datos.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public abstract class Drone implements Serializable, WeatherObserver {

    private static final long serialVersionUID = 3L;

    private int id;
    private String serial;
    private String fabricante;
    private String modelo;
    private double peso;

    /** Ultimo clima notificado. Es transitorio: no se persiste. */
    private transient Weather climaActual;

    /**
     * Construye un dron con sus datos generales.
     *
     * @param id identificador del dron
     * @param serial serial unico del dron
     * @param fabricante fabricante del dron
     * @param modelo modelo comercial
     * @param peso peso en kilogramos
     */
    protected Drone(int id, String serial, String fabricante,
                    String modelo, double peso) {
        this.id = id;
        this.serial = serial;
        this.fabricante = fabricante;
        this.modelo = modelo;
        this.peso = peso;
    }

    /**
     * Obtiene el identificador.
     *
     * @return identificador del dron
     */
    public int getId() {
        return id;
    }

    /**
     * Modifica el identificador.
     *
     * @param id nuevo identificador
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Obtiene el serial.
     *
     * @return serial del dron
     */
    public String getSerial() {
        return serial;
    }

    /**
     * Modifica el serial.
     *
     * @param serial nuevo serial
     */
    public void setSerial(String serial) {
        this.serial = serial;
    }

    /**
     * Obtiene el fabricante.
     *
     * @return fabricante del dron
     */
    public String getFabricante() {
        return fabricante;
    }

    /**
     * Modifica el fabricante.
     *
     * @param fabricante nuevo fabricante
     */
    public void setFabricante(String fabricante) {
        this.fabricante = fabricante;
    }

    /**
     * Obtiene el modelo.
     *
     * @return modelo del dron
     */
    public String getModelo() {
        return modelo;
    }

    /**
     * Modifica el modelo.
     *
     * @param modelo nuevo modelo
     */
    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    /**
     * Obtiene el peso.
     *
     * @return peso en kilogramos
     */
    public double getPeso() {
        return peso;
    }

    /**
     * Modifica el peso.
     *
     * @param peso nuevo peso
     */
    public void setPeso(double peso) {
        this.peso = peso;
    }

    /**
     * Devuelve el tipo concreto de dron.
     *
     * @return nombre del tipo de dron
     */
    public abstract String getTipo();
    /**
     * Recibe la notificacion del clima de la ciudad a la que el dron
     * esta suscrito y la guarda como su clima actual.
     *
     * @param clima condiciones climaticas notificadas
     */
    @Override
    public void actualizarClima(Weather clima) {
        this.climaActual = clima;
    }

    /**
     * Obtiene el ultimo clima recibido.
     *
     * @return clima actual, o {@code null} si aun no ha recibido ninguno
     */
    public Weather getClimaActual() {
        return climaActual;
    }

    /**
     * Dos drones son iguales si son del mismo tipo y tienen el mismo id.
     *
     * <p>
     * Cada consulta a la base de datos crea objetos nuevos; comparar por
     * id permite reconocer al mismo dron al suscribirlo o desuscribirlo.
     * Los drones sin guardar (id 0) solo son iguales a si mismos.
     * </p>
     *
     * @param otro objeto a comparar
     * @return {@code true} si representan el mismo dron
     */
    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (otro == null || getClass() != otro.getClass() || id == 0) {
            return false;
        }
        return id == ((Drone) otro).id;
    }

    /**
     * Calcula el hash a partir del tipo y el id, coherente con
     * {@link #equals(Object)}.
     *
     * @return codigo hash
     */
    @Override
    public int hashCode() {
        return id == 0 ? System.identityHashCode(this) : 31 * getClass().hashCode() + id;
    }
}
