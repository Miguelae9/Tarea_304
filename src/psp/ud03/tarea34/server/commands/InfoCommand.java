package psp.ud03.tarea34.server.commands;

import psp.ud03.tarea34.net.ProtocolIO;
import psp.ud03.tarea34.server.CommandContext;
import psp.ud03.tarea34.server.PathResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Devuelve “metadatos” del fichero/directorio (tamaño, tipo, fechas, etc. según tu implementación).
 * Respuesta: OK + varias líneas de datos (o 1 línea), KO si error.
 * <p>
 * Comando: info <ruta>  (y stat como alias en el registry)
 * <p>
 * Devuelve información básica de fichero/directorio.
 * <p>
 * Protocolo:
 * - KO si no existe
 * - OK + líneas clave=valor si existe
 */
public class InfoCommand implements ICommand {

    @Override
    public boolean execute(CommandContext ctx, String params) throws IOException {
        try {
            String target = requireParam(params);
            Path p = PathResolver.resolve(ctx, target);

            if (!Files.exists(p)) {
                ctx.reply("KO");
                return false;
            }

            ctx.reply("OK");
            ProtocolIO.writeLine(ctx.out(), "name=" + p.getFileName());
            ProtocolIO.writeLine(ctx.out(), "type=" + (Files.isDirectory(p) ? "dir" : "file"));
            ProtocolIO.writeLine(ctx.out(), "size=" + (Files.isDirectory(p) ? 0 : Files.size(p)));
            ProtocolIO.writeLine(ctx.out(), "modified=" + Files.getLastModifiedTime(p));
        } catch (Exception e) {
            ctx.reply("KO");
        }
        return false;
    }

    private String requireParam(String params) {
        if (params == null || params.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing param");
        }
        return params.trim();
    }
}
