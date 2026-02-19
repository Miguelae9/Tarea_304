package psp.ud03.tarea34.server.commands;

import psp.ud03.tarea34.server.CommandContext;
import psp.ud03.tarea34.server.PathResolver;

import java.io.IOException;
import java.nio.file.*;

/**
 * Copia un fichero/directorio (según implementación; en tu proyecto es típico que sea fichero).
 * Respuesta: OK si copia, KO si error.
 * <p>
 * Comando: copy <src> <dst>
 * <p>
 * Copia un fichero dentro del servidor (baseDir -> baseDir).
 * <p>
 * Protocolo:
 * - Responde OK si se copia correctamente.
 * - Responde KO si faltan params, no existe src, dst ya existe (si no sobrescribimos), etc.
 * <p>
 * Decisión (coherente con UploadCommand):
 * - NO sobrescribir: si dst existe, KO.
 */
public class CopyCommand implements ICommand {

    @Override
    public boolean execute(CommandContext ctx, String params) throws IOException {

        try {
            // 1) Validar y separar parámetros: src y dst
            String[] p = split2(params);

            // 2) Resolver rutas seguras dentro de baseDir
            Path src = PathResolver.resolve(ctx, p[0]);
            Path dst = PathResolver.resolve(ctx, p[1]);

            // 3) Validaciones mínimas
            if (!Files.exists(src) || !Files.isRegularFile(src)) {
                ctx.reply("KO");
                return false;
            }

            // 4) Política: no sobrescribir (igual que upload)
            if (Files.exists(dst)) {
                ctx.reply("KO");
                return false;
            }

            // 5) Copiar
            Files.copy(src, dst, StandardCopyOption.COPY_ATTRIBUTES);

            ctx.reply("OK");
            return false;

        } catch (Exception e) {
            ctx.reply("KO");
            return false;
        }
    }

    /**
     * Divide "src dst" en 2 tokens.
     * Nota: versión simple (sin comillas). Para examen suele bastar.
     */
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
