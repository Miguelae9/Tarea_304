package psp.ud03.tarea34.server.commands;

import psp.ud03.tarea34.server.CommandContext;
import psp.ud03.tarea34.server.PathResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Cambia el directorio actual (cwd) de esa conexión para no escribir rutas largas.
 * Respuesta: OK si entra, KO si no existe/no es directorio o intenta salir de baseDir.
 * <p>
 * Comando: cd <dir>
 * <p>
 * Cambia el directorio actual (cwd) del servidor para ESTA conexión.
 * <p>
 * Reglas:
 * - No permite salir de baseDir (anti-traversal) porque usa PathResolver.
 * - Solo se puede hacer cd a un directorio existente.
 * <p>
 * Protocolo (texto):
 * 1) Servidor responde: OK | KO
 */
public class CdCommand implements ICommand {

    @Override
    public boolean execute(CommandContext ctx, String params) throws IOException {

        if (params == null || params.isEmpty()) {
            ctx.reply("KO");
            return false;
        }

        Path target = PathResolver.resolve(ctx, params);

        if (!Files.exists(target) || !Files.isDirectory(target)) {
            ctx.reply("KO");
            return false;
        }

        ctx.setCurrentDir(target);
        ctx.reply("OK");
        return false;
    }
}
