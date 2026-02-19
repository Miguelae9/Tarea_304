package psp.ud03.tarea34.server.commands;

import psp.ud03.tarea34.net.ProtocolIO;
import psp.ud03.tarea34.server.CommandContext;
import psp.ud03.tarea34.server.PathResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Muestra un fichero de texto (UTF-8).
 * Respuesta: OK + N + N líneas. KO si no existe/no es fichero/no legible.
 * <p>
 * <p>
 * Comando: show <ruta>
 * <p>
 * Función:
 * - Muestra el contenido de un fichero de texto (UTF-8).
 * <p>
 * Respuesta:
 * - KO si:
 * - faltan parámetros
 * - la ruta no existe / no es fichero / no se puede leer
 * <p>
 * - OK si todo va bien, y después:
 * - una línea con el número de líneas (N)
 * - N líneas con el contenido del fichero
 * <p>
 * Nota:
 * - Este comando es SOLO para texto. Si le pasas un binario, puede dar contenido extraño.
 */
public class ShowCommand implements ICommand {

    @Override
    public boolean execute(CommandContext ctx, String params) throws IOException {

        // 1) Validación básica
        if (params == null || params.length() == 0) {
            ctx.reply("KO");
            return false;
        }

        // 2) Resolver ruta (relativa respecto a baseDir o absoluta del servidor)
        Path pathShow = PathResolver.resolve(ctx, params);

        // 3) Validaciones de fichero
        if (!Files.exists(pathShow) || !Files.isRegularFile(pathShow) || !Files.isReadable(pathShow)) {
            ctx.reply("KO");
            return false;
        }

        // 4) Leer todas las líneas (UTF-8)
        List<String> lines = Files.readAllLines(pathShow, StandardCharsets.UTF_8);

        // 5) Responder según el protocolo del show:
        // OK
        // N
        // line1
        // line2
        // ...
        ctx.reply("OK");
        ProtocolIO.writeLine(ctx.out(), String.valueOf(lines.size()));

        for (int i = 0; i < lines.size(); i++) {
            ProtocolIO.writeLine(ctx.out(), lines.get(i));
        }

        return false;
    }
}
