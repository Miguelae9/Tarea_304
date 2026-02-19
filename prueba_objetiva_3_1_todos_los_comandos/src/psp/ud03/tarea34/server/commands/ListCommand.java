package psp.ud03.tarea34.server.commands;

import psp.ud03.tarea34.net.ProtocolIO;
import psp.ud03.tarea34.server.CommandContext;
import psp.ud03.tarea34.server.PathResolver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Lista el contenido de un directorio del servidor.
 * Respuesta: OK + líneas "nombre KiB" + línea vacía (fin). KO si no existe/no es dir.
 *
 * <p>
 * Comando: list <ruta>
 * <p>
 * Función:
 * - Lista el contenido de un directorio del servidor.
 * <p>
 * Respuesta:
 * - KO si:
 * - faltan parámetros
 * - la ruta no existe o no es directorio
 * - error al listar (listFiles null)
 * <p>
 * - OK si todo va bien, y después:
 * - una línea por cada entrada: "nombre KiB"
 * - línea vacía final para indicar "fin del listado"
 * <p>
 * Nota:
 * - KiB solo se calcula para ficheros (para directorios se deja 0).
 */
public class ListCommand implements ICommand {

    @Override
    public boolean execute(CommandContext ctx, String params) throws IOException {

        // 1) Validación básica de parámetros
        if (params == null || params.length() == 0) {
            ctx.reply("KO");
            return false;
        }

        // 2) Resolver ruta:
        // - relativa => baseDir + params
        // - absoluta => tal cual (del servidor)
        Path pathList = PathResolver.resolve(ctx, params);

        // 3) Validar que existe y es un directorio
        if (!Files.exists(pathList) || !Files.isDirectory(pathList)) {
            ctx.reply("KO");
            return false;
        }

        // 4) Listar entradas del directorio
        File[] files = pathList.toFile().listFiles();
        if (files == null) {
            ctx.reply("KO");
            return false;
        }

        // 5) Enviar OK y después el listado
        ctx.reply("OK");

        for (int i = 0; i < files.length; i++) {
            File file = files[i];

            String name = file.getName();
            long kiB = 0;

            // Si es fichero, calculamos su tamaño aproximado en KiB
            if (file.isFile()) {
                long bytes = file.length();
                kiB = (bytes + 1023) / 1024; // redondeo hacia arriba
            }

            ProtocolIO.writeLine(ctx.out(), name + " " + kiB);
        }

        // 6) Línea vacía final = terminador del listado
        ProtocolIO.writeLine(ctx.out(), "");

        return false;
    }
}
