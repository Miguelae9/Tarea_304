package psp.ud03.tarea34.server.commands;

import psp.ud03.tarea34.server.CommandContext;

import java.io.IOException;

/**
 * Interfaz común de todos los comandos del servidor.
 * <p>
 * Contrato del protocolo:
 * - Cada comando recibe:
 * - ctx: streams del socket + ip + baseDir
 * - params: texto tras el nombre del comando (puede ser "")
 * <p>
 * - Devuelve:
 * - true  => el servidor debe cerrar la sesión con ese cliente (ej: quit)
 * - false => seguir atendiendo comandos en el mismo socket
 * <p>
 * Importante:
 * - Los comandos escriben sus respuestas al socket:
 * - Siempre empiezan por "OK" o "KO" en una línea.
 * - Si el comando implica binario (upload/download), tras el OK:
 * - se envía/lee long size
 * - se envían/leen exactamente size bytes
 */
public interface ICommand {
    boolean execute(CommandContext ctx, String params) throws IOException;
}
