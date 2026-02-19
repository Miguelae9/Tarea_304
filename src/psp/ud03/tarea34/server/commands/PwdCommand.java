package psp.ud03.tarea34.server.commands;

import psp.ud03.tarea34.net.ProtocolIO;
import psp.ud03.tarea34.server.CommandContext;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Devuelve el directorio actual de esa conexión (cwd).
 * Respuesta: OK + 1 línea con la ruta (normalmente relativa al baseDir).
 * <p>
 * Comando: pwd
 * <p>
 * Devuelve el directorio actual (cwd) del servidor para ESTA conexión.
 * <p>
 * Protocolo (texto):
 * 1) Servidor responde: OK | KO
 * 2) Si OK, servidor envía 1 línea con la ruta relativa a baseDir ("." si es la raíz).
 */
public class PwdCommand implements ICommand {

    @Override
    public boolean execute(CommandContext ctx, String params) throws IOException {
        ctx.reply("OK");

        Path root = ctx.baseDir().toAbsolutePath().normalize();
        Path cwd = ctx.currentDir().toAbsolutePath().normalize();

        String rel;
        try {
            rel = root.relativize(cwd).toString();
        } catch (IllegalArgumentException ex) {
            // Si no se puede relativizar, devolvemos "." y reseteamos
            ctx.setCurrentDir(root);
            rel = "";
        }

        if (rel == null || rel.isEmpty()) {
            rel = ".";
        }

        ProtocolIO.writeLine(ctx.out(), rel);
        return false;
    }
}
