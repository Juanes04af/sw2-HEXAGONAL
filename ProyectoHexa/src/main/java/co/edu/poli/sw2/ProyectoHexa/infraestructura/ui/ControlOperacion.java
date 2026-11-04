package co.edu.poli.sw2.ProyectoHexa.infraestructura.ui;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ExecuteMissionUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ListarDronesUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.PasoMision;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ResultadoMision;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronException;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Vigilancia;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Duration;
import javafx.util.StringConverter;

/**
 * Adaptador de entrada JavaFX de la pestana Operacion.
 *
 * <p>
 * Lista los drones registrados (ya convertidos al modelo
 * {@link Drone}), permite elegir uno y su nivel de bateria, e inicia la
 * operacion de vigilancia a traves del mediador.
 * </p>
 *
 * <p>
 * El mediador resuelve la mision de inmediato; la pantalla la presenta
 * como una carga: cada paso aparece primero como "Verificando..." y,
 * pasados unos milisegundos, cambia a "OK" o "Denegado".
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class ControlOperacion implements Initializable {

    /** Milisegundos entre el inicio de un paso y el siguiente. */
    private static final double MS_POR_PASO = 900;

    /** Milisegundos que un paso permanece en "Verificando...". */
    private static final double MS_VERIFICACION = 600;

    /** Estado mostrado mientras se verifica un paso. */
    private static final String ESTADO_VERIFICANDO = "Verificando...";

    /** Estado de un paso exitoso. */
    private static final String ESTADO_OK = "✔ OK";

    /** Estado de un paso rechazado. */
    private static final String ESTADO_DENEGADO = "✖ Denegado";

    /** Selector del dron registrado. */
    @FXML
    private ComboBox<Drone> cmbDrone;

    /** Datos del dron seleccionado. */
    @FXML
    private Label lblInfoDrone;

    /** Nivel de bateria. */
    @FXML
    private Slider sldBateria;

    /** Texto del nivel de bateria. */
    @FXML
    private Label lblBateria;

    /** Boton que inicia la operacion. */
    @FXML
    private Button btnIniciar;

    /** Mensajes de validacion. */
    @FXML
    private Label lblMensaje;

    /** Barra de progreso de la secuencia. */
    @FXML
    private ProgressBar barraProgreso;

    /** Resultado final de la mision. */
    @FXML
    private Label lblResultado;

    /** Tabla de pasos de la mision. */
    @FXML
    private TableView<FilaPaso> tablaPasos;

    /** Columna del orden. */
    @FXML
    private TableColumn<FilaPaso, String> colOrden;

    /** Columna del componente. */
    @FXML
    private TableColumn<FilaPaso, String> colComponente;

    /** Columna del evento. */
    @FXML
    private TableColumn<FilaPaso, String> colEvento;

    /** Columna del estado. */
    @FXML
    private TableColumn<FilaPaso, String> colEstado;

    /** Columna del detalle. */
    @FXML
    private TableColumn<FilaPaso, String> colDetalle;

    /** Caso de uso que inicia la mision. */
    private final ExecuteMissionUseCase executeMission;

    /** Caso de uso que lista los drones registrados. */
    private final ListarDronesUseCase listarDrones;

    /** Filas visibles de la tabla. */
    private final ObservableList<FilaPaso> filas = FXCollections.observableArrayList();

    /** Animacion en curso, para detenerla si se inicia otra. */
    private Timeline animacion;

    /**
     * Construye el controlador.
     *
     * @param executeMission caso de uso de inicio de mision
     * @param listarDrones   caso de uso de listado de drones
     */
    public ControlOperacion(ExecuteMissionUseCase executeMission,
                            ListarDronesUseCase listarDrones) {
        this.executeMission = executeMission;
        this.listarDrones = listarDrones;
    }

    /**
     * Configura el selector, la bateria y la tabla.
     *
     * @param location  ubicacion del recurso
     * @param resources recursos de internacionalizacion
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmbDrone.setConverter(new ConvertidorDrone());
        cmbDrone.setOnShowing(evento -> cargarDrones());
        cmbDrone.valueProperty().addListener((obs, anterior, nuevo) -> mostrarInfo(nuevo));

        sldBateria.valueProperty().addListener((obs, anterior, nuevo) ->
                lblBateria.setText("Nivel de bateria: " + nuevo.intValue() + " %"));

        colOrden.setCellValueFactory(d -> d.getValue().orden);
        colComponente.setCellValueFactory(d -> d.getValue().componente);
        colEvento.setCellValueFactory(d -> d.getValue().evento);
        colEstado.setCellValueFactory(d -> d.getValue().estado);
        colDetalle.setCellValueFactory(d -> d.getValue().detalle);
        colEstado.setCellFactory(columna -> new CeldaEstado());
        tablaPasos.setItems(filas);

        cargarDrones();
    }

    /**
     * Inicia la operacion con el dron elegido y presenta la secuencia
     * como una carga, paso a paso.
     */
    @FXML
    private void iniciarOperacion() {
        try {
            ResultadoMision resultado = executeMission.ejecutarMision(
                    cmbDrone.getValue(), (int) Math.round(sldBateria.getValue()));
            limpiarMensaje();
            animar(resultado);

        } catch (DronException e) {
            lblMensaje.setText(e.getMessage());
            lblMensaje.getStyleClass().removeAll("mensaje-ok", "mensaje-error");
            lblMensaje.getStyleClass().add("mensaje-error");
        }
    }

    /**
     * Muestra los pasos uno a uno: primero "Verificando..." y luego
     * su resultado. Al final muestra el mensaje de la mision.
     *
     * @param resultado resultado entregado por el mediador
     */
    private void animar(ResultadoMision resultado) {
        if (animacion != null) {
            animacion.stop();
        }

        filas.clear();
        barraProgreso.setProgress(0);
        btnIniciar.setDisable(true);
        lblResultado.setText("Coordinando la mision...");
        estiloResultado("etiqueta-campo");

        List<PasoMision> pasos = resultado.getPasos();
        List<KeyFrame> cuadros = new ArrayList<>();

        for (int i = 0; i < pasos.size(); i++) {
            PasoMision paso = pasos.get(i);
            FilaPaso fila = new FilaPaso(paso);
            double progreso = (i + 1.0) / pasos.size();
            double inicio = i * MS_POR_PASO;

            cuadros.add(new KeyFrame(Duration.millis(inicio), e -> filas.add(fila)));
            cuadros.add(new KeyFrame(Duration.millis(inicio + MS_VERIFICACION), e -> {
                fila.completar();
                barraProgreso.setProgress(progreso);
            }));
        }

        animacion = new Timeline(cuadros.toArray(new KeyFrame[0]));
        animacion.setOnFinished(e -> {
            barraProgreso.setProgress(1);
            lblResultado.setText(resultado.getMensaje());
            estiloResultado(resultado.isIniciada() ? "valor-destacado" : "valor-cancelado");
            btnIniciar.setDisable(false);
        });
        animacion.play();
    }

    /**
     * Recarga los drones registrados, conservando la seleccion.
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
            lblMensaje.setText(e.getMessage());
        }
    }

    /**
     * Muestra el tipo, modelo y camara termica del dron elegido.
     *
     * @param drone dron seleccionado
     */
    private void mostrarInfo(Drone drone) {
        if (drone == null) {
            lblInfoDrone.setText("");
            return;
        }
        boolean termica = drone instanceof Vigilancia && ((Vigilancia) drone).isDeteccionTermica();
        lblInfoDrone.setText(drone.getTipo() + " | "
                + (drone.getModelo() == null ? "sin modelo" : drone.getModelo())
                + " | Camara termica: " + (termica ? "Si" : "No"));
    }

    /**
     * Cambia el estilo de la etiqueta de resultado.
     *
     * @param estilo clase CSS a aplicar
     */
    private void estiloResultado(String estilo) {
        lblResultado.getStyleClass().removeAll("etiqueta-campo", "valor-destacado",
                "valor-cancelado");
        lblResultado.getStyleClass().add(estilo);
    }

    /**
     * Borra el mensaje de validacion.
     */
    private void limpiarMensaje() {
        lblMensaje.setText("");
    }

    /**
     * Fila de la tabla: empieza en "Verificando..." y al completarse
     * muestra el estado y el detalle del paso.
     */
    private static final class FilaPaso {

        /** Paso que representa. */
        private final PasoMision paso;

        /** Orden visible. */
        final StringProperty orden;

        /** Componente visible. */
        final StringProperty componente;

        /** Evento visible. */
        final StringProperty evento = new SimpleStringProperty("...");

        /** Estado visible. */
        final StringProperty estado = new SimpleStringProperty(ESTADO_VERIFICANDO);

        /** Detalle visible. */
        final StringProperty detalle = new SimpleStringProperty("");

        /**
         * Crea la fila en estado de verificacion.
         *
         * @param paso paso de la mision
         */
        FilaPaso(PasoMision paso) {
            this.paso = paso;
            this.orden = new SimpleStringProperty(String.valueOf(paso.getOrden()));
            this.componente = new SimpleStringProperty(paso.getComponente());
        }

        /**
         * Muestra el resultado real del paso.
         */
        void completar() {
            evento.set(paso.getEvento());
            estado.set(paso.isExitoso() ? ESTADO_OK : ESTADO_DENEGADO);
            detalle.set(paso.getDetalle());
        }
    }

    /**
     * Celda del estado con color: gris al verificar, verde si es OK y
     * rojo si fue denegado.
     */
    private static final class CeldaEstado extends TableCell<FilaPaso, String> {

        /** {@inheritDoc} */
        @Override
        protected void updateItem(String valor, boolean vacia) {
            super.updateItem(valor, vacia);
            setText(vacia ? null : valor);
            if (vacia || valor == null || ESTADO_VERIFICANDO.equals(valor)) {
                setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");
            } else if (ESTADO_OK.equals(valor)) {
                setStyle("-fx-text-fill: #1e8449; -fx-font-weight: bold;");
            } else {
                setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
            }
        }
    }

    /**
     * Muestra cada dron en el selector como "#id - serial".
     */
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
