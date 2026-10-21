package co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada;

/**
 * Puerto de entrada que ejecuta la consulta a la API externa y
 * dispara las notificaciones del patron Observer.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public interface NotificarClimaUseCase {

    /**
     * Consulta el clima de cada ciudad que tenga al menos un dron
     * suscrito y notifica a todos sus suscriptores. Las ciudades sin
     * suscriptores no se consultan.
     *
     * @return clima obtenido y errores, por ciudad
     */
    ResultadoNotificacion notificarSuscritos();
}
