package co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio;

import java.util.logging.Logger;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ValidarClimaUseCase;
import co.edu.poli.sw2.ProyectoHexa.dominio.chain.CadenaValidacionClima;
import co.edu.poli.sw2.ProyectoHexa.dominio.chain.EvaluacionClima;
import co.edu.poli.sw2.ProyectoHexa.dominio.chain.WeatherHandler;
import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Weather;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronValidacionException;

/**
 * Caso de uso que valida el clima con la cadena de responsabilidad.
 * Registra en el log el motivo de cada rechazo. No usa base de datos.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class ValidarClimaServicio implements ValidarClimaUseCase {

    /** Registro donde se anota el motivo de los rechazos. */
    private static final Logger LOG = Logger.getLogger(ValidarClimaServicio.class.getName());

    /** Primer eslabon de la cadena. */
    private final WeatherHandler cadena = CadenaValidacionClima.crear();

    /**
     * {@inheritDoc}
     *
     * @throws DronValidacionException si el clima es nulo
     */
    @Override
    public EvaluacionClima validar(Weather clima) {
        if (clima == null) {
            throw new DronValidacionException("No hay clima para validar.");
        }

        String motivo = cadena.validar(clima);

        if (motivo == null) {
            return EvaluacionClima.aprobada();
        }

        LOG.warning("Clima rechazado: " + motivo);   // se registra el motivo
        return EvaluacionClima.rechazada(motivo);
    }
}
