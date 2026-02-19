package psp.ud03.tarea34.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Punto de entrada del servidor TCP.
 * <p>
 * Responsabilidades:
 * 1) Leer configuración (puerto).
 * 2) Calcular baseDir (directorio de trabajo del servidor).
 * 3) Crear CommandRegistry (comandos disponibles).
 * 4) Abrir ServerSocket y aceptar clientes.
 * 5) Por cada cliente: crear un hilo con ServerWorker.
 * <p>
 * Nota importante:
 * - baseDir = Paths.get(".")... => "directorio actual del proceso servidor".
 * Si arrancas el servidor desde Desktop, '.' = Desktop.
 */
public class MainFileServerApp {

    public static void main(String[] args) {
        int port = loadPort();

        // Registry único: todos los workers usan los mismos comandos registrados
        CommandRegistry registry = new CommandRegistry();

        // Directorio base del servidor: el "current working directory"
        Path baseDir = Paths.get(".").toAbsolutePath().normalize();

        ServerLogger.log("SERVER", "START port=" + port);

        // ServerSocket: el servidor queda escuchando en ese puerto
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            // Bucle infinito: el servidor acepta clientes continuamente
            while (true) {
                Socket client = serverSocket.accept();

                // Un hilo por cliente => el servidor es concurrente (multihilo)
                Thread t = new Thread(new ServerWorker(client, baseDir, registry));
                t.start();
            }

        } catch (IOException e) {
            // En prácticas: si falla el server socket no podemos seguir
            throw new RuntimeException(e);
        }
    }

    /**
     * Lee el puerto desde "server.properties".
     * Clave esperada: "puerto"
     * Si no existe el fichero o el valor no es válido => 2121.
     */
    private static int loadPort() {
        Properties properties = new Properties();

        try (FileInputStream fis = new FileInputStream("server.properties")) {
            properties.load(fis);

            String value = properties.getProperty("puerto");
            return Integer.parseInt(value);

        } catch (IOException e) {
            // No existe server.properties o no se pudo leer
            return 2121;

        } catch (NumberFormatException e) {
            // El valor no era un número
            return 2121;

        } catch (Exception e) {
            // Cualquier otra cosa rara
            return 2121;
        }
    }
}
