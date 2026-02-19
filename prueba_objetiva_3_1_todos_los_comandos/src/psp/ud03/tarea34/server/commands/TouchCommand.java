package psp.ud03.tarea34.server.commands;

import psp.ud03.tarea34.server.CommandContext;
import psp.ud03.tarea34.server.PathResolver;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;

/**
 * Crea un fichero vacío si no existe o actualiza su fecha (según implementación).
 * Respuesta: OK si lo consigue, KO si error.
 * <p>
 * Comando: touch <fichero>
 * <p>
 * - Si no existe: crea fichero vacío.
 * - Si existe: actualiza la fecha de modificación.
 * <p>
 * Protocolo (texto):
 * 1) Servidor responde: OK | KO
 */
public class TouchCommand implements ICommand {

    @Override
    public boolean execute(CommandContext ctx, String params) throws IOException {

        if (params == null || params.isEmpty()) {
            ctx.reply("KO");
            return false;
        }

        Path target = PathResolver.resolve(ctx, params);

        try {
            if (Files.exists(target)) {
                // Solo tiene sentido para ficheros
                if (!Files.isRegularFile(target)) {
                    ctx.reply("KO");
                    return false;
                }
                Files.setLastModifiedTime(target, FileTime.fromMillis(System.currentTimeMillis()));
            } else {
                // Crear vacío (y crear directorios padre si existen en la ruta)
                Path parent = target.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                Files.createFile(target);
            }

            ctx.reply("OK");
            return false;
        } catch (IOException ex) {
            ctx.reply("KO");
            return false;
        }
    }
}
