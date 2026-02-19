package psp.ud03.tarea34.server.commands;

import psp.ud03.tarea34.server.CommandContext;
import psp.ud03.tarea34.server.PathResolver;
import psp.ud03.tarea34.server.ServerLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Borra un fichero o un directorio vacío.
 * Respuesta: OK si borró, KO si falta/ no existe / no se puede borrar.
 * <p>
 * Comando: delete <ruta>
 * <p>
 * Función:
 * - Borra un fichero o un directorio (solo si está vacío).
 * <p>
 * Respuesta:
 * - KO si:
 * - faltan parámetros
 * - no existe
 * - no se puede borrar (por ejemplo: directorio no vacío, permisos, etc.)
 * <p>
 * - OK si se borra correctamente.
 */
public class DeleteCommand implements ICommand {

    @Override
    public boolean execute(CommandContext ctx, String params) throws IOException {

        // 1) Validación básica
        if (params == null || params.length() == 0) {
            ctx.reply("KO");
            return false;
        }

        // 2) Resolver ruta
        Path pathDelete = PathResolver.resolve(ctx, params);

        // 3) Comprobar que existe
        if (!Files.exists(pathDelete)) {
            ctx.reply("KO");
            return false;
        }

        // 4) Intentar borrar
        try {
            Files.delete(pathDelete);
            ctx.reply("OK");
        } catch (IOException e) {
            // Si falla el borrado, respondemos KO (el motivo exacto depende del SO/permisos)
            ctx.reply("KO");

            // Log opcional de error para depurar en examen (NO es RESP, no duplica)
            ServerLogger.log(ctx.clientIp(), "ERROR (delete failed: " + e.getMessage() + ")");
        }

        return false;
    }
}
