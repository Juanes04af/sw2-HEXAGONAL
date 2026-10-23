package co.edu.poli.sw2.ProyectoHexa.infraestructura.ui;

import java.net.URL;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Function;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ListarDronesUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.NotificarClimaUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ResultadoNotificacion;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.Suscripcion;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.SuscripcionClimaUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ValidarClimaUseCase;
import co.edu.poli.sw2.ProyectoHexa.dominio.chain.EvaluacionClima;
import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Ciudad;
import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Weather;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronException;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.StringConverter;

/**
 * Adaptador de entrada JavaFX de la pestana Clima.
 *
 * <p>
 * Permite suscribir o dar de baja drones en el notificador del clima de
 * una ciudad (Observer) y consultar las APIs externas. Al terminar, la
 * tabla muestra el clima notificado a cada dron y si ese clima le
 * permite volar, segun la cadena de responsabilidad (Chain).
 * </p>
 *
 * <p>
 * La consulta se ejecuta en un hilo de fondo para no congelar la ventana.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class ControlClima implements Initializable {

    /** Texto mostrado en las celdas sin clima notificado. */
    private static final String SIN_DATO = "-";

    /** Selector del dron. */
    @FXML private ComboBox<Drone> cmbDrone;

    /** Selector de la ciudad. */
    @FXML private ComboBox<Ciudad> cmbCiudad;

    /** Boton que dispara la consulta y la notificacion. */
    @FXML private Button btnConsultar;

    /** Indicador visible mientras se consulta la API. */
    @FXML private ProgressIndicator indicadorCarga;

    /** Etiqueta de mensajes al usuario. */
    @FXML private Label lblMensaje;

    /** Tabla de drones suscritos y su clima. */
    @FXML private TableView<Suscripcion> tablaSuscripciones;

    @FXML private TableColumn<Suscripcion, String> colId;
    @FXML private TableColumn<Suscripcion, String> colSerial;
    @FXML private TableColumn<Suscripcion, String> colCiudad;
    @FXML private TableColumn<Suscripcion, String> colTemperatura;
    @FXML private TableColumn<Suscripcion, String> colHumedad;
    @FXML private TableColumn<Suscripcion, String> colViento;
    @FXML private TableColumn<Suscripcion, String> colPresion;
    @FXML private TableColumn<Suscripcion, String> colVisibilidad;

    /** Columna nueva: indica si el clima notificado permite volar (Si/No). */
    @FXML private TableColumn<Suscripcion, String> colPuedeVolar;

    /** Columna nueva: regla incumplida cuando no puede volar. */
    @FXML private TableColumn<Suscripcion, String> colMotivo;

    /** Caso de uso para suscribir y desuscribir drones. */
    private final SuscripcionClimaUseCase suscripcionClima;

    /** Caso de uso que consulta la API y notifica. */
    private final NotificarClimaUseCase notificarClima;

    /** Caso de uso para listar los drones registrados. */
    private final ListarDronesUseCase listarDrones;

    /** Caso de uso que valida el clima con la cadena de responsabilidad. */
    private final ValidarClimaUseCase validarClima;

    /**
     * Evaluaciones ya calculadas, por instancia de clima. Evita validar
     * (y registrar en el log) lo mismo cada vez que la tabla se repinta.
     */
    private final Map<Weather, EvaluacionClima> evaluaciones = new IdentityHashMap<>();

    /**
     * Construye el controlador con los casos de uso que necesita.
     *
     * @param suscripcionClima caso de uso de suscripciones
     * @param notificarClima   caso de uso de notificacion
     * @param listarDrones     caso de uso de listado de drones
     * @param validarClima     caso de uso de validacion de vuelo
     */
    public ControlClima(SuscripcionClimaUseCase suscripcionClima,
                        NotificarClimaUseCase notificarClima,
                        ListarDronesUseCase listarDrones,
                        ValidarClimaUseCase validarClima) {
        this.suscripcionClima = suscripcionClima;
        this.notificarClima = notificarClima;
        this.listarDrones = listarDrones;
        this.validarClima = validarClima;
    }

    /**
     * Configura los selectores y la tabla despues de cargar el FXML.
     *
     * @param location  ubicacion del recurso
     * @param resources recursos de internacionalizacion
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmbCiudad.setItems(FXCollections.observableArrayList(Ciudad.values()));
        cmbDrone.setConverter(new ConvertidorDrone());

        // La lista de drones se recarga cada vez que se abre el selector.
        cmbDrone.setOnShowing(evento -> cargarDrones());

        // Al elegir un dron, se muestra la ciudad a la que ya esta suscrito.
        cmbDrone.valueProperty().addListener((obs, anterior, nuevo) -> {
            if (nuevo != null) {
                suscripcionClima.ciudadDe(nuevo).ifPresent(cmbCiudad::setValue);
            }
        });

        // Al elegir una fila, se carga en los selectores para desuscribir rapido.
        tablaSuscripciones.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, nueva) -> {
                    if (nueva != null) {
                        cmbDrone.setValue(nueva.getDrone());
                        cmbCiudad.setValue(nueva.getCiudad());
                    }
                });

        configurarTabla();
        cargarDrones();
        refrescarTabla();
    }

    /**
     * Configura las columnas: los cinco datos del clima, mas "Puede volar"
     * y el motivo del rechazo.
     */
    private void configurarTabla() {
        colId.setCellValueFactory(d ->
                new SimpleStringProperty(String.valueOf(d.getValue().getDrone().getId())));
        colSerial.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getDrone().getSerial()));
        colCiudad.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getCiudad().getNombreVisible()));

        colTemperatura.setCellValueFactory(d ->
                celdaClima(d.getValue(), c -> formato("%.1f \u00B0C", c.getTemperatura())));
        colHumedad.setCellValueFactory(d ->
                celdaClima(d.getValue(), c -> formato("%.0f %%", c.getHumedad())));
        colViento.setCellValueFactory(d ->
                celdaClima(d.getValue(), c -> formato("%.1f km/h", c.getVelocidadViento())));
        colPresion.setCellValueFactory(d ->
                celdaClima(d.getValue(), c -> formato("%.1f hPa", c.getPresion())));
        colVisibilidad.setCellValueFactory(d ->
                celdaClima(d.getValue(), c -> formato("%.1f km", c.getVisibilidad())));

        // Resultado de la cadena de responsabilidad sobre el clima del dron.
        colPuedeVolar.setCellValueFactory(d ->
                celdaClima(d.getValue(), c -> evaluar(c).isApto() ? "Si" : "No"));
        colMotivo.setCellValueFactory(d ->
                celdaClima(d.getValue(), c -> {
                    EvaluacionClima evaluacion = evaluar(c);
                    return evaluacion.isApto() ? SIN_DATO : evaluacion.getMotivo();
                }));
    }

    /** Suscribe el dron seleccionado a la ciudad seleccionada. */
    @FXML
    private void suscribir() {
        try {
            Drone drone = cmbDrone.getValue();
            Ciudad ciudad = cmbCiudad.getValue();
            boolean trasladado = drone != null && suscripcionClima.ciudadDe(drone).isPresent();

            suscripcionClima.suscribir(drone, ciudad);
            refrescarTabla();

            mostrarMensaje("Drone " + drone.getSerial()
                    + (trasladado ? " trasladado a " : " suscrito a ") + ciudad + ".", false);

        } catch (DronException e) {
            mostrarMensaje(e.getMessage(), true);
        }
    }

    /** Da de baja el dron seleccionado de su ciudad. */
    @FXML
    private void desuscribir() {
        try {
            Drone drone = cmbDrone.getValue();
            suscripcionClima.desuscribir(drone);
            refrescarTabla();
            mostrarMensaje("Drone " + drone.getSerial() + " desuscrito.", false);

        } catch (DronException e) {
            mostrarMensaje(e.getMessage(), true);
        }
    }

    /**
     * Consulta el clima de las ciudades con drones suscritos y notifica a
     * cada uno. Se ejecuta en un hilo de fondo.
     */
    @FXML
    private void consultarClima() {
        if (suscripcionClima.listarSuscripciones().isEmpty()) {
            mostrarMensaje("No hay drones suscritos. Suscriba al menos uno.", true);
            return;
        }

        Task<ResultadoNotificacion> tarea = new Task<>() {
            @Override
            protected ResultadoNotificacion call() {
                return notificarClima.notificarSuscritos();
            }
        };

        tarea.setOnSucceeded(evento -> {
            terminarCarga();
            evaluaciones.clear();            // llego clima nuevo: se evalua de nuevo
            refrescarTabla();
            mostrarResultado(tarea.getValue());
        });

        tarea.setOnFailed(evento -> {
            terminarCarga();
            Throwable error = tarea.getException();
            mostrarMensaje("Error al consultar el clima: "
                    + (error == null ? "desconocido" : error.getMessage()), true);
        });

        btnConsultar.setDisable(true);
        indicadorCarga.setVisible(true);
        mostrarMensaje("Consultando el clima...", false);

        Thread hilo = new Thread(tarea, "consulta-clima");
        hilo.setDaemon(true);
        hilo.start();
    }

    /**
     * Informa, por ciudad, "CLIMA APTO..." o "CLIMA NO APTO..." con la
     * regla incumplida, y los errores de consulta si los hubo.
     *
     * @param resultado resultado de la ronda de notificaciones
     */
    private void mostrarResultado(ResultadoNotificacion resultado) {
        StringBuilder texto = new StringBuilder();
        boolean hayProblema = false;

        for (Map.Entry<Ciudad, Weather> entrada : resultado.getNotificadas().entrySet()) {
            EvaluacionClima evaluacion = evaluar(entrada.getValue());
            if (texto.length() > 0) {
                texto.append("\n");
            }
            texto.append(entrada.getKey()).append(": ").append(evaluacion.getMensaje());
            hayProblema |= !evaluacion.isApto();
        }

        for (Map.Entry<Ciudad, String> error : resultado.getErrores().entrySet()) {
            if (texto.length() > 0) {
                texto.append("\n");
            }
            texto.append(error.getKey()).append(": ").append(error.getValue());
            hayProblema = true;
        }

        mostrarMensaje(texto.toString(), hayProblema);
    }

    /**
     * Evalua un clima con la cadena, una sola vez por instancia.
     *
     * @param clima clima notificado
     * @return evaluacion de la cadena de responsabilidad
     */
    private EvaluacionClima evaluar(Weather clima) {
        return evaluaciones.computeIfAbsent(clima, validarClima::validar);
    }

    /** Recarga la lista de drones registrados, conservando la seleccion. */
    private void cargarDrones() {
        try {
            Drone seleccionado = cmbDrone.getValue();
            List<Drone> drones = listarDrones.listar();
            cmbDrone.setItems(FXCollections.observableArrayList(drones));

            if (seleccionado != null) {
                drones.stream()
                      .filter(seleccionado::equals)
                      .findFirst()
                      .ifPresent(cmbDrone::setValue);
            }
        } catch (DronException e) {
            mostrarMensaje(e.getMessage(), true);
        }
    }

    /** Vuelve a cargar la tabla con las suscripciones actuales. */
    private void refrescarTabla() {
        tablaSuscripciones.setItems(
                FXCollections.observableArrayList(suscripcionClima.listarSuscripciones()));
        tablaSuscripciones.refresh();
    }

    /** Restaura los controles al terminar la consulta. */
    private void terminarCarga() {
        btnConsultar.setDisable(false);
        indicadorCarga.setVisible(false);
    }

    /**
     * Construye el valor de una celda que depende del clima del dron.
     *
     * @param suscripcion fila de la tabla
     * @param extractor   funcion que obtiene el texto a partir del clima
     * @return texto de la celda, o "-" si el dron aun no tiene clima
     */
    private static SimpleStringProperty celdaClima(Suscripcion suscripcion,
                                                   Function<Weather, String> extractor) {
        Weather clima = suscripcion.getDrone().getClimaActual();
        return new SimpleStringProperty(clima == null ? SIN_DATO : extractor.apply(clima));
    }

    /**
     * Formatea un numero con punto decimal.
     *
     * @param patron patron de formato
     * @param valor  valor a formatear
     * @return texto formateado
     */
    private static String formato(String patron, double valor) {
        return String.format(Locale.US, patron, valor);
    }

    /**
     * Muestra un mensaje informativo o de error.
     *
     * @param mensaje texto a mostrar
     * @param error   {@code true} si es un error
     */
    private void mostrarMensaje(String mensaje, boolean error) {
        lblMensaje.setText(mensaje);
        lblMensaje.getStyleClass().removeAll("mensaje-ok", "mensaje-error");
        lblMensaje.getStyleClass().add(error ? "mensaje-error" : "mensaje-ok");
    }

    /** Muestra cada dron en el selector como "#id - serial". */
    private static final class ConvertidorDrone extends StringConverter<Drone> {

        /** {@inheritDoc} */
        @Override
        public String toString(Drone drone) {
            return drone == null ? "" : "#" + drone.getId() + " - " + drone.getSerial();
        }

        /** No se usa: el selector no es editable. */
        @Override
        public Drone fromString(String texto) {
            return null;
        }
    }
}
