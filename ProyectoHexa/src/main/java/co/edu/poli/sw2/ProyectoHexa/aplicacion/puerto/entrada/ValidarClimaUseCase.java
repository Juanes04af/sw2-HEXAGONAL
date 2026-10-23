package co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada;

import co.edu.poli.sw2.ProyectoHexa.dominio.chain.EvaluacionClima;
import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Weather;

/**
 * Puerto de entrada que valida si un clima permite operar drones.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public interface ValidarClimaUseCase {

    /**
     * Valida el clima con la cadena de responsabilidad.
     *
     * @param clima condiciones climaticas a evaluar
     * @return resultado: apto, o no apto con la regla incumplida
     */
    EvaluacionClima validar(Weather clima);
}
