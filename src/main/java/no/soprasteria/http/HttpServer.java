package no.soprasteria.http;

import java.io.FileInputStream;
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
        new HttpServer(8080, Path.of("src", "main", "resources")).start();
    }

    void start() {
        new Thread(() -> {
            while (!Thread.interrupted()) {
                try (var clientSocket = serverSocket.accept()) {
                    handleClient(clientSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Exited");
        }).start();
    }

    private void handleClient(Socket clientSocket) throws IOException {
        var requestLine = readLine(clientSocket).split(" ");
        var requestTarget = requestLine[1];

        var requestPath = basePath.resolve(requestTarget.substring(1));
        if (Files.isDirectory(requestPath)) {
            requestPath = requestPath.resolve("index.html");
        }
        if (Files.isRegularFile(requestPath)) {
            clientSocket.getOutputStream().write("""
                HTTP/1.1 200 OK\r
                Content-Length: %d\r
                Connection: close\r
                \r
                """.formatted(Files.size(requestPath)).getBytes());
            try (var content = new FileInputStream(requestPath.toFile())) {
                content.transferTo(clientSocket.getOutputStream());
            }
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
            if (c == -1) break;
            line.append((char) c);
        }
        return line.toString();
    }

    public URL getURL() throws MalformedURLException {
        return new URL("http", "localhost", serverSocket.getLocalPort(), "/");
    }
}
