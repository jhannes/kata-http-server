package no.soprasteria.http;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class HttpServer {

    public static void main(String[] args) throws IOException {
        new HttpServer(8080, null).startServer();
    }

    private final ServerSocket socket;
    private final Path baseDir;

    public HttpServer(int port, Path baseDir) throws IOException {
        socket = new ServerSocket(port);
        this.baseDir = baseDir;
    }

    void startServer() {
        new Thread(() -> {
            while (!Thread.interrupted()) {
                try (var clientSocket = socket.accept()) {
                    handleRequest(clientSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void handleRequest(Socket clientSocket) throws IOException {
        var requestLine = readLine(clientSocket.getInputStream());
        System.out.println(requestLine);
        var parts = requestLine.split(" ");
        var requestTarget = parts[1];

        var resolvedPath = baseDir.resolve(requestTarget.substring(1));
        if (Files.exists(resolvedPath)) {
            writeHeader(clientSocket, 200, "OK", "text/html; charset=utf-8");
            clientSocket.getOutputStream().write((Long.toHexString(Files.size(resolvedPath)) + "\r\n").getBytes());
            try (var inputStream = new FileInputStream(resolvedPath.toFile())) {
                inputStream.transferTo(clientSocket.getOutputStream());
            }
            clientSocket.getOutputStream().write(("\r\n" + 0 + "\r\n\r\n").getBytes());
        } else {
            writeHeader(clientSocket, 404, "NOT FOUND", "text/html; charset=utf-8");
            writeResponseBody(clientSocket, "Unknown file " + requestTarget);
        }
    }

    private static void writeResponseBody(Socket clientSocket, String body) throws IOException {
        var contentLength = body.getBytes(StandardCharsets.UTF_8).length;
        clientSocket.getOutputStream().write((Integer.toHexString(contentLength) + "\r\n" +
                                              body + "\r\n" +
                                              0 + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
    }

    private static void writeHeader(Socket clientSocket, int responseCode, String responseMessage, String contentType) throws IOException {
        var responseHeader = "HTTP/1.1 " + responseCode + " " + responseMessage + "\r\n" +
                             "Connection: close\r\n" +
                             "Transfer-Encoding: chunked\r\n" +
                             "Content-Type: " + contentType + "\r\n" +
                             "\r\n";
        clientSocket.getOutputStream().write(responseHeader.getBytes(StandardCharsets.UTF_8));
    }

    private static String readLine(InputStream inputStream) throws IOException {
        var result = new StringBuilder();
        int c;
        while ((c = inputStream.read()) != '\r') {
            result.append((char) c);
        }
        return result.toString();
    }

    public URL getURL() throws MalformedURLException {
        return new URL("http", "localhost", socket.getLocalPort(), "/");
    }
}
