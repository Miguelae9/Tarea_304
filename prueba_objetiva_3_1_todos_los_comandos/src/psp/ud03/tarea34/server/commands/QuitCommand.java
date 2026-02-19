package psp.ud03.tarea34.server.commands;

import psp.ud03.tarea34.server.CommandContext;

import java.io.IOException;

/**
 * Comando: quit
 * - Cierra la sesión de forma ordenada.
 */
public class QuitCommand implements ICommand {

    @Override
    public boolean execute(CommandContext ctx, String params) throws IOException {
        ctx.reply("OK");
        return true;
    }
}
