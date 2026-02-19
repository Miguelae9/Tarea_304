package psp.ud03.tarea34.net;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Utilidades de E/S para nuestro protocolo TCP.
 * <p>
 * IDEA CLAVE:
 * - El protocolo combina TEXTO (líneas) y BINARIO (tamaño + bytes).
 * - NO usamos BufferedReader en el socket porque puede leer "de más" y dejar bytes
 * binarios atrapados en su buffer => desincroniza el protocolo.
 * <p>
 * Con ProtocolIO:
 * - readLine(...) lee exactamente hasta '\n' sin preleer bytes extra.
 * - writeLine(...) envía una línea con '\n'.
 * - copyNBytes(...) mueve exactamente N bytes (transferencia binaria).
 */
public final class ProtocolIO {

    /**
     * Clase de utilidades: no se instancia.
     */
    private ProtocolIO() {
    }

    /**
     * Lee una línea terminada en '\n' desde un InputStream (normalmente el del socket).
     * <p>
     * Importante:
     * - Devuelve la línea SIN el '\n' final.
     * - Ignora '\r' para soportar CRLF (\r\n) típico de Windows.
     * - Devuelve null si el stream se cierra y no había nada leído (EOF real).
     * <p>
     * Por qué está implementado "a mano":
     * - Para evitar el buffer interno de BufferedReader y mantener el stream limpio
     * cuando después toca leer binario (long + bytes).
     */
    public static String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int b;

        // Leemos byte a byte hasta '\n' o EOF (-1)
        while ((b = in.read()) != -1) {
            if (b == '\n') {
                break; // fin de línea
            }
            if (b != '\r') {
                baos.write(b); // guardamos el byte de texto
            }
        }

        // Si el socket se cerró (b == -1) y no leímos nada => no hay línea => null
        if (b == -1 && baos.size() == 0) {
            return null;
        }

        // Convertimos los bytes a String en UTF-8
        return baos.toString(StandardCharsets.UTF_8);
    }

    /**
     * Escribe una línea al OutputStream (normalmente el del socket).
     * <p>
     * Siempre termina en '\n' y hace flush.
     * <p>
     * Se usa para enviar respuestas tipo:
     * - "OK\n"
     * - "KO\n"
     * - líneas del list/show
     */
    public static void writeLine(OutputStream out, String line) throws IOException {
        out.write((line + "\n").getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    /**
     * Copia EXACTAMENTE n bytes desde 'in' hasta 'out'.
     * <p>
     * Se usa en transferencias binario:
     * - upload: socket -> fichero (servidor)
     * - upload: fichero -> socket (cliente)
     * - download: fichero -> socket (servidor)
     * - download: socket -> fichero (cliente)
     * <p>
     * Reglas:
     * - Lee en bloques (buffer 4096) para rendimiento.
     * - Si el socket se cierra antes de recibir n bytes => EOFException.
     * (Eso indica transferencia incompleta).
     */
    public static void copyNBytes(InputStream in, OutputStream out, long n) throws IOException {
        byte[] buf = new byte[4096];
        long remaining = n;

        while (remaining > 0) {
            int toRead = (int) Math.min(buf.length, remaining);

            int read = in.read(buf, 0, toRead);

            // Si read == -1, se cerró el stream antes de terminar => transferencia cortada
            if (read == -1) {
                throw new EOFException("Socket closed during binary transfer");
            }

            out.write(buf, 0, read);
            remaining -= read;
        }

        // Nos aseguramos de que lo enviado/escrito salga realmente
        out.flush();
    }
}
