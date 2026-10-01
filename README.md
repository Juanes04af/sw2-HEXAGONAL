# Gestión de Drones — Arquitectura Hexagonal

Aplicación de escritorio en **JavaFX** para gestionar drones de **vigilancia** sobre **PostgreSQL**, construida con **arquitectura hexagonal (puertos y adaptadores)**.

Es la evolución del primer proyecto (`Proyecto-1` / `sw2`). Conserva la misma base de datos, pero reorganiza el código para que la lógica de negocio no dependa de la interfaz gráfica ni de la tecnología de persistencia.

**Autores:** Alejandra Cano y Juan Rosero
**Asignatura:** Ingeniería de Software 2 — Politécnico Grancolombiano

---

## 1. Alcance

El proyecto se limita deliberadamente a lo esencial:

- **5 casos de uso:** crear, modificar, eliminar, buscar por ID y listar todos.
- **Un único tipo de dron:** `Vigilancia` (con su atributo `deteccionTermica`). No es posible cambiar a otro tipo; los drones de agricultura que existan en la base de datos simplemente no se muestran.
- **Un único patrón de diseño:** **Singleton**, aplicado a la conexión con la base de datos (`ConexionBD`).

> La explicación de la **arquitectura hexagonal** (capas, puertos, adaptadores y casos de uso) está en la **Wiki** del repositorio.

---

## 2. Patrón Singleton: `ConexionBD`

`ConexionBD` garantiza una única instancia encargada de la configuración de conexión, usando *double-checked locking*:

- La clase es `final`, para que no se pueda extender.
- El constructor es `private`, así que nadie puede hacer `new ConexionBD()`.
- El campo `instancia` es `static volatile`, lo que lo hace seguro entre hilos.
- `getInstancia()` es el único punto de acceso.

Cada llamada a `obtenerConexion()` abre una conexión JDBC nueva, que quien la usa debe cerrar (los repositorios lo hacen con `try-with-resources` o en un `finally`).

### Orden de lectura de credenciales

1. Variables de entorno del sistema operativo (`DB_URL`, `DB_USER`, `DB_PASSWORD`).
2. Archivo `.env` en la raíz del proyecto, leído por `CargadorEnv`.
3. Archivo `db.properties` en el classpath (opcional).

Si ninguna fuente aporta la URL, se usa `jdbc:postgresql://localhost:5432/dronesdb`.

> Si en Windows existe una variable de entorno `DB_USER` o `DB_PASSWORD`, esta tiene prioridad sobre el `.env`.

---

## 3. Tecnologías

| Componente | Versión |
|---|---|
| Java | 11 |
| JavaFX (controls + fxml) | 17.0.9 |
| Maven / javafx-maven-plugin | 0.0.8 |
| PostgreSQL JDBC | 42.7.3 |
| JUnit | 5.10.2 |
| IDE | Eclipse (con EGit para Git) |

---

## 4. Base de datos

Se usa **la misma base de datos del primer proyecto** (`dronesdb`), sin cambios. El esquema aplica herencia por tablas:

- **`dron`** (tabla padre): `id`, `serial` (único), `fabricante`, `modelo`, `peso`, `tipo`.
- **`dron_vigilancia`** (tabla hija): `id_dron` (referencia a `dron.id` con `ON DELETE CASCADE`), `deteccion_termica`.

El repositorio solo lee y modifica registros con `tipo = 'VIGILANCIA'`. Crear y modificar se hacen en una transacción sobre ambas tablas, de modo que un fallo parcial no deja registros huérfanos. Los errores de PostgreSQL se traducen a excepciones de dominio según su SQLState (`23505` duplicado, `23514` y `23502` dato inválido).

> Al compartir la base de datos con el primer proyecto, cualquier cambio sobre un dron de vigilancia se refleja en ambas aplicaciones.

---

## 5. Estructura del proyecto

```
ProyectoHexa/
├── .env                 ← tus credenciales (NO se sube)
├── .env.example         ← plantilla pública
├── .gitignore
├── pom.xml
└── src/
    ├── main/
    │   ├── java/
    │   │   ├── module-info.java
    │   │   └── co/edu/poli/sw2/ProyectoHexa/
    │   │       ├── dominio/
    │   │       │   ├── modelo/
    │   │       │   └── excepcion/
    │   │       ├── aplicacion/
    │   │       │   ├── puerto/entrada/
    │   │       │   ├── puerto/salida/
    │   │       │   └── servicio/
    │   │       └── infraestructura/
    │   │           ├── persistencia/
    │   │           ├── ui/
    │   │           └── web/
    │   └── resources/
    │       └── co/edu/poli/sw2/vista/
    │           ├── drone.fxml
    │           └── styles.css
    └── test/java/
```

> Los recursos (`drone.fxml` y `styles.css`) **deben** estar en `src/main/resources`. Si quedan en otra carpeta, Maven no los copia y la aplicación falla al iniciar con `Location is not set`.

---

## 6. Cómo ejecutar el proyecto

### Requisitos

- JDK 11.
- Eclipse con soporte Maven (m2e) y EGit.
- PostgreSQL corriendo, con la base de datos `dronesdb` y las tablas `dron` y `dron_vigilancia`.

### Pasos

