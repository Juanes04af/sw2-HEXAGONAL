/**
 * Modulo principal de la aplicacion de gestion de drones.
 *
 * @author Alejandra Cano y Juan Rosero
 */
module co.edu.poli.sw2 {

    requires javafx.controls;
    requires javafx.fxml;

    requires java.sql;
    requires org.postgresql.jdbc;

    /* Cliente HTTP de Java para las APIs de Open-Meteo. */
    requires java.net.http;

    /* JavaFX necesita acceso reflexivo a los adaptadores de entrada. */
    opens co.edu.poli.sw2.ProyectoHexa.infraestructura.ui
            to javafx.fxml;

    /* TableView lee por reflexion las propiedades de los drones. */
    opens co.edu.poli.sw2.ProyectoHexa.dominio.modelo
            to javafx.base, javafx.fxml;

    exports co.edu.poli.sw2.ProyectoHexa.infraestructura.web;
    exports co.edu.poli.sw2.ProyectoHexa.dominio.modelo;

    /* Weather y el Observer viven aqui; Drone los usa en su API publica. */
    exports co.edu.poli.sw2.ProyectoHexa.dominio.clima;
}
