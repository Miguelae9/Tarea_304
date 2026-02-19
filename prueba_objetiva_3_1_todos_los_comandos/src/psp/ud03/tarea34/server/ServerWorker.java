package psp.ud03.tarea34.server;

import psp.ud03.tarea34.net.ProtocolIO;
import psp.ud03.tarea34.server.commands.ICommand;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Hilo de trabajo por cliente (Runnable).
 * <p>
 * Responsabilidades:
 * - Crear el CommandContext (streams + IP + baseDir).
 * - Leer comandos del cliente (una línea por comando).
 * - Parsear: cmd + params.
 * - Buscar el comando en CommandRegistry.
 * - Ejecutar ICommand.execute(ctx, params).
 * - Gestionar desconexión normal (quit) o inesperada (EOF/error).
 * <p>
 * Nota de protocolo:
 * - Se usa ProtocolIO.readLine(InputStream) para texto.
 * - upload/download cambian a binario (long + bytes) DESPUÉS del OK.
 */
public class ServerWorker implements Runnable {

    private final Socket socket;
    private final Path baseDir;
    private final CommandRegistry registry;

    public ServerWorker(Socket socket, Path baseDir, CommandRegistry registry) {
        this.socket = socket;
        this.baseDir = baseDir;
        this.registry = registry;
    }

    @Override
    public void run() {
        try {
            // Si baseDir no existe, lo crea (si ya existe no pasa nada)
            Files.createDirectories(baseDir);

            // Contexto con streams del socket y datos del cliente
            CommandContext ctx = new CommandContext(socket, baseDir);
            ServerLogger.log(ctx.clientIp(), "CONNECT");

            boolean exit = false;

            // Bucle principal: 1 línea = 1 comando
            while (!exit) {

                // Leer comando del cliente
                String line = ProtocolIO.readLine(ctx.in());

                // null => el cliente cerró el socket sin "quit"
                if (line == null) {
                    ServerLogger.log(ctx.clientIp(), "DISCONNECT unexpected");
                    break;
                }

                line = line.trim();

                // Ignorar líneas vacías (no ejecutan nada)
                if (!line.isEmpty()) {

                    // Parse básico: "cmd params..."
                    String cmd;
                    String params;

                    int pos = line.indexOf(' ');
                    if (pos == -1) {
                        cmd = line;
                        params = "";
                    } else {
                        cmd = line.substring(0, pos);
                        params = line.substring(pos + 1).trim();
                    }

                    // Texto completo del comando recibido (para el log unificado)
                    String received = cmd;
                    if (!params.isEmpty()) {
                        received = received + " " + params;
                    }

                    // Guardamos el comando para que ctx.reply(...) pueda loguear:
                    // RECV: <comando> -> RESP: OK/KO
                    ctx.setLastReceived(received);

                    // Buscar implementación del comando
                    ICommand command = registry.get(cmd);

                    // Si no existe: respondemos KO y queda logueado en una sola línea
                    if (command == null) {
                        ctx.reply("KO");
                    } else {
                        // Ejecutar el comando
                        // - Devuelve true si el comando pide cerrar (quit)
                        boolean shouldClose = command.execute(ctx, params);

                        if (shouldClose) {
                            ServerLogger.log(ctx.clientIp(), "DISCONNECT quit");
                            exit = true;
                        }
                    }
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
