package co.edu.poli.sw2.ProyectoHexa.dominio.modelo;

import java.io.Serializable;

/**
 * Entidad abstracta que representa un dron dentro del dominio.
 * Contiene los atributos comunes compartidos por los diferentes
 * tipos de drones.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public abstract class Drone implements Serializable {

    private static final long serialVersionUID = 3L;

    private int id;
    private String serial;
    private String fabricante;
    private String modelo;
    private double peso;

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
}