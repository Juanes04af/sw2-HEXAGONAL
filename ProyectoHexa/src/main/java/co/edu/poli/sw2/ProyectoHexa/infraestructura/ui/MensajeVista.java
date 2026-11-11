package co.edu.poli.sw2.ProyectoHexa.infraestructura.ui;

import javafx.scene.control.Label;

/**
 * Muestra mensajes de exito o de error en una etiqueta, con su estilo CSS.
 * Compartido por los controladores para no repetir el mismo metodo.
 *
 * @author Alejandra Cano y Juan Rosero
 */
final class MensajeVista {

    private MensajeVista() {
    }

    /**
     * @param etiqueta etiqueta donde se muestra el mensaje
     * @param mensaje  texto a mostrar
     * @param error    {@code true} para el estilo de error (rojo)
     */
    static void mostrar(Label etiqueta, String mensaje, boolean error) {
        etiqueta.setText(mensaje);
        etiqueta.getStyleClass().removeAll("mensaje-ok", "mensaje-error");
        etiqueta.getStyleClass().add(error ? "mensaje-error" : "mensaje-ok");
    }
}
