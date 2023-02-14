package com.soprasteria.http;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class HttpServer {

    private final ServerSocket serverSocket;
    private final Path httpRoot;

    HttpServer(int port, Path httpRoot) throws IOException {
        serverSocket = new ServerSocket(port);
        this.httpRoot = httpRoot;

        new Thread(this::runServer).start();
    }

    private void runServer() {
        try {
            var clientSocket = serverSocket.accept();
            handleClient(clientSocket, httpRoot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void handleClient(Socket clientSocket, Path httpRoot1) throws IOException {
        String requestLine = readLine(clientSocket);
        var requestTarget = requestLine.split(" ")[1];

        var requestFile = httpRoot1.resolve(requestTarget.substring(1));
        if (Files.exists(requestFile)) {
            var body = Files.readString(requestFile);
            clientSocket.getOutputStream().write("""
                HTTP/1.1 200 OK\r
                Content-Length: %d\r
                Connection: close\r
                \r
                %s""".formatted(body.length(), body).getBytes());

        } else {
            var body = "Unknown path " + requestTarget;
            clientSocket.getOutputStream().write("""
                HTTP/1.1 404 Not found\r
                Content-Length: %d\r
                Connection: close\r
                Content-type: text/html\r
                \r
                %s""".formatted(body.length(), body).getBytes());
        }
    }

    private static String readLine(Socket clientSocket) throws IOException {
        var line = new StringBuilder();
        int c;
        while((c = clientSocket.getInputStream().read()) != -1) {
            if (c == '\n') {
                break;
            }
            line.append((char)c);
        }
        return line.toString();
    }

    public URL getURL() throws MalformedURLException {
        return new URL("http", "localhost", serverSocket.getLocalPort(), "/");
    }

    public static void main(String[] args) throws IOException {
        new HttpServer(8080, Path.of("src", "main", "resources"));
    }
}
