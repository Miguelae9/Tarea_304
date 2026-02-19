package psp.ud03.tarea34.server.commands;

import psp.ud03.tarea34.net.ProtocolIO;
import psp.ud03.tarea34.server.CommandContext;
import psp.ud03.tarea34.server.PathResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Devuelve el tamaño en bytes de un fichero.
 * Respuesta: OK + 1 línea con el número (bytes). KO si no existe/no es fichero.
 * <p>
 * Comando: size <fichero>
 * <p>
 * Devuelve el tamaño del fichero en bytes.
 * <p>
 * Protocolo (texto):
 * 1) Servidor responde: OK | KO
 * 2) Si OK, servidor envía 1 línea con el tamaño (long en texto).
 */
public class SizeCommand implements ICommand {

    @Override
    public boolean execute(CommandContext ctx, String params) throws IOException {

        if (params == null || params.isEmpty()) {
            ctx.reply("KO");
            return false;
        }

        Path target = PathResolver.resolve(ctx, params);

        if (!Files.exists(target) || !Files.isRegularFile(target)) {
            ctx.reply("KO");
            return false;
        }

        long size;
        try {
            size = Files.size(target);
        } catch (IOException ex) {
            ctx.reply("KO");
            return false;
        }

        ctx.reply("OK");
        ProtocolIO.writeLine(ctx.out(), String.valueOf(size));
        return false;
    }
}
