package psp.ud03.tarea34.server.commands;

import psp.ud03.tarea34.net.ProtocolIO;
import psp.ud03.tarea34.server.CommandContext;
import psp.ud03.tarea34.server.PathResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Listado recursivo tipo árbol del directorio (si no pasas dir, usa el cwd).
 * Respuesta: OK + líneas + línea vacía (fin). KO si error.
 * <p>
 * Comando: tree [dir]
 * <p>
 * Lista recursivamente el contenido de un directorio con indentación.
 * Si no se indica dir, usa el currentDir.
 * <p>
 * Respuesta:
 * - KO si no existe o no es directorio legible.
 * - OK + líneas + línea vacía terminadora.
 */
public class TreeCommand implements ICommand {

    private static final int MAX_ITEMS = 20000; // límite razonable

    @Override
    public boolean execute(CommandContext ctx, String params) throws IOException {

        Path start = PathResolver.resolve(ctx, (params == null) ? "" : params.trim());

        if (!Files.exists(start) || !Files.isDirectory(start) || !Files.isReadable(start)) {
            ctx.reply("KO");
            return false;
        }

        ctx.reply("OK");

        int[] count = {0};
        // Primera línea: el directorio raíz relativo
        ProtocolIO.writeLine(ctx.out(), start.getFileName() == null ? start.toString() : start.getFileName().toString());
        walkDir(ctx, start, 1, count);

        // Terminador
        ProtocolIO.writeLine(ctx.out(), "");
        return false;
    }

    private void walkDir(CommandContext ctx, Path dir, int depth, int[] count) throws IOException {
        if (count[0] >= MAX_ITEMS) return;

        List<Path> children;
        try {
            children = Files.list(dir)
                    .sorted(Comparator.comparing(p -> p.getFileName().toString().toLowerCase()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return;
        }

        for (Path child : children) {
            if (count[0] >= MAX_ITEMS) return;
            count[0]++;

            ProtocolIO.writeLine(ctx.out(), indent(depth) + child.getFileName());

            if (Files.isDirectory(child) && Files.isReadable(child)) {
                walkDir(ctx, child, depth + 1, count);
            }
        }
    }

    private String indent(int depth) {
        StringBuilder sb = new StringBuilder(depth * 2);
        for (int i = 0; i < depth; i++) sb.append("  ");
        return sb.toString();
    }
}
