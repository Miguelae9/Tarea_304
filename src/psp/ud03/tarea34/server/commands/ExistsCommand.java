package psp.ud03.tarea34.server.commands;

import psp.ud03.tarea34.net.ProtocolIO;
import psp.ud03.tarea34.server.CommandContext;
import psp.ud03.tarea34.server.PathResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Comprueba si existe una ruta en el servidor.
 * Respuesta: normalmente OK si existe / KO si no existe (según tu implementación).
 * <p>
 * Comando: exists <ruta>
 * <p>
 * Devuelve si existe o no una ruta en el servidor.
 * <p>
 * Protocolo:
 * 1) OK
 * 2) true/false (una línea)
 * <p>
 * KO solo si hay error real (params mal, ruta inválida por política, etc.)
 */
public class ExistsCommand implements ICommand {

    @Override
    public boolean execute(CommandContext ctx, String params) throws IOException {
        try {
            String target = requireParam(params);
            Path p = PathResolver.resolve(ctx, target);

            ctx.reply("OK");
            ProtocolIO.writeLine(ctx.out(), Boolean.toString(Files.exists(p)));
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
