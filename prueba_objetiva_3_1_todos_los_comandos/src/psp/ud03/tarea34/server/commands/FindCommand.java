package psp.ud03.tarea34.server.commands;

import psp.ud03.tarea34.net.ProtocolIO;
import psp.ud03.tarea34.server.CommandContext;
import psp.ud03.tarea34.server.PathResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Busca recursivo por nombre que contenga pattern (en dir o cwd).
 * Respuesta: OK + coincidencias (una por línea) + línea vacía (fin). KO si error.
 * <p>
 * Comando: find <pattern> [dir]
 * <p>
 * Busca recursivamente por nombre (contiene pattern, case-insensitive).
 * Si no se indica dir, busca desde currentDir.
 * <p>
 * Respuesta:
 * - KO si faltan params o dir inválido.
 * - OK + líneas con rutas relativas (a dir) + línea vacía terminadora.
 */
public class FindCommand implements ICommand {

    private static final int MAX_RESULTS = 5000;

    @Override
    public boolean execute(CommandContext ctx, String params) throws IOException {

        if (params == null || params.trim().isEmpty()) {
            ctx.reply("KO");
            return false;
        }

        String[] parts = params.trim().split("\\s+", 2);
        String pattern = parts[0].toLowerCase(Locale.ROOT);
        String dirParam = (parts.length == 2) ? parts[1].trim() : "";

        Path start = PathResolver.resolve(ctx, dirParam);
        if (!Files.exists(start) || !Files.isDirectory(start) || !Files.isReadable(start)) {
            ctx.reply("KO");
            return false;
        }

        ctx.reply("OK");

        try (Stream<Path> st = Files.walk(start)) {
            List<String> results = st
                    .filter(p -> p.getFileName() != null)
                    .filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).contains(pattern))
                    .limit(MAX_RESULTS)
                    .map(p -> start.relativize(p).toString())
                    .sorted(Comparator.naturalOrder())
                    .collect(Collectors.toList());

            for (String r : results) {
                ProtocolIO.writeLine(ctx.out(), r);
            }
        }

        ProtocolIO.writeLine(ctx.out(), "");
        return false;
    }
}
