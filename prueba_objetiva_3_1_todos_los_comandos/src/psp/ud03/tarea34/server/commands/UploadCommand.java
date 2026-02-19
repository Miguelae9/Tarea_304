package psp.ud03.tarea34.server.commands;

import psp.ud03.tarea34.net.ProtocolIO;
import psp.ud03.tarea34.server.CommandContext;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;

/**
 * Sube un fichero del cliente al servidor. El servidor se queda solo con el basename (para evitar rutas del cliente).
 * Protocolo:
 * <p>
 * Cliente: upload X
 * <p>
 * Servidor: OK o KO
 * <p>
 * Si OK: cliente envía binario: long size + size bytes.
 *
 * <p>
 * Comando: upload <nombre_fichero>
 * <p>
 * Protocolo:
 * 1) Cliente manda: "upload <nombre>\n" (texto)
 * 2) Servidor responde:
 * - "KO\n" si no puede recibir (faltan params, existe ya, permisos, etc.)
 * - "OK\n" si va a recibir
 * 3) Si OK, cliente manda en BINARIO:
 * - long size
 * - size bytes
 */
public class UploadCommand implements ICommand {

    @Override
    public boolean execute(CommandContext ctx, String params) throws IOException {

        // 1) Validación básica
        if (params == null || params.length() == 0) {
            ctx.reply("KO");
            return false;
        }

        // 2) Nos quedamos solo con el nombre (evita rutas del cliente)
        String fileName = Paths.get(params).getFileName().toString();

        // 3) Ruta destino: cwd (por conexión) + fileName
        Files.createDirectories(ctx.currentDir());
        Path target = ctx.currentDir().resolve(fileName).normalize();

        // 4) No sobrescribir
        if (Files.exists(target)) {
            ctx.reply("KO");
            return false;
        }

        /*
         * 5) Validación REAL antes del OK:
         * - Si no podemos crear/abrir el fichero, respondemos KO.
         * - Importante: NO mandar OK si luego no podremos escribir (desincroniza el protocolo).
         */
        OutputStream fos;
        try {
            fos = Files.newOutputStream(
                    target,
                    StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE
            );
        } catch (IOException e) {
            ctx.reply("KO");
            return false;
        }

        // 6) Ya podemos recibir => OK (texto + log unificado)
        ctx.reply("OK");

        // 7) Leer tamaño (binario)
        long size = ctx.dis().readLong();
        if (size < 0) {
            // Tras OK no mandamos KO: cortamos
            try {
                fos.close();
            } catch (IOException ignored) {
            }
            throw new IOException("Negative upload size");
        }

        // 8) Recibir bytes exactos
        try (OutputStream outFile = fos) {
            ProtocolIO.copyNBytes(ctx.in(), outFile, size);
        }

        return false;
    }
}
