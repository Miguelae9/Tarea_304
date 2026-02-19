package psp.ud03.tarea34.server;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Resuelve rutas recibidas por el cliente aplicando una política segura:
 * - Nunca permite salir de baseDir (anti-traversal).
 * - No admite rutas absolutas del servidor (si llegan, se reduce a getFileName()).
 */
public final class PathResolver {

    private PathResolver() {
    }

    public static Path resolve(CommandContext ctx, String params) {

        // Root del servidor (no se puede salir de aquí)
        Path root = ctx.baseDir().toAbsolutePath().normalize();

        // CWD por conexión (base para rutas relativas)
        Path base = ctx.currentDir().toAbsolutePath().normalize();

        // Defensa extra: si por lo que sea base no cuelga de root, volver a root
        if (!base.startsWith(root)) {
            base = root;
            ctx.setCurrentDir(root);
        }

        if (params == null || params.length() == 0) {
            return base;
        }

        Path p = Paths.get(params);

        /*
         * - No se permiten rutas absolutas del servidor.
         * - No se permite traversal fuera de baseDir.
         *
         * Decisión práctica:
         * - Si llega una ruta absoluta, nos quedamos con el nombre final (getFileName).
         */
        if (p.isAbsolute()) {
            Path onlyName = p.getFileName();
            if (onlyName == null) {
                return base;
            }
            p = onlyName;
        }

        // Resolver y normalizar dentro del cwd
        Path resolved = base.resolve(p).normalize();

        /*
         * Anti-traversal:
         * - Si el resultado se sale de baseDir (por ".."), lo bloqueamos.
         */
        if (!resolved.startsWith(root)) {
            Path onlyName = p.getFileName();
            if (onlyName == null) return base;

            Path safe = base.resolve(onlyName).normalize();

            // Garantía final: si incluso así se sale (caso ".."), devuelve base
            if (!safe.startsWith(root)) return base;

            return safe;
        }

        return resolved;
    }
}
