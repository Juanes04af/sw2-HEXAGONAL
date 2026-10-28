package co.edu.poli.sw2.ProyectoHexa.infraestructura.ui;

import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.CalcularConsumoUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ListarDronesUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ResultadoConsumo;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronException;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.strategy.ConsumoStrategy;
import co.edu.poli.sw2.ProyectoHexa.dominio.strategy.Ruta;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.StringConverter;

/**
 * Adaptador de entrada JavaFX de la pestana Consumo.
 *
 * <p>
 * Muestra la ruta cargada del archivo JSON, permite elegir un dron y
 * una estrategia de consumo, y presenta la distancia, la estrategia
 * seleccionada y el calculo de la bateria requerida. Cada calculo se
 * agrega al historial para comparar las estrategias.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class ControlConsumo implements Initializable {

    /** Texto mostrado cuando no hay dato. */
    private static final String SIN_DATO = "-";

    /** Origen de la ruta. */
    @FXML
    private Label lblOrigen;

    /** Destino de la ruta. */
    @FXML
    private Label lblDestino;

    /** Distancia de la ruta. */
    @FXML
    private Label lblDistanciaRuta;

    /** Selector del dron. */
    @FXML
    private ComboBox<Drone> cmbDrone;

    /** Selector de la estrategia de consumo. */
    @FXML
    private ComboBox<ConsumoStrategy> cmbEstrategia;

    /** Boton que ejecuta el calculo. */
    @FXML
    private Button btnCalcular;

    /** Etiqueta de mensajes al usuario. */
    @FXML
    private Label lblMensaje;

    /** Resultado: dron. */
    @FXML
    private Label lblResDrone;

    /** Resultado: distancia. */
    @FXML
    private Label lblResDistancia;

    /** Resultado: estrategia. */
    @FXML
    private Label lblResEstrategia;

    /** Resultado: operacion realizada. */
    @FXML
    private Label lblResCalculo;

    /** Resultado: bateria requerida. */
    @FXML
    private Label lblResConsumo;

    /** Historial de calculos. */
    @FXML
    private TableView<ResultadoConsumo> tablaHistorial;

    /** Columna del dron. */
    @FXML
    private TableColumn<ResultadoConsumo, String> colDrone;

    /** Columna de la estrategia. */
    @FXML
    private TableColumn<ResultadoConsumo, String> colEstrategia;

    /** Columna de la distancia. */
    @FXML
    private TableColumn<ResultadoConsumo, String> colDistancia;

    /** Columna de la operacion. */
    @FXML
    private TableColumn<ResultadoConsumo, String> colCalculo;

    /** Columna de la bateria requerida. */
    @FXML
    private TableColumn<ResultadoConsumo, String> colConsumo;

    /** Caso de uso de calculo de consumo (contexto del Strategy). */
    private final CalcularConsumoUseCase calcularConsumo;

    /** Caso de uso de listado de drones. */
    private final ListarDronesUseCase listarDrones;

    /** Calculos realizados en esta sesion, el mas reciente primero. */
    private final ObservableList<ResultadoConsumo> historial =
            FXCollections.observableArrayList();

    /**
     * Construye el controlador con los casos de uso que necesita.
     *
     * @param calcularConsumo caso de uso de calculo de consumo
     * @param listarDrones    caso de uso de listado de drones
     */
    public ControlConsumo(CalcularConsumoUseCase calcularConsumo,
                          ListarDronesUseCase listarDrones) {
        this.calcularConsumo = calcularConsumo;
        this.listarDrones = listarDrones;
    }

    /**
     * Carga la ruta, las estrategias y los drones despues de cargar
     * el FXML.
     *
     * @param location  ubicacion del recurso
     * @param resources recursos de internacionalizacion
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmbDrone.setConverter(new ConvertidorDrone());
        cmbDrone.setOnShowing(evento -> cargarDrones());

        cmbEstrategia.setItems(FXCollections.observableArrayList(
                calcularConsumo.estrategiasDisponibles()));
        cmbEstrategia.setConverter(new ConvertidorEstrategia());

        configurarTabla();
        cargarRuta();
        cargarDrones();
    }

    /**
     * Calcula la bateria requerida con el dron y la estrategia elegidos.
     */
    @FXML
    private void calcular() {
        try {
            // El usuario elige la estrategia; el contexto la recibe en tiempo de ejecucion.
            calcularConsumo.setEstrategia(cmbEstrategia.getValue());
            ResultadoConsumo resultado = calcularConsumo.calcular(cmbDrone.getValue());

            mostrarResultado(resultado);
            historial.add(0, resultado);
            mostrarMensaje("Consumo calculado con " + resultado.getEstrategia().getNombre() + ".",
                    false);

        } catch (DronException e) {
            mostrarMensaje(e.getMessage(), true);
        }
    }

    /**
     * Muestra la ruta del archivo JSON. Si no se puede cargar, se
     * informa el error y se deshabilita el calculo.
     */
    private void cargarRuta() {
        try {
            Ruta ruta = calcularConsumo.obtenerRuta();
            lblOrigen.setText(ruta.getOrigen().toString());
            lblDestino.setText(ruta.getDestino().toString());
            lblDistanciaRuta.setText(km(calcularConsumo.obtenerDistanciaKm()));

        } catch (DronException e) {
            btnCalcular.setDisable(true);
            mostrarMensaje(e.getMessage(), true);
        }
    }

    /**
     * Recarga la lista de drones registrados, conservando la seleccion.
     */
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

    /**
     * Configura las columnas del historial.
     */
    private void configurarTabla() {
        colDrone.setCellValueFactory(d ->
                new SimpleStringProperty(nombreDrone(d.getValue().getDrone())));
        colEstrategia.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getEstrategia().getNombre()));
        colDistancia.setCellValueFactory(d ->
                new SimpleStringProperty(km(d.getValue().getDistanciaKm())));
        colCalculo.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().describirCalculo()));
        colConsumo.setCellValueFactory(d ->
                new SimpleStringProperty(bateria(d.getValue().getConsumo())));
        tablaHistorial.setItems(historial);
    }

    /**
     * Muestra un resultado en la tarjeta de resultado.
     *
     * @param resultado resultado del calculo
     */
    private void mostrarResultado(ResultadoConsumo resultado) {
        ConsumoStrategy estrategia = resultado.getEstrategia();
        lblResDrone.setText(nombreDrone(resultado.getDrone()));
        lblResDistancia.setText(km(resultado.getDistanciaKm()));
        lblResEstrategia.setText(String.format(Locale.US, "%s (factor %.1f)",
                estrategia.getNombre(), estrategia.getFactor()));
        lblResCalculo.setText(resultado.describirCalculo());
        lblResConsumo.setText(bateria(resultado.getConsumo()));
    }

    /**
     * Formatea una distancia.
     *
     * @param valor distancia en km
     * @return por ejemplo "12.51 km"
     */
    private static String km(double valor) {
        return String.format(Locale.US, "%.2f km", valor);
    }

    /**
     * Formatea la bateria requerida.
     *
     * @param valor consumo
     * @return por ejemplo "6.26 unidades"
     */
    private static String bateria(double valor) {
        return String.format(Locale.US, "%.2f unidades", valor);
    }

    /**
     * Describe un dron como "#id - serial".
     *
     * @param drone dron
     * @return texto visible
     */
    private static String nombreDrone(Drone drone) {
        return drone == null ? SIN_DATO : "#" + drone.getId() + " - " + drone.getSerial();
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

    /**
     * Muestra cada dron en el selector como "#id - serial".
     */
    private static final class ConvertidorDrone extends StringConverter<Drone> {

        /** {@inheritDoc} */
        @Override
        public String toString(Drone drone) {
            return drone == null ? "" : nombreDrone(drone);
        }

        /** No se usa: el selector no es editable. */
        @Override
        public Drone fromString(String texto) {
            return null;
        }
    }

    /**
     * Muestra cada estrategia con su factor, por ejemplo
     * "Modo Ahorro (x0.5)".
     */
    private static final class ConvertidorEstrategia extends StringConverter<ConsumoStrategy> {

        /** {@inheritDoc} */
        @Override
        public String toString(ConsumoStrategy estrategia) {
            return estrategia == null ? ""
                    : String.format(Locale.US, "%s (x%.1f)",
                            estrategia.getNombre(), estrategia.getFactor());
        }

        /** No se usa: el selector no es editable. */
        @Override
        public ConsumoStrategy fromString(String texto) {
            return null;
        }
    }
}
