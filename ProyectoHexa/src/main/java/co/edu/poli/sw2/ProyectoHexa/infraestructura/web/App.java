package co.edu.poli.sw2.ProyectoHexa.infraestructura.web;

import java.io.IOException;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.BuscarDroneUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.CrearDroneUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.EliminarDroneUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ListarDronesUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ModificarDroneUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.salida.DroneRepository;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio.BuscarDroneServicio;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio.CrearDroneServicio;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio.EliminarDroneServicio;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio.ListarDronesServicio;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio.ModificarDroneServicio;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.GetWeatherByCityUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.salida.WeatherProvider;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio.GetWeatherByCityServicio;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio.WeatherNotificationService;
import co.edu.poli.sw2.ProyectoHexa.infraestructura.weather.GeoApiAdapter;
import co.edu.poli.sw2.ProyectoHexa.infraestructura.weather.GeoApiClient;
import co.edu.poli.sw2.ProyectoHexa.infraestructura.weather.OpenWeatherAdapter;
import co.edu.poli.sw2.ProyectoHexa.infraestructura.weather.OpenWeatherClient;
import co.edu.poli.sw2.ProyectoHexa.infraestructura.persistencia.PostgresDroneRepository;
import co.edu.poli.sw2.ProyectoHexa.infraestructura.ui.ControlClima;
import co.edu.poli.sw2.ProyectoHexa.infraestructura.ui.ControlDrone;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Punto de entrada principal de la aplicacion.
 *
 * <p>
 * Esta clase realiza el wiring de la arquitectura hexagonal.
 * Construye el adaptador de salida, los servicios de aplicacion
 * y finalmente el adaptador de entrada JavaFX.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class App extends Application {

    /**
     * Construye las dependencias de la aplicacion y carga
     * la interfaz JavaFX.
     *
     * @param stage escenario principal de JavaFX
     * @throws IOException si no es posible cargar el archivo FXML
     */
    @Override
    public void start(Stage stage)
            throws IOException {

        /*
         * ADAPTADOR DE SALIDA
         */
        DroneRepository droneRepository =
                new PostgresDroneRepository();

        /*
         * SERVICIOS / CASOS DE USO
         */
        CrearDroneUseCase crearDrone =
                new CrearDroneServicio(
                        droneRepository);

        ModificarDroneUseCase modificarDrone =
                new ModificarDroneServicio(
                        droneRepository);

        EliminarDroneUseCase eliminarDrone =
                new EliminarDroneServicio(
                        droneRepository);

        BuscarDroneUseCase buscarDrone =
                new BuscarDroneServicio(
                        droneRepository);

        ListarDronesUseCase listarDrones =
                new ListarDronesServicio(
                        droneRepository);

        /*
         * Clima (patron Observer): adaptador de salida hacia las
         * APIs de Open-Meteo, caso de uso de consulta y servicio que
         * administra un notificador por ciudad.
         */
        WeatherProvider weatherProvider =
                new OpenWeatherAdapter(
                        new GeoApiAdapter(new GeoApiClient()),
                        new OpenWeatherClient());

        GetWeatherByCityUseCase getWeatherByCity =
                new GetWeatherByCityServicio(
                        weatherProvider);

        WeatherNotificationService notificacionClima =
                new WeatherNotificationService(
                        getWeatherByCity);

        /*
         * CARGADOR FXML
         */
        FXMLLoader loader =
                new FXMLLoader(
                        getClass().getResource(
                                "/co/edu/poli/sw2/vista/drone.fxml"));

        /*
         * INYECCION DE LOS PUERTOS EN EL
         * ADAPTADOR DE ENTRADA
         */
        loader.setControllerFactory(
                controllerClass -> {

                    if (controllerClass
                            == ControlDrone.class) {

                        return new ControlDrone(
                                crearDrone,
                                modificarDrone,
                                eliminarDrone,
                                buscarDrone,
                                listarDrones,
                                notificacionClima);
                    }

                    if (controllerClass
                            == ControlClima.class) {

                        return new ControlClima(
                                notificacionClima,
                                notificacionClima,
                                listarDrones);
                    }

                    try {

                        return controllerClass
                                .getDeclaredConstructor()
                                .newInstance();

                    } catch (Exception e) {

                        throw new RuntimeException(
                                "No fue posible crear el controlador.",
                                e);
                    }
                });

        Parent root =
                loader.load();

        Scene scene =
                new Scene(
                        root,
                        1100,
                        720);

        stage.setTitle(
                "Gestion de Drones - Arquitectura Hexagonal");

        stage.setScene(scene);

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