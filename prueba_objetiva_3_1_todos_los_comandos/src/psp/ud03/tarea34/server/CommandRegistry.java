package psp.ud03.tarea34.server;

import psp.ud03.tarea34.server.commands.*;

import java.util.*;

/**
 * Registro (diccionario) de comandos del servidor.
 * <p>
 * Objetivo:
 * - Evitar un switch gigante en ServerWorker.
 * - Hacer que añadir un comando nuevo sea sencillo:
 * 1) Crear clase NewCommand implements ICommand
 * 2) Registrar: commands.put("new", new NewCommand())
 * <p>
 * Así el ServerWorker no cambia aunque crezca el protocolo.
 */
public class CommandRegistry {

    /**
     * Mapa: nombre del comando -> implementación.
     * <p>
     * Ejemplos:
     * "list" -> new ListCommand()
     * "upload" -> new UploadCommand()
     */
    private final Map<String, ICommand> commands;

    /**
     * Constructor: crea el mapa y registra los comandos "base".
     */
    public CommandRegistry() {
        commands = new HashMap<String, ICommand>();
        registerDefaults();
    }

    /**
     * Registra los comandos disponibles por defecto.
     * <p>
     * Nota:
     * - Aquí es donde se "activa" un comando.
     * - Si mañana quieres añadir otro, lo metes aquí o llamas a register().
     */
    private void registerDefaults() {
        commands.put("list", new ListCommand());
        commands.put("show", new ShowCommand());
        commands.put("delete", new DeleteCommand());
        commands.put("upload", new UploadCommand());
        commands.put("download", new DownloadCommand());
        commands.put("quit", new QuitCommand());
        commands.put("ping", new PingCommand());
        commands.put("mkdir", new MkdirCommand());
        commands.put("rename", new RenameCommand());
        ICommand info = new InfoCommand();
        commands.put("info", info);
        commands.put("stat", info); // alias
        commands.put("exists", new ExistsCommand());
        commands.put("copy", new CopyCommand());

        // Navegación simple por directorios (por conexión)
        commands.put("pwd", new PwdCommand());
        commands.put("cd", new CdCommand());

        // Utilidades típicas
        commands.put("touch", new TouchCommand());
        commands.put("size", new SizeCommand());

        // Lectura/búsqueda típicas de examen (solo texto)
        commands.put("head", new HeadCommand());
        commands.put("tail", new TailCommand());
        commands.put("tree", new TreeCommand());
        commands.put("find", new FindCommand());
        commands.put("hash", new HashCommand());

        // help necesita el registry
        commands.put("help", new HelpCommand(this));

    }

    /**
     * Obtiene el comando asociado a un nombre.
     * Si no existe, devuelve null.
     *
     * @param name comando escrito por el cliente (por ejemplo "list")
     */
    public ICommand get(String name) {
        return commands.get(name);
    }

    /**
     * Permite registrar comandos nuevos en tiempo de arranque.
     *
     * @param name    nombre del comando (lo que escribe el cliente)
     * @param command clase que implementa la lógica
     */
    public void register(String name, ICommand command) {
        commands.put(name, command);
    }

    public List<String> namesSorted() {
        List<String> names = new ArrayList<>(commands.keySet());

        // Opcional: ocultar alias para no confundir en el help
        names.remove("stat");

        Collections.sort(names);
        return names;
    }

}
