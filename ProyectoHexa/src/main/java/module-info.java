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

    /*
     * JavaFX necesita acceso reflexivo al
     * adaptador de entrada.
     */
    opens co.edu.poli.sw2.ProyectoHexa.infraestructura.ui
            to javafx.fxml;

    /*
     * TableView utiliza reflexion para leer
     * propiedades como id, serial, fabricante,
     * modelo, peso y tipo.
     */
    opens co.edu.poli.sw2.ProyectoHexa.dominio.modelo
            to javafx.base, javafx.fxml;

    exports co.edu.poli.sw2.ProyectoHexa.infraestructura.web;
    exports co.edu.poli.sw2.ProyectoHexa.dominio.modelo;
}