package psp.ud03.tarea34.server.commands;

import psp.ud03.tarea34.net.ProtocolIO;
import psp.ud03.tarea34.server.CommandContext;
import psp.ud03.tarea34.server.PathResolver;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Devuelve las últimas n líneas (por defecto 10).
 * Respuesta: OK + N + N líneas. KO si error.
 * <p>
 * Comando: tail <ruta> [n]
 * <p>
 * Muestra las últimas n líneas de un fichero de texto (UTF-8).
 * n por defecto: 10.
 * <p>
 * Respuesta:
 * - KO si faltan params o no es fichero legible.
 * - OK y luego:
 * - una línea con N (líneas que se van a enviar)
 * - N líneas de contenido
 */
public class TailCommand implements ICommand {

    private static final int DEFAULT_N = 10;
    private static final int MAX_N = 5000;

    @Override
    public boolean execute(CommandContext ctx, String params) throws IOException {

        if (params == null || params.isEmpty()) {
            ctx.reply("KO");
            return false;
        }

        String[] parts = params.trim().split("\\s+");
        String route = parts[0];
        int n = DEFAULT_N;
        if (parts.length >= 2) {
            try {
                n = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {
                n = DEFAULT_N;
            }
        }
        if (n < 0) n = 0;
        if (n > MAX_N) n = MAX_N;

        Path p = PathResolver.resolve(ctx, route);
        if (!Files.exists(p) || !Files.isRegularFile(p) || !Files.isReadable(p)) {
            ctx.reply("KO");
            return false;
        }

        // Cola circular de tamaño n
        Deque<String> dq = new ArrayDeque<>(Math.max(1, n));
        try (BufferedReader br = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (n == 0) break;
                if (dq.size() == n) dq.removeFirst();
                dq.addLast(line);
            }
        }

        ctx.reply("OK");
        ProtocolIO.writeLine(ctx.out(), String.valueOf(dq.size()));
        for (String s : dq) {
            ProtocolIO.writeLine(ctx.out(), s);
        }
        return false;
    }
}
