package co.edu.poli.sw2.ProyectoHexa.infraestructura.ui;

import java.net.URL;
import java.util.ResourceBundle;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ExecuteMissionUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.PasoMision;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ResultadoMision;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronException;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * Adaptador de entrada JavaFX de la pestana Operacion.
 *
 * <p>
 * Recibe los datos de un dron de demostracion (solo en memoria) y su
 * nivel de bateria, inicia la operacion de vigilancia y muestra paso a
 * paso como el mediador coordino a los sistemas.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class ControlOperacion implements Initializable {

    /** Campo del serial. */
    @FXML
    private TextField txtSerial;

    /** Campo del modelo. */
    @FXML
    private TextField txtModelo;

    /** Casilla de deteccion termica. */
    @FXML
    private CheckBox chkTermica;

    /** Nivel de bateria. */
    @FXML
    private Slider sldBateria;

    /** Texto del nivel de bateria. */
    @FXML
    private Label lblBateria;

    /** Mensajes de validacion. */
    @FXML
    private Label lblMensaje;

    /** Resultado final de la mision. */
    @FXML
    private Label lblResultado;

    /** Tabla de pasos de la mision. */
    @FXML
    private TableView<PasoMision> tablaPasos;

    /** Columna del orden. */
    @FXML
    private TableColumn<PasoMision, String> colOrden;

    /** Columna del componente. */
    @FXML
    private TableColumn<PasoMision, String> colComponente;

    /** Columna del evento. */
    @FXML
    private TableColumn<PasoMision, String> colEvento;

    /** Columna del estado. */
    @FXML
    private TableColumn<PasoMision, String> colEstado;

    /** Columna del detalle. */
    @FXML
    private TableColumn<PasoMision, String> colDetalle;

    /** Caso de uso que inicia la mision. */
    private final ExecuteMissionUseCase executeMission;

    /**
     * Construye el controlador.
     *
     * @param executeMission caso de uso de inicio de mision
     */
    public ControlOperacion(ExecuteMissionUseCase executeMission) {
        this.executeMission = executeMission;
    }

    /**
     * Configura el control de bateria y la tabla.
     *
     * @param location  ubicacion del recurso
     * @param resources recursos de internacionalizacion
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sldBateria.valueProperty().addListener((obs, anterior, nuevo) ->
                lblBateria.setText("Nivel de bateria: " + nuevo.intValue() + " %"));

        colOrden.setCellValueFactory(d ->
                new SimpleStringProperty(String.valueOf(d.getValue().getOrden())));
        colComponente.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getComponente()));
        colEvento.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getEvento()));
        colEstado.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().isExitoso() ? "OK" : "Fallo"));
        colDetalle.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getDetalle()));
    }

    /**
     * Inicia la operacion de vigilancia y muestra la secuencia.
     */
    @FXML
    private void iniciarOperacion() {
        try {
            ResultadoMision resultado = executeMission.ejecutarMision(
                    txtSerial.getText(),
                    txtModelo.getText(),
                    chkTermica.isSelected(),
                    (int) Math.round(sldBateria.getValue()));

            tablaPasos.setItems(FXCollections.observableArrayList(resultado.getPasos()));
            lblResultado.setText(resultado.getMensaje());
            lblResultado.getStyleClass().removeAll("valor-destacado", "valor-cancelado");
            lblResultado.getStyleClass().add(
                    resultado.isIniciada() ? "valor-destacado" : "valor-cancelado");
            lblMensaje.setText("");

        } catch (DronException e) {
            lblMensaje.setText(e.getMessage());
            lblMensaje.getStyleClass().removeAll("mensaje-ok", "mensaje-error");
            lblMensaje.getStyleClass().add("mensaje-error");
        }
    }
}
