package psp.ud03.tarea34.server.commands;

import psp.ud03.tarea34.server.CommandContext;
import psp.ud03.tarea34.server.PathResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Crea un directorio (normalmente 1 nivel).
 * Respuesta: OK si crea, KO si error (ya existe, permisos, etc.).
 * <p>
 * Comando: mkdir <dir>
 * <p>
 * Crea un directorio dentro del directorio base del servidor.
 * <p>
 * Protocolo:
 * - Responde OK si se crea (o ya existe).
 * - Responde KO si faltan parámetros o hay error (ruta inválida, permisos, etc.).
 */
public class MkdirCommand implements ICommand {

    @Override
    public boolean execute(CommandContext ctx, String params) throws IOException {
        try {
            // 1) Validamos que venga un nombre de directorio
            String dirName = requireParam(params);

            // 2) Resolvemos la ruta de forma segura con la política de PathResolver
            //    (anti-traversal: evita salir de baseDir con ".." y rutas absolutas)
            Path dir = PathResolver.resolve(ctx, dirName);

            // 3) Creamos el directorio (si ya existe, no falla)
            Files.createDirectories(dir);

            ctx.reply("OK");
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
