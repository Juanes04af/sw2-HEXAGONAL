package co.edu.poli.sw2.ProyectoHexa.infraestructura.web;

import java.io.IOException;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.BuscarDroneUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.CrearDroneUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.EliminarDroneUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.GetWeatherByCityUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ListarDronesUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ModificarDroneUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ValidarClimaUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.salida.DroneRepository;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.salida.WeatherProvider;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio.BuscarDroneServicio;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio.CrearDroneServicio;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio.EliminarDroneServicio;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio.GetWeatherByCityServicio;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio.ListarDronesServicio;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio.ModificarDroneServicio;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio.ValidarClimaServicio;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio.WeatherNotificationService;
import co.edu.poli.sw2.ProyectoHexa.infraestructura.persistencia.PostgresDroneRepository;
import co.edu.poli.sw2.ProyectoHexa.infraestructura.ui.ControlClima;
import co.edu.poli.sw2.ProyectoHexa.infraestructura.ui.ControlDrone;
import co.edu.poli.sw2.ProyectoHexa.infraestructura.weather.GeoApiAdapter;
import co.edu.poli.sw2.ProyectoHexa.infraestructura.weather.GeoApiClient;
import co.edu.poli.sw2.ProyectoHexa.infraestructura.weather.OpenWeatherAdapter;
import co.edu.poli.sw2.ProyectoHexa.infraestructura.weather.OpenWeatherClient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Punto de entrada principal. Realiza el wiring de la arquitectura
 * hexagonal: adaptadores de salida, casos de uso y adaptadores de entrada.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class App extends Application {

    /**
     * Construye las dependencias y carga la interfaz JavaFX.
     *
     * @param stage escenario principal
     * @throws IOException si no se puede cargar el FXML
     */
    @Override
    public void start(Stage stage) throws IOException {

        /* ADAPTADOR DE SALIDA: base de datos */
        DroneRepository droneRepository = new PostgresDroneRepository();

        /* CASOS DE USO DE DRONES */
        CrearDroneUseCase crearDrone = new CrearDroneServicio(droneRepository);
        ModificarDroneUseCase modificarDrone = new ModificarDroneServicio(droneRepository);
        EliminarDroneUseCase eliminarDrone = new EliminarDroneServicio(droneRepository);
        BuscarDroneUseCase buscarDrone = new BuscarDroneServicio(droneRepository);
        ListarDronesUseCase listarDrones = new ListarDronesServicio(droneRepository);

        /* OBSERVER: adaptador de salida hacia Open-Meteo y servicio de suscripciones */
        WeatherProvider weatherProvider = new OpenWeatherAdapter(
                new GeoApiAdapter(new GeoApiClient()),
                new OpenWeatherClient());

        GetWeatherByCityUseCase getWeatherByCity = new GetWeatherByCityServicio(weatherProvider);
        WeatherNotificationService notificacionClima = new WeatherNotificationService(getWeatherByCity);

        /* CHAIN OF RESPONSIBILITY: validacion de la viabilidad de vuelo */
        ValidarClimaUseCase validarClima = new ValidarClimaServicio();

        /* CARGADOR FXML (drone.fxml incluye clima.fxml en su segunda pestana) */
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/co/edu/poli/sw2/vista/drone.fxml"));

        /* INYECCION de los puertos en los adaptadores de entrada */
        loader.setControllerFactory(controllerClass -> {

            if (controllerClass == ControlDrone.class) {
                return new ControlDrone(crearDrone, modificarDrone, eliminarDrone,
                        buscarDrone, listarDrones, notificacionClima);
            }

            if (controllerClass == ControlClima.class) {
                return new ControlClima(notificacionClima, notificacionClima,
                        listarDrones, validarClima);
            }

            try {
                return controllerClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("No fue posible crear el controlador.", e);
            }
        });

        Parent root = loader.load();

        stage.setTitle("Gestion de Drones - Arquitectura Hexagonal");
        stage.setScene(new Scene(root, 1100, 720));
        stage.show();
    }

    /**
     * Inicia la aplicacion JavaFX.
     *
     * @param args argumentos de linea de comandos
     */
    public static void main(String[] args) {
        launch(args);
    }
}