1. **Clonar el repositorio** desde Eclipse: *File → Import → Git → Projects from Git → Clone URI*.
2. **Importar el proyecto Maven** `ProyectoHexa` (*File → Import → Maven → Existing Maven Projects*).
3. **Crear tu archivo `.env`** en la carpeta `ProyectoHexa`, copiando `.env.example` y poniendo tus datos:
   ```
   DB_URL=jdbc:postgresql://localhost:5432/dronesdb
   DB_USER=tu_usuario
   DB_PASSWORD=tu_password
   ```
   Escribe una variable por línea, sin espacios ni comillas.
4. **Actualizar el proyecto:** clic derecho → *Maven → Update Project* (Alt+F5) y luego *Project → Clean*.
5. **Ejecutar** `infraestructura/web/App.java` con *Run As → Java Application*.

Desde consola también puede ejecutarse con:

```bash
mvn clean javafx:run
```

---

## 7. Pruebas

Las pruebas usan **JUnit 5** y están en `src/test/java`, en los mismos paquetes que el código que prueban. Se dividen en dos grupos.

### Pruebas unitarias (no necesitan base de datos)

| Clase de prueba | Qué verifica |
|---|---|
| `VigilanciaTest` | Constructor, *getters*/*setters*, tipo `VIGILANCIA` y que sea un `Drone` serializable |
| `ValidadorDroneTest` | Reglas de serial, fabricante y peso (vacíos, solo números, sin alfanuméricos, cero o negativos, coma decimal, `NaN`/`Infinity`) |
| `CrearDroneServicioTest` | Creación, recorte de espacios, modelo opcional, peso con coma, serial duplicado y que un dato inválido **nunca llegue** al repositorio |
| `ModificarDroneServicioTest` | Actualización de datos, dron sin seleccionar, tipo distinto a vigilancia, que un dato inválido no altere el original, inexistente y serial de otro dron |
| `EliminarDroneServicioTest` | Eliminación, que solo se borre el indicado, dron sin seleccionar e inexistente |
| `BuscarYListarDroneServicioTest` | Búsqueda por ID, ID inexistente, lista vacía y orden del listado |
| `ConexionBDTest` | Estructura del Singleton: misma instancia, seguridad con 20 hilos, constructor privado, clase `final`, campo `static volatile` y `toString` sin credenciales |

Los casos de uso se prueban con `RepositorioEnMemoria`, una implementación falsa del puerto `DroneRepository` que imita las reglas de la base de datos (ids, seriales únicos, no encontrado). Así la lógica de aplicación se prueba de forma aislada, sin PostgreSQL.

### Pruebas de integración (necesitan PostgreSQL y el `.env`)

| Clase de prueba | Qué verifica |
|---|---|
| `ConexionBDIT` | Que el `.env` aporte credenciales y que la conexión real sea válida e independiente en cada llamada |
| `PostgresDroneRepositoryIT` | CRUD completo contra la BD, cascada al eliminar, serial duplicado con *rollback*, inexistentes y rechazo de tipos distintos a vigilancia |

Estas pruebas crean drones con serial `TEST-xxxxxxxx` y los **borran al terminar**, para no dejar datos basura en la base de datos compartida.

### Cómo ejecutarlas

- **Eclipse:** clic derecho sobre `src/test/java` → *Run As → JUnit Test* (corre todas), o sobre una clase concreta.
- **Maven:** `mvn test` corre solo las unitarias (las clases `*Test`). Las de integración (`*IT`) se corren a propósito con:
  ```bash
  mvn test -Dtest=*IT
  ```

---

## 8. Seguridad de credenciales

- El archivo `.env` está en `.gitignore` y **nunca se sube** al repositorio. Cada integrante crea el suyo a partir de `.env.example`.
- `db.properties` también está ignorado.
- `ConexionBD.toString()` devuelve un texto fijo (`ConexionBD[configurada]`), así que las credenciales no se filtran ni en un `println` accidental.

---

## 9. Solución de problemas

| Síntoma | Causa probable | Solución |
|---|---|---|
| `Location is not set` al iniciar | `drone.fxml` no está en `src/main/resources` | Mover la carpeta `co/...` a `src/main/resources` y hacer *Maven → Update Project* |
| `Faltan las credenciales de la base de datos` | No existe `.env` o le faltan claves | Crear el `.env` a partir de `.env.example` |
| `password authentication failed for user "..."` | Usuario o contraseña incorrectos | Revisar el `.env` (sin espacios ni texto extra) |
| `Connection ... refused` | PostgreSQL no está corriendo | Iniciar el servicio `postgresql-x64-XX` en `services.msc` |
| `database "dronesdb" does not exist` | La base de datos tiene otro nombre | Corregir `DB_URL` en el `.env` |
| La ventana se ve sin estilos | `styles.css` no está junto al FXML o tiene contenido inválido | Verificar que esté en `src/main/resources/co/edu/poli/sw2/vista/` y que contenga CSS |
| No se ven los archivos `.env` / `.gitignore` en Eclipse | Eclipse oculta los archivos que empiezan con punto | *Package Explorer → ⋮ → Filters* → desmarcar `.* resources` |

---

## 10. Trabajo con Git (Eclipse / EGit)

1. Abrir la vista *Window → Show View → Git → Git Staging*.
2. Revisar que **`.env` no aparezca** en la lista de cambios.
3. Pasar los archivos a *Staged Changes*, escribir el mensaje y pulsar **Commit and Push**.

Para ignorar un archivo nuevo: clic derecho → *Team → Ignore*.
