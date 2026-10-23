package co.edu.poli.sw2.ProyectoHexa.dominio.chain;

/**
 * Resultado de validar un clima con la cadena de responsabilidad.
 * Es inmutable y no se guarda en la base de datos.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public final class EvaluacionClima {

    /** Mensaje cuando todos los handlers aprueban. */
    public static final String MENSAJE_APTO = "CLIMA APTO PARA OPERACIÓN DE DRONES";

    /** Mensaje cuando algun handler rechaza. */
    public static final String MENSAJE_NO_APTO = "CLIMA NO APTO PARA OPERACIÓN DE DRONES";

    /** Regla incumplida; {@code null} si el clima es apto. */
    private final String motivo;

    private EvaluacionClima(String motivo) {
        this.motivo = motivo;
    }

    /** @return evaluacion de un clima apto */
    public static EvaluacionClima aprobada() {
        return new EvaluacionClima(null);
    }

    /**
     * @param motivo regla incumplida
     * @return evaluacion de un clima no apto
     */
    public static EvaluacionClima rechazada(String motivo) {
        return new EvaluacionClima(motivo);
    }

    /** @return {@code true} si el clima permite volar */
    public boolean isApto() {
        return motivo == null;
    }

    /** @return regla incumplida, o {@code null} si es apto */
    public String getMotivo() {
        return motivo;
    }

    /** @return mensaje final, con la regla incumplida si no es apto */
    public String getMensaje() {
        return isApto() ? MENSAJE_APTO : MENSAJE_NO_APTO + " — " + motivo;
    }
}
