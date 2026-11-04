package co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

/**
 * Resultado de la coordinacion de una mision: los pasos en el orden en
 * que ocurrieron y si la mision pudo iniciar.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public final class ResultadoMision {

    /** Mensaje cuando la mision inicia. */
    public static final String MENSAJE_INICIADA = "MISION DE VIGILANCIA INICIADA";

    /** Prefijo del mensaje cuando la mision se cancela. */
    public static final String MENSAJE_CANCELADA = "MISION CANCELADA";

    /** Dron de la mision. */
    private final Drone drone;

    /** Pasos en orden. */
    private final List<PasoMision> pasos;

    /** Indica si la mision inicio. */
    private final boolean iniciada;

    /** Motivo de la cancelacion; nulo si la mision inicio. */
    private final String motivo;

    /**
     * Crea el resultado.
     *
     * @param drone    dron de la mision
     * @param pasos    pasos en orden
     * @param iniciada {@code true} si la mision inicio
     * @param motivo   motivo de la cancelacion, o {@code null}
     */
    public ResultadoMision(Drone drone, List<PasoMision> pasos, boolean iniciada, String motivo) {
        this.drone = drone;
        this.pasos = Collections.unmodifiableList(new ArrayList<>(pasos));
        this.iniciada = iniciada;
        this.motivo = motivo;
    }

    /**
     * Obtiene el dron de la mision.
     *
     * @return dron
     */
    public Drone getDrone() {
        return drone;
    }

    /**
     * Obtiene los pasos en el orden en que ocurrieron.
     *
     * @return lista de solo lectura
     */
    public List<PasoMision> getPasos() {
        return pasos;
    }

    /**
     * Indica si la mision inicio.
     *
     * @return {@code true} si todas las condiciones se cumplieron
     */
    public boolean isIniciada() {
        return iniciada;
    }

    /**
     * Obtiene el motivo de la cancelacion.
     *
     * @return motivo, o {@code null} si la mision inicio
     */
    public String getMotivo() {
        return motivo;
    }

    /**
     * Mensaje final de la mision.
     *
     * @return "MISION DE VIGILANCIA INICIADA" o "MISION CANCELADA: motivo"
     */
    public String getMensaje() {
        return iniciada ? MENSAJE_INICIADA : MENSAJE_CANCELADA + ": " + motivo;
    }
}
