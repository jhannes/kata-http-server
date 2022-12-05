package no.soprasteria.http;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class HttpServer {

    private final ServerSocket serverSocket;
    private final Path basePath;

    public HttpServer(int port, Path basePath) throws IOException {
        serverSocket = new ServerSocket(port);
        this.basePath = basePath;
    }

    public static void main(String[] args) throws IOException {
        new HttpServer(8080, null).start();
    }

    void start() {
        new Thread(() -> {
            try (var clientSocket = serverSocket.accept()) {
                handleClient(clientSocket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void handleClient(Socket clientSocket) throws IOException {
        var requestLine = readLine(clientSocket).split(" ");
        var requestTarget = requestLine[1];

        var requestPath = basePath.resolve(requestTarget.substring(1));
        if (Files.isRegularFile(requestPath)) {
            var content = "Hello world";
            clientSocket.getOutputStream().write("""
                HTTP/1.1 200 OK\r
                Content-Length: %d\r
                Connection: close\r
                \r
                %s""".formatted(content.length(), content).getBytes());
            return;
        }


        var content = "Not found " + requestTarget;
        clientSocket.getOutputStream().write("""
                HTTP/1.1 404 NOT FOUND\r
                Content-Length: %d\r
                Connection: close\r
                \r
                %s""".formatted(content.length(), content).getBytes());
    }

    private static String readLine(Socket clientSocket) throws IOException {
        var line = new StringBuilder();
        int c;
        while ((c = clientSocket.getInputStream().read()) != '\r') {
            line.append((char) c);
        }
        return line.toString();
    }

    public URL getURL() throws MalformedURLException {
        return new URL("http", "localhost", serverSocket.getLocalPort(), "/");
    }
}
