package psp.ud03.tarea34.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Logger del servidor en fichero: server.log
 * <p>
 * Requisitos que cumple:
 * - Escribe en UTF-8.
 * - Añade timestamp + IP + evento:
 * [YYYY-MM-DD HH:mm:ss] [IP] [EVENTO]
 * - Es seguro en multihilo: cada llamada escribe una línea completa sin mezclarse.
 */
public final class ServerLogger {

    // Fichero de log en el directorio actual del servidor
    private static final Path LOG_PATH = Paths.get("server.log");

    // Formato de fecha/hora del log
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ServerLogger() {
    }

    /**
     * Escribe un evento en server.log.
     * (synchronized => una línea completa por vez)
     *
     * @param clientIp IP del cliente (o "SERVER")
     * @param event    texto del evento
     */
    public static synchronized void log(String clientIp, String event) {
        String ts = LocalDateTime.now().format(FMT);
        String line = "[" + ts + "] [" + clientIp + "] [" + event + "]";

        try (BufferedWriter w = Files.newBufferedWriter(
                LOG_PATH,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.APPEND
        )) {
            w.write(line);
            w.newLine();
        } catch (IOException e) {
            // Si el log falla, no debe romper el servidor
            System.err.println("LOG ERROR: " + e.getMessage());
        }
    }
}
