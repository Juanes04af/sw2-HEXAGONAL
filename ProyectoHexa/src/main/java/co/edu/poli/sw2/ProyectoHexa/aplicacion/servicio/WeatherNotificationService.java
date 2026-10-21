package co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.GetWeatherByCityUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.NotificarClimaUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ResultadoNotificacion;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.Suscripcion;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.SuscripcionClimaUseCase;
import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Ciudad;
import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Weather;
import co.edu.poli.sw2.ProyectoHexa.dominio.clima.WeatherNotifier;
import co.edu.poli.sw2.ProyectoHexa.dominio.clima.WeatherObserver;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronException;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronValidacionException;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

/**
 * Servicio de aplicacion que administra el patron <b>Observer</b>.
 *
 * <p>
 * Crea un {@link WeatherNotifier} (sujeto) por cada {@link Ciudad} y
 * se encarga de suscribir y dar de baja drones en ellos. Al
 * ejecutar {@link #notificarSuscritos()}, consulta la API solo para
 * las ciudades con suscriptores y cada notificador avisa
 * automaticamente a sus drones.
 * </p>
 *
 * <p>
 * Regla de negocio: un dron pertenece como maximo a una ciudad. Las
 * suscripciones no se guardan en la base de datos.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class WeatherNotificationService
        implements SuscripcionClimaUseCase, NotificarClimaUseCase {

    /** Un sujeto por ciudad. */
    private final Map<Ciudad, WeatherNotifier> notificadores = new EnumMap<>(Ciudad.class);

    /** Caso de uso que consulta la API del clima. */
    private final GetWeatherByCityUseCase getWeatherByCity;

    /**
     * Construye el servicio y crea un notificador por ciudad.
     *
     * @param getWeatherByCity caso de uso para consultar el clima
     */
    public WeatherNotificationService(GetWeatherByCityUseCase getWeatherByCity) {
        this.getWeatherByCity = Objects.requireNonNull(getWeatherByCity);
        for (Ciudad ciudad : Ciudad.values()) {
            notificadores.put(ciudad, new WeatherNotifier(ciudad));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws DronValidacionException si falta el dron o la ciudad, o si
     *         el dron ya esta suscrito a esa misma ciudad
     */
    @Override
    public synchronized void suscribir(Drone drone, Ciudad ciudad) {
        if (drone == null) {
            throw new DronValidacionException("Seleccione un drone para suscribir.");
        }
        if (ciudad == null) {
            throw new DronValidacionException("Seleccione una ciudad.");
        }

        Optional<Ciudad> actual = ciudadDe(drone);
        if (actual.isPresent() && actual.get() == ciudad) {
            throw new DronValidacionException(
                    "El drone " + drone.getSerial() + " ya esta suscrito a " + ciudad + ".");
        }

        actual.ifPresent(anterior -> notificadores.get(anterior).desuscribir(drone));
        notificadores.get(ciudad).suscribir(drone);
    }

    /**
     * {@inheritDoc}
     *
     * @throws DronValidacionException si falta el dron o no esta suscrito
     */
    @Override
    public synchronized void desuscribir(Drone drone) {
        if (drone == null) {
            throw new DronValidacionException("Seleccione un drone para desuscribir.");
        }
        Ciudad ciudad = ciudadDe(drone).orElseThrow(() -> new DronValidacionException(
                "El drone " + drone.getSerial() + " no esta suscrito a ninguna ciudad."));
        notificadores.get(ciudad).desuscribir(drone);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void retirar(Drone drone) {
        if (drone != null) {
            ciudadDe(drone).ifPresent(c -> notificadores.get(c).desuscribir(drone));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void sincronizar(Drone drone) {
        if (drone == null) {
            return;
        }
        for (WeatherNotifier notifier : notificadores.values()) {
            for (WeatherObserver observer : notifier.getObservadores()) {
                if (observer.equals(drone) && observer != drone) {
                    Weather ultimo = ((Drone) observer).getClimaActual();
                    notifier.desuscribir(observer);
                    if (ultimo != null) {
                        drone.actualizarClima(ultimo);
                    }
                    notifier.suscribir(drone);
                    return;
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Optional<Ciudad> ciudadDe(Drone drone) {
        for (WeatherNotifier notifier : notificadores.values()) {
            if (notifier.estaSuscrito(drone)) {
                return Optional.of(notifier.getCiudad());
            }
        }
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized List<Suscripcion> listarSuscripciones() {
        List<Suscripcion> lista = new ArrayList<>();
        for (WeatherNotifier notifier : notificadores.values()) {
            for (WeatherObserver observer : notifier.getObservadores()) {
                if (observer instanceof Drone) {
                    lista.add(new Suscripcion((Drone) observer, notifier.getCiudad()));
                }
            }
        }
        return lista;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Si una ciudad falla, el error se registra en el resultado y se
     * continua con las demas.
     * </p>
     */
    @Override
    public ResultadoNotificacion notificarSuscritos() {
        ResultadoNotificacion resultado = new ResultadoNotificacion();

        for (WeatherNotifier notifier : notificadores.values()) {
            if (!notifier.tieneObservadores()) {
                continue;
            }
            Ciudad ciudad = notifier.getCiudad();
            try {
                Weather clima = getWeatherByCity.consultar(ciudad);
                notifier.notificar(clima);
                resultado.registrarExito(ciudad, clima);
            } catch (DronException e) {
                resultado.registrarError(ciudad, e.getMessage());
            }
        }
        return resultado;
    }

    /**
     * Obtiene el notificador de una ciudad. Uso principal: pruebas.
     *
     * @param ciudad ciudad buscada
     * @return notificador de la ciudad
     */
    public WeatherNotifier getNotificador(Ciudad ciudad) {
        return notificadores.get(ciudad);
    }
}
