package psp.ud03.tarea34.server.commands;

import psp.ud03.tarea34.net.ProtocolIO;
import psp.ud03.tarea34.server.CommandContext;
import psp.ud03.tarea34.server.PathResolver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Calcula el SHA-256 de un fichero y lo devuelve en hex.
 * Respuesta: OK + 1 línea con el hash. KO si error.
 * <p>
 * Comando: hash <ruta>
 * <p>
 * Calcula el SHA-256 de un fichero.
 * <p>
 * Respuesta:
 * - KO si falta ruta o no es fichero legible
 * - OK y luego 1 línea extra con el hash en hex
 */
public class HashCommand implements ICommand {

    @Override
    public boolean execute(CommandContext ctx, String params) throws IOException {

        if (params == null || params.trim().isEmpty()) {
            ctx.reply("KO");
            return false;
        }

        Path p = PathResolver.resolve(ctx, params.trim());
        if (!Files.exists(p) || !Files.isRegularFile(p) || !Files.isReadable(p)) {
            ctx.reply("KO");
            return false;
        }

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            ctx.reply("KO");
            return false;
        }

        try (InputStream is = Files.newInputStream(p)) {
            byte[] buf = new byte[8192];
            int r;
            while ((r = is.read(buf)) != -1) {
                md.update(buf, 0, r);
            }
        }

        String hex = toHex(md.digest());
        ctx.reply("OK");
        ProtocolIO.writeLine(ctx.out(), hex);
        return false;
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }
}
