package psp.ud03.tarea34.server.commands;

import psp.ud03.tarea34.net.ProtocolIO;
import psp.ud03.tarea34.server.CommandContext;
import psp.ud03.tarea34.server.PathResolver;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Baja un fichero del servidor al cliente.
 * Protocolo:
 * <p>
 * Cliente: download X
 * <p>
 * Servidor: OK o KO
 * <p>
 * Si OK: servidor envía binario: long size + size bytes.
 *
 * <p>
 * Comando: download <ruta_fichero>
 * <p>
 * Función:
 * - Envía un fichero desde el servidor al cliente.
 * <p>
 * Protocolo (IMPORTANTE):
 * 1) Cliente manda: "download <ruta>\n"
 * 2) Servidor responde:
 * - "KO\n" si error
 * - "OK\n" si correcto
 * 3) Si OK, servidor envía en BINARIO:
 * - long size  (8 bytes)
 * - size bytes (contenido exacto del fichero)
 * <p>
 * Si no se respeta este orden, el cliente se queda colgado o se desincroniza.
 */
public class DownloadCommand implements ICommand {

    @Override
    public boolean execute(CommandContext ctx, String params) throws IOException {

        // 1) Validación básica
        if (params == null || params.length() == 0) {
            ctx.reply("KO");
            return false;
        }

        // 2) Resolver ruta del fichero en el servidor
        Path file = PathResolver.resolve(ctx, params);

        // 3) Comprobar que existe y es fichero
        if (!Files.exists(file)) {
            ctx.reply("KO");
            return false;
        }

        if (!Files.isRegularFile(file)) {
            ctx.reply("KO");
            return false;
        }

        // 4) Tamaño del fichero (para que el cliente sepa cuántos bytes leer)
        long size = Files.size(file);

        // 5) Enviar OK (texto)
        ctx.reply("OK");

        // 6) Enviar tamaño y contenido (binario)
        ctx.dos().writeLong(size);
        ctx.dos().flush();

        try (FileInputStream fis = new FileInputStream(file.toFile())) {
            ProtocolIO.copyNBytes(fis, ctx.out(), size);
        }

        return false;
    }
}
