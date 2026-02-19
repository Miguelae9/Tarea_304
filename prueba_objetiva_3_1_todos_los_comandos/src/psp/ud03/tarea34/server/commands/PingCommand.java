package psp.ud03.tarea34.server.commands;

import psp.ud03.tarea34.net.ProtocolIO;
import psp.ud03.tarea34.server.CommandContext;

import java.io.IOException;

/**
 * Comando de prueba para ver si el servidor responde.
 * Respuesta: OK (o “PONG”, según implementación; en tu proyecto es OK).
 * <p>
 * Comando: ping
 * <p>
 * Objetivo:
 * - Comprobar que la conexión está viva y que el servidor responde.
 * <p>
 * Protocolo (texto):
 * 1) Servidor responde: OK
 * 2) Servidor envía: pong
 */
public class PingCommand implements ICommand {

    @Override
    public boolean execute(CommandContext ctx, String params) throws IOException {
        ctx.reply("OK");
        ProtocolIO.writeLine(ctx.out(), "pong");
        return false;
    }
}
