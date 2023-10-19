package com.soprasteria.johannes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class HttpServer {

    private final ServerSocket serverSocket;
    private final Path contentRoot;

    public HttpServer(int port, Path contentRoot) throws IOException {
        serverSocket = new ServerSocket(port);
        this.contentRoot = contentRoot;
    }

    public static void main(String[] args) throws IOException {
        new HttpServer(22080, Path.of("src", "main", "resources")).start();
    }

    void start() {
        new Thread(() -> {
            try {
                runServer();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private void runServer() throws IOException {
        //noinspection InfiniteLoopStatement
        while (true) {
            var clientSocket = serverSocket.accept();
            processRequest(clientSocket);
        }
    }

    private void processRequest(Socket clientSocket) throws IOException {
        var startLine = readLine(clientSocket);
        var parts = startLine.split(" ", 3);

        var requestMethod = parts[0];
        var requestTarget = parts[1];

        var headers = new HashMap<String, String>();
        String headerLine;
        while (!(headerLine = readLine(clientSocket)).isBlank()) {
            var headerParts = headerLine.split(":\\s*", 2);
            headers.put(headerParts[0], headerParts[1].trim());
        }


        if (requestTarget.equals("/api/login")) {
            handleApiLogin(requestMethod, clientSocket, headers);
            return;
        }

        var requestPath = contentRoot.resolve(requestTarget.substring(1));
        System.out.println("Request path " + requestPath.toAbsolutePath());
        if (Files.exists(requestPath)) {
            var response = Files.readString(requestPath);
            clientSocket.getOutputStream().write(
                    """
                    HTTP/1.1 200 OK\r
                    Content-Length: %d\r
                    Connection: close\r
                    \r
                    %s""".formatted(response.length(), response).getBytes()
            );
            return;
        }

        var response = "Not found " + requestTarget;
        clientSocket.getOutputStream().write(
                """
                HTTP/1.1 404 Not Found\r
                Content-Length: %d\r
                Content-Type: text/plain\r
                Connection: close\r
                \r
                %s""".formatted(response.length(), response).getBytes()
        );

    }

    private static void handleApiLogin(String requestMethod, Socket clientSocket, HashMap<String, String> headers) throws IOException {
        if (requestMethod.equals("POST")) {
            var contentLength = Integer.parseInt(headers.get("Content-Length"));

            var requestBody = new StringBuilder();
            for (int i = 0; i < contentLength; i++) {
                requestBody.append((char) clientSocket.getInputStream().read());
            }
            var parameterParts = requestBody.toString().split("=");
            var username = parameterParts[1];

            var response = "You are now logged in";
            clientSocket.getOutputStream().write(
                    """
                    HTTP/1.1 200 OK\r
                    Content-Length: %d\r
                    Connection: close\r
                    Set-Cookie: session=%s\r
                    \r
                    %s""".formatted(response.length(), username, response).getBytes()
            );
            return;
        }


        var cookie = headers.get("Cookie");

        if (cookie == null) {
            var response = "Not logged in";
            clientSocket.getOutputStream().write(
                    """
                    HTTP/1.1 401 Unauthenticated\r
                    Content-Length: %d\r
                    Connection: close\r
                    \r
                    %s""".formatted(response.length(), response).getBytes()
            );
            return;
        }

        var cookieParts = cookie.split("=", 2);
        var response = "Welcome " + cookieParts[1];
        clientSocket.getOutputStream().write(
                """
                HTTP/1.1 200 OK\r
                Content-Length: %d\r
                Connection: close\r
                \r
                %s""".formatted(response.length(), response).getBytes()
        );
    }

    private static String readLine(Socket clientSocket) throws IOException {
        var startLine = new StringBuilder();
        int c;
        while ((c = clientSocket.getInputStream().read()) != '\n') {
            startLine.append((char)c);
        }
        return startLine.toString();
    }

    public URL getURL() throws MalformedURLException {
        return new URL("http", "localhost", serverSocket.getLocalPort(), "/");
    }
}