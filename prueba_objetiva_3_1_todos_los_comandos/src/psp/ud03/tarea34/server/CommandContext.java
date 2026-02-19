package psp.ud03.tarea34.server;

import psp.ud03.tarea34.net.ProtocolIO;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;

/**
 * Contexto que se pasa a todos los comandos del servidor.
 * <p>
 * Objetivo:
 * - Evitar pasar 10 parámetros a cada comando.
 * - Centralizar en un objeto todo lo necesario para atender a un cliente:
 * - socket
 * - streams de entrada/salida
 * - DataInputStream/DataOutputStream para long/binario
 * - IP del cliente (para log)
 * - baseDir (directorio de trabajo del servidor)
 * <p>
 * Ventaja para ampliar comandos:
 * - Un comando nuevo solo necesita ctx + params.
 * - Si mañana añadimos algo (por ejemplo usuario autenticado),
 * lo metes aquí y no rompes todas las firmas.
 */
public class CommandContext {

    private final Socket socket;

    // Streams "crudos" del socket (los usamos para texto y para copiar bytes)
    private final InputStream in;
    private final OutputStream out;

    // Wrappers para leer/escribir binario (por ejemplo long size)
    private final DataInputStream dis;
    private final DataOutputStream dos;

    // IP del cliente para el server.log
    private final String clientIp;

    // Directorio base en el que opera el servidor (por ejemplo "." si arrancas en Desktop)
    private final Path baseDir;

    // Directorio actual (por conexión). Inicialmente = baseDir.
    private Path currentDir;

    // Último comando recibido (texto completo) para log unificado
    private String lastReceived;

    /**
     * Construye el contexto a partir de un Socket ya aceptado.
     * <p>
     * Importante:
     * - dis y dos se crean SOBRE los streams del socket.
     * - No cerramos aquí los streams: se cierran al cerrar el socket en el worker.
     */
    public CommandContext(Socket socket, Path baseDir) throws IOException {
        this.socket = socket;
        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();

        // DataInput/Output para leer/escribir "long" en upload/download
        this.dis = new DataInputStream(in);
        this.dos = new DataOutputStream(out);

        // IP para auditoría
        this.clientIp = socket.getInetAddress().getHostAddress();

        // directorio actual/raíz del servidor
        this.baseDir = baseDir;

        // cwd por conexión
        this.currentDir = baseDir;
    }

    // Getters simples (estilo básico, muy claro para examen)
    public Socket socket() {
        return socket;
    }

    public InputStream in() {
        return in;
    }

    public OutputStream out() {
        return out;
    }

    public DataInputStream dis() {
        return dis;
    }

    public DataOutputStream dos() {
        return dos;
    }

    public String clientIp() {
        return clientIp;
    }

    public Path baseDir() {
        return baseDir;
    }

    public Path currentDir() {
        return currentDir;
    }

    public void setCurrentDir(Path currentDir) {
        this.currentDir = currentDir;
    }

    /**
     * Guarda el último comando recibido en formato texto.
     * (El ServerWorker lo llama antes de ejecutar cada comando.)
     */
    public void setLastReceived(String lastReceived) {
        this.lastReceived = lastReceived;
    }

    /**
     * Envía una respuesta al cliente (OK/KO) y escribe un log unificado:
     * RECV: <comando> -> RESP: <status>
     * <p>
     * Regla:
     * - Solo debe llamarse UNA vez por comando (para la primera respuesta OK/KO).
     * - El resto de datos (list/show/download bytes...) se envían con ctx.out()/ctx.dos().
     */
    public void reply(String status) throws IOException {
        ProtocolIO.writeLine(out, status);

        String received = lastReceived;
        if (received == null) {
            received = "";
        }

        ServerLogger.log(clientIp, "RECV: " + received + " -> RESP: " + status);
    }
}
