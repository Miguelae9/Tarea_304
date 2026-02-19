package psp.ud03.tarea34.client;

import psp.ud03.tarea34.net.ProtocolIO;

import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Cliente TCP de gestión remota de ficheros.
 * <p>
 * Características clave:
 * - Usa ProtocolIO.readLine/writeLine para el TEXTO del protocolo.
 * - Usa DataInputStream/DataOutputStream para BINARIO (long size).
 * - NO usa BufferedReader en el socket para no romper el modo texto/binario.
 * <p>
 * Directorio de trabajo del cliente:
 * - cwd = directorio actual desde el que se ejecuta el cliente (".")
 * - download guarda aquí mismo.
 * - upload lee desde aquí mismo (o ruta absoluta si la escribes).
 */
public class MainFileClientApp {

    public static void main(String[] args) throws IOException {

        // 1) Host/puerto por defecto
        String host = "localhost";
        int port = 2121;

        // 2) Si se pasan argumentos, se usan
        if (args.length >= 1) {
            host = args[0];
        }
        if (args.length >= 2) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException ignored) {
                port = 2121;
            }
        }

        // 3) Directorio actual del cliente
        // Si ejecutas el jar en Desktop, cwd será Desktop.
        Path cwd = Paths.get(".").toAbsolutePath().normalize();

        // 4) Abrimos socket y streams una sola vez (sesión persistente)
        try (Scanner scanner = new Scanner(System.in);
             Socket socket = new Socket(host, port)) {

            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            // Para leer/escribir long en binario
            DataInputStream dis = new DataInputStream(in);
            DataOutputStream dos = new DataOutputStream(out);

            boolean exit = false;

            // 5) Bucle interactivo
            while (!exit) {

                printMenu();
                System.out.print("Introduce the command: ");
                String commandLine = scanner.nextLine();

                if (commandLine == null) {
                    commandLine = "";
                }

                commandLine = commandLine.trim();

                /*
                 * Ignorar entrada vacía:
                 * - No ejecuta nada
                 * - Vuelve al menú sin enviar nada al servidor
                 */
                if (!commandLine.isEmpty()) {

                    // 6) Parse básico: cmd + params
                    String cmd;
                    String params;

                    int pos = commandLine.indexOf(' ');
                    if (pos == -1) {
                        cmd = commandLine;
                        params = "";
                    } else {
                        cmd = commandLine.substring(0, pos);
                        params = commandLine.substring(pos + 1).trim();
                    }

                    /*
                     * CASO ESPECIAL: upload
                     *
                     * Motivo:
                     * - Upload mezcla TEXTO + BINARIO.
                     * - Primero se manda "upload nombre\n" y se espera OK/KO.
                     * - Si OK, se manda long size + bytes.
                     *
                     * Además, VALIDAMOS el fichero LOCAL ANTES de hablar con el servidor.
                     * Así evitamos:
                     * - enviar "upload ..." y luego no poder mandar los bytes (desincroniza el protocolo).
                     */
                    if ("upload".equals(cmd)) {

                        boolean canUpload = true;

                        // 1) Debe haber parámetro
                        if (params.length() == 0) {
                            System.out.println("Missing local file.");
                            canUpload = false;
                        }

                        // 2) Resolver ruta local:
                        // - si no es absoluta, se entiende respecto a cwd
                        Path local = null;
                        if (canUpload) {
                            local = Paths.get(params);
                            if (!local.isAbsolute()) {
                                local = cwd.resolve(local).normalize();
                            }
                        }

                        // 3) Validar que existe y es fichero
                        if (canUpload) {
                            if (!Files.exists(local) || !Files.isRegularFile(local)) {
                                System.out.println("Local file not found: " + local);
                                canUpload = false; // MUY IMPORTANTE: no enviamos nada al servidor
                            }
                        }

                        if (canUpload) {
                            // 4) Enviar comando al servidor pero solo con el nombre:
                            //    upload <nombre>
                            // Esto evita mandar rutas del cliente (no tienen sentido en el servidor).
                            String onlyName = local.getFileName().toString();
                            ProtocolIO.writeLine(out, "upload " + onlyName);

                            // 5) Leer status del servidor (OK/KO)
                            String status = ProtocolIO.readLine(in);
                            if (status == null) {
                                break;
                            }
                            System.out.println(status);

                            // 6) Si OK, enviar tamaño + bytes (binario)
                            if ("OK".equals(status)) {
                                long size = Files.size(local);

                                // Enviamos el long (8 bytes)
                                dos.writeLong(size);
                                dos.flush();

                                // Enviamos EXACTAMENTE size bytes
                                try (InputStream fis = Files.newInputStream(local, StandardOpenOption.READ)) {
                                    ProtocolIO.copyNBytes(fis, out, size);
                                }

                                System.out.println("Upload sent: " + onlyName + " (" + size + " bytes)");
                            }
                        }

                    } else {

                        /*
                         * FLUJO GENERAL (list/show/delete/download/quit/unknown...)
                         *
                         * 1) Enviar comando en texto
                         * 2) Leer "OK" o "KO"
                         * 3) Si OK, leer el resto según el comando
                         */
                        ProtocolIO.writeLine(out, commandLine);

                        String status = ProtocolIO.readLine(in);
                        if (status == null) {
                            break;
                        }
                        System.out.println(status);

                        if ("OK".equals(status)) {

                            switch (cmd) {

                                case "list":
                                    handleList(in);
                                    break;

                                case "show":
                                    handleShow(in);
                                    break;

                                case "head":
                                case "tail":
                                    // head/tail = mismo formato que show: N + N líneas
                                    handleShow(in);
                                    break;

                                case "download":
                                    handleDownload(params, in, dis, cwd);
                                    break;

                                case "help":
                                    // help = lista de líneas terminada en línea vacía
                                    for (String line : readList(in)) {
                                        System.out.println(line);
                                    }
                                    break;

                                case "tree":
                                case "find":
                                    // tree/find = lista de líneas terminada en línea vacía
                                    for (String line : readList(in)) {
                                        System.out.println(line);
                                    }
                                    break;

                                case "ping": {
                                    // ping = 1 línea extra (pong)
                                    String line = ProtocolIO.readLine(in);
                                    if (line == null) throw new EOFException("Server closed connection during ping");
                                    System.out.println(line);
                                    break;
                                }

                                case "exists": {
                                    // exists = 1 línea extra (true/false)
                                    String line = ProtocolIO.readLine(in);
                                    if (line == null) throw new EOFException("Server closed connection during exists");
                                    System.out.println(line);
                                    break;
                                }

                                case "pwd": {
                                    // pwd = 1 línea extra (cwd)
                                    String line = ProtocolIO.readLine(in);
                                    if (line == null) throw new EOFException("Server closed connection during pwd");
                                    System.out.println(line);
                                    break;
                                }

                                case "size": {
                                    // size = 1 línea extra (bytes)
                                    String line = ProtocolIO.readLine(in);
                                    if (line == null) throw new EOFException("Server closed connection during size");
                                    System.out.println(line);
                                    break;
                                }

                                case "hash": {
                                    // hash = 1 línea extra (sha-256)
                                    String line = ProtocolIO.readLine(in);
                                    if (line == null) throw new EOFException("Server closed connection during hash");
                                    System.out.println(line);
                                    break;
                                }

                                case "info":
                                case "stat": {
                                    // info/stat = 4 líneas (según el InfoCommand)
                                    for (int i = 0; i < 4; i++) {
                                        String line = ProtocolIO.readLine(in);
                                        if (line == null)
                                            throw new EOFException("Server closed connection during info");
                                        System.out.println(line);
                                    }
                                    break;
                                }

                                default:
                                    // mkdir/rename/delete/copy/quit -> no traen más datos
                                    break;
                            }
                        }

                        // Salida limpia del bucle si quit fue OK (quit no lleva params)
                        if ("quit".equals(cmd) && "OK".equals(status) && pos == -1) {
                            exit = true;
                        }
                    }
                }
            }

        }
    }

    /**
     * Menú por consola.
     */
    private static void printMenu() {
        System.out.println("Commands");
        System.out.println("list <route>");
        System.out.println("show <route>");
        System.out.println("delete <route>");
        System.out.println("upload <local_file>");
        System.out.println("download <server_file>");
        System.out.println("help");
        System.out.println("ping");
        System.out.println("mkdir <dir>");
        System.out.println("rename <src> <dst>");
        System.out.println("info <route>");
        System.out.println("exists <route>");
        System.out.println("copy <src> <dst>");
        System.out.println("pwd");
        System.out.println("cd <dir>");
        System.out.println("touch <file>");
        System.out.println("size <file>");
        System.out.println("head <file> [n]");
        System.out.println("tail <file> [n]");
        System.out.println("tree [dir]");
        System.out.println("find <pattern> [dir]");
        System.out.println("hash <file>");
        System.out.println("quit");
    }

    /**
     * Lee la respuesta del comando list:
     * - El servidor manda líneas hasta una línea vacía.
     */
    private static void handleList(InputStream in) throws IOException {
        String line = ProtocolIO.readLine(in);

        while (line != null && line.length() != 0) {
            System.out.println(line);
            line = ProtocolIO.readLine(in);
        }

        if (line == null) {
            throw new EOFException("Server closed connection during list");
        }
    }

    /**
     * Lee la respuesta del comando show:
     * - El servidor manda N y luego N líneas.
     */
    private static void handleShow(InputStream in) throws IOException {
        String nStr = ProtocolIO.readLine(in);
        if (nStr == null) {
            throw new EOFException("Server closed connection during show");
        }

        int n = Integer.parseInt(nStr);
        System.out.println(nStr);

        for (int i = 0; i < n; i++) {
            String line = ProtocolIO.readLine(in);
            if (line == null) {
                throw new EOFException("Server closed connection during show content");
            }
            System.out.println(line);
        }
    }

    /**
     * Lee la respuesta del comando download:
     * <p>
     * Protocolo:
     * - Tras "OK", el servidor manda:
     * long size
     * size bytes
     * <p>
     * Guardado:
     * - Se guarda en el directorio actual del cliente (cwd).
     */
    private static void handleDownload(String params, InputStream in, DataInputStream dis, Path cwd) throws
            IOException {

        if (params == null || params.isEmpty()) {
            System.out.println("Missing server file.");
            return;
        }

        // 1) Leer tamaño (binario)
        long size = dis.readLong();

        // 2) Elegir nombre local (solo el nombre, sin rutas)
        String fileName = Paths.get(params).getFileName().toString();
        Path target = cwd.resolve(fileName).normalize();

        // 3) Recibir exactamente size bytes y guardarlos
        try (OutputStream fos = Files.newOutputStream(
                target,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        )) {
            ProtocolIO.copyNBytes(in, fos, size);
        }

        System.out.println("Downloaded: " + target + " (" + size + " bytes)");
    }

    private static List<String> readList(InputStream in) throws IOException {
        List<String> lines = new ArrayList<>();
        while (true) {
            String line = ProtocolIO.readLine(in);
            if (line == null || line.isEmpty()) break;
            lines.add(line);
        }
        return lines;
    }

}
