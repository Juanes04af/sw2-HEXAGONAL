package co.edu.poli.sw2.ProyectoHexa.infraestructura.weather;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.ClimaException;

/**
 * Pruebas de {@link ClienteHttp} contra un servidor HTTP local minimo.
 *
 * <p>
 * Usa solo {@link ServerSocket} (modulo java.base), asi compila tanto en
 * Eclipse con module-info como en Maven, sin depender de jdk.httpserver.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
class ClienteHttpTest {

    private ServerSocket servidor;
    private Thread hilo;
    private String base;

    @BeforeEach
    void iniciar() throws IOException {
        servidor = new ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
        hilo = new Thread(this::atender, "servidor-prueba");
        hilo.setDaemon(true);
        hilo.start();
        base = "http://127.0.0.1:" + servidor.getLocalPort();
    }

    @AfterEach
    void detener() throws IOException {
        if (servidor != null && !servidor.isClosed()) {
            servidor.close();
        }
    }

    /** Atiende peticiones hasta que se cierre el servidor. */
    private void atender() {
        while (!servidor.isClosed()) {
            try (Socket cliente = servidor.accept()) {
                responder(cliente);
            } catch (IOException e) {
                // El servidor se cerro o el cliente corto la conexion.
            }
        }
    }

    private static void responder(Socket cliente) throws IOException {
        BufferedReader entrada = new BufferedReader(
                new InputStreamReader(cliente.getInputStream(), StandardCharsets.UTF_8));

        String primeraLinea = entrada.readLine();
        if (primeraLinea == null) {
            return;
        }
        String ruta = primeraLinea.split(" ")[1];

        String accept = "";
        String linea;
        while ((linea = entrada.readLine()) != null && !linea.isEmpty()) {
            if (linea.toLowerCase().startsWith("accept:")) {
                accept = linea.substring("accept:".length()).trim();
            }
        }

        int codigo;
        String cuerpo;
        if ("/ok".equals(ruta)) {
            codigo = 200;
            cuerpo = "{'ok':true}";
        } else if ("/accept".equals(ruta)) {
            codigo = 200;
            cuerpo = accept;
        } else {
            codigo = 503;
            cuerpo = "{}";
        }

        byte[] bytes = cuerpo.getBytes(StandardCharsets.UTF_8);
        String cabecera = "HTTP/1.1 " + codigo + (codigo == 200 ? " OK" : " Service Unavailable")
                + "\r\nContent-Type: application/json"
                + "\r\nContent-Length: " + bytes.length
                + "\r\nConnection: close\r\n\r\n";

        OutputStream salida = cliente.getOutputStream();
        salida.write(cabecera.getBytes(StandardCharsets.US_ASCII));
        salida.write(bytes);
        salida.flush();
    }

    @Test
    @DisplayName("Con respuesta 200 devuelve el cuerpo")
    void get_devuelveElCuerpo() {
        assertEquals("{'ok':true}", ClienteHttp.get(base + "/ok", "clima"));
    }

    @Test
    @DisplayName("Con un codigo distinto de 200 lanza ClimaException con el codigo y el servicio")
    void get_codigoDistintoDe200() {
        ClimaException ex = assertThrows(ClimaException.class,
                () -> ClienteHttp.get(base + "/error", "clima"));

        assertTrue(ex.getMessage().contains("503"));
        assertTrue(ex.getMessage().contains("clima"));
    }

    @Test
    @DisplayName("La peticion pide application/json")
    void get_pideJson() {
        assertEquals("application/json", ClienteHttp.get(base + "/accept", "clima"));
    }

    @Test
    @DisplayName("Sin servidor escuchando lanza ClimaException de conexion")
    void get_sinServidor() throws IOException {
        int puerto = servidor.getLocalPort();
        servidor.close();

        ClimaException ex = assertThrows(ClimaException.class,
                () -> ClienteHttp.get("http://127.0.0.1:" + puerto + "/ok", "geocodificacion"));

        assertTrue(ex.getMessage()
                .contains("No fue posible conectarse con la API de geocodificacion"));
    }
}
