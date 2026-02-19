package psp.ud03.tarea34.server.commands;

import psp.ud03.tarea34.server.CommandContext;
import psp.ud03.tarea34.server.PathResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Renombra o mueve dentro del servidor (según cómo lo uses).
 * Respuesta: OK si renombra, KO si error (no existe, destino existe, permisos…).
 * <p>
 * Comando: rename <src> <dst>
 * <p>
 * Renombra (o mueve) un fichero/directorio dentro de baseDir.
 * <p>
 * Protocolo:
 * - OK si la operación se realiza
 * - KO si params mal o error (no existe src, permisos, etc.)
 */
public class RenameCommand implements ICommand {

    @Override
    public boolean execute(CommandContext ctx, String params) throws IOException {
        try {
            // 1) Partimos params en dos rutas: origen y destino
            String[] p = split2(params);

            // 2) Resolvemos ambas rutas aplicando la política segura
            Path src = PathResolver.resolve(ctx, p[0]);
            Path dst = PathResolver.resolve(ctx, p[1]);

            // 3) Movemos/renombramos (si no quieres reemplazar, quita REPLACE_EXISTING)
            Files.move(src, dst, StandardCopyOption.REPLACE_EXISTING);

            ctx.reply("OK");
        } catch (Exception e) {
            ctx.reply("KO");
        }
        return false;
    }

    private String[] split2(String params) {
        if (params == null) throw new IllegalArgumentException("Missing params");
        String p = params.trim();
        int i = p.indexOf(' ');
        if (i < 0) throw new IllegalArgumentException("Need 2 params");
        String a = p.substring(0, i).trim();
        String b = p.substring(i + 1).trim();
        if (a.isEmpty() || b.isEmpty()) throw new IllegalArgumentException("Need 2 params");
        return new String[]{a, b};
    }
}
