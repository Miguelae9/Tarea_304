package psp.ud03.tarea34.server.commands;

import psp.ud03.tarea34.net.ProtocolIO;
import psp.ud03.tarea34.server.CommandContext;
import psp.ud03.tarea34.server.CommandRegistry;

import java.io.IOException;

/**
 * Lista los comandos disponibles del servidor.
 * Respuesta: OK + comandos (1 por línea) + línea vacía (fin).
 * <p>
 * Comando: help
 * <p>
 * Objetivo:
 * - Mostrar al cliente los comandos disponibles en el servidor.
 * <p>
 * Protocolo (texto):
 * 1) Servidor responde: OK
 * 2) Servidor envía una lista de líneas (un comando por línea)
 * 3) Servidor envía una línea vacía como terminador (""), para que el cliente sepa cuándo parar.
 * <p>
 * Nota:
 * - Este comando necesita acceso al CommandRegistry para poder listar los nombres registrados.
 */
public class HelpCommand implements ICommand {

    private final CommandRegistry registry;

    public HelpCommand(CommandRegistry registry) {
        this.registry = registry;
    }

    @Override
    public boolean execute(CommandContext ctx, String params) throws IOException {
        ctx.reply("OK");

        // Enviamos cada comando en una línea.
        for (String name : registry.namesSorted()) {
            ProtocolIO.writeLine(ctx.out(), name);
        }

        // Terminador de lista: línea vacía.
        ProtocolIO.writeLine(ctx.out(), "");

        // false = no cerrar conexión
        return false;
    }
}
