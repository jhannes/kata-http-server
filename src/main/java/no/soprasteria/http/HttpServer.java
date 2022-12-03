package no.soprasteria.http;

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
            var responseHeader = """
                HTTP/1.1 200 OK\r
                Connection: close\r
                Transfer-Encoding: chunked\r
                Content-Type: text/html; charset=utf-8\r
                \r
                """;
            clientSocket.getOutputStream().write(responseHeader.getBytes(StandardCharsets.UTF_8));

            var body = "Unknown file " + requestTarget;
            var contentLength = body.getBytes(StandardCharsets.UTF_8).length;
            clientSocket.getOutputStream().write((Integer.toHexString(contentLength) + "\r\n" +
                                                  body + "\r\n" +
                                                  0 + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
            return;
        }


        var responseHeader = """
                HTTP/1.1 404 NOT FOUND\r
                Connection: close\r
                Transfer-Encoding: chunked\r
                Content-Type: text/html; charset=utf-8\r
                \r
                """;
        clientSocket.getOutputStream().write(responseHeader.getBytes(StandardCharsets.UTF_8));

        var body = "Unknown file " + requestTarget;
        var contentLength = body.getBytes(StandardCharsets.UTF_8).length;
        clientSocket.getOutputStream().write((Integer.toHexString(contentLength) + "\r\n" +
                                              body + "\r\n" +
                                              0 + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
    }

    private static String readLine(InputStream inputStream) throws IOException {
        var result = new StringBuilder();
        int c;
        while ((c = inputStream.read()) != '\r') {
            result.append((char)c);
        }
        return result.toString();
    }

    public URL getURL() throws MalformedURLException {
        return new URL("http", "localhost", socket.getLocalPort(), "/");
    }
}
