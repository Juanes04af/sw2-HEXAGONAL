package co.edu.poli.sw2.ProyectoHexa.infraestructura.persistencia;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Lee el archivo .env de la raiz del proyecto (una sola vez).
 *
 * @author Alejandra Cano y Juan Rosero
 */
final class CargadorEnv {

    private static final String[] RUTAS = { ".env", "ProyectoHexa/.env", "../.env" };
    private static final Map<String, String> VALORES = cargar();

    private CargadorEnv() { }

    static String obtener(String clave) {
        return VALORES.get(clave);
    }

    private static Map<String, String> cargar() {
        for (String ruta : RUTAS) {
            Path archivo = Paths.get(ruta);
            if (Files.isRegularFile(archivo)) {
                try {
                    Map<String, String> mapa = new HashMap<>();
                    for (String linea : Files.readAllLines(archivo, StandardCharsets.UTF_8)) {
                        linea = linea.replace("\uFEFF", "").trim(); // quita BOM si existe
                        if (linea.isEmpty() || linea.startsWith("#")) continue;
                        int igual = linea.indexOf('=');
                        if (igual > 0) {
                            mapa.put(linea.substring(0, igual).trim(),
                                     linea.substring(igual + 1).trim());
                        }
                    }
                    return Collections.unmodifiableMap(mapa);
                } catch (IOException e) {
                    return Collections.emptyMap();
                }
            }
        }
        return Collections.emptyMap();
    }
}