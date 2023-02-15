package com.soprasteria.http;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class HttpServer {

    private final ServerSocket serverSocket;
    private final Path httpRoot;

    public HttpServer(int port, Path httpRoot) throws IOException {
        serverSocket = new ServerSocket(port);
        this.httpRoot = httpRoot;
        new Thread(this::handleServerSocket).start();
    }

    public static void main(String[] args) throws IOException {
        new HttpServer(8080, Path.of("src", "main", "resources"));
    }

    private void handleServerSocket() {
        try {
            while (!Thread.interrupted()) {
                var clientSocket = serverSocket.accept();
                handleClientSocket(clientSocket);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleClientSocket(Socket clientSocket) throws IOException {
        var requestLine = readLine(clientSocket);

        var requestMethod = requestLine.split(" ")[0];
        var requestTarget = requestLine.split(" ")[1];

        System.out.println(requestLine);
        var path = httpRoot.resolve(requestTarget.substring(1));
        if (requestTarget.equals("/api/hello")) {
            handleApiHello(clientSocket);
        } else if (requestTarget.equals("/api/login")) {
            handleLoginRequest(clientSocket, requestMethod);
        } else if (Files.exists(path)) {
            handleFileOnDisk(clientSocket, path);
        } else {
            handleUnknownPath(clientSocket, requestTarget);
        }
    }

    private static void handleApiHello(Socket clientSocket) throws IOException {
        var body = "Hællæ verden";
        clientSocket.getOutputStream().write("""
                HTTP/1.1 200 OK\r
                Content-Length: %d\r
                Connection: close\r
                \r
                %s""".formatted(body.length(), body).getBytes());
    }

    private static void handleLoginRequest(Socket clientSocket, String requestMethod) throws IOException {
        Map<String, String> headers = readHeaders(clientSocket);
        if (requestMethod.equals("GET")) {
            handleLoginGetRequest(clientSocket, headers);
        } else {
            handleLoginPostRequest(clientSocket, headers);
        }
    }

    private static void handleLoginPostRequest(Socket clientSocket, Map<String, String> headers) throws IOException {
        StringBuilder requestBody = readBody(clientSocket, headers);

        var parameterValue = requestBody.toString().split("=")[1];
        var body = "session=" + parameterValue;
        clientSocket.getOutputStream().write("""
                HTTP/1.1 200 OK\r
                Set-Cookie: %s\r
                Connection: close\r
                \r
                """.formatted(body).getBytes());
    }

    private static StringBuilder readBody(Socket clientSocket, Map<String, String> headers) throws IOException {
        var requestBody = new StringBuilder();
        var contentLength = Integer.parseInt(headers.get("Content-Length"));
        for (int i = 0; i < contentLength; i++) {
            requestBody.append((char) clientSocket.getInputStream().read());
        }
        return requestBody;
    }

    private static void handleLoginGetRequest(Socket clientSocket, Map<String, String> headers) throws IOException {
        var cookie = headers.get("Cookie");
        if (cookie != null) {
            var cookieValue = cookie.split("=")[1];
            var body = "Logged in as " + cookieValue;
            clientSocket.getOutputStream().write("""
                HTTP/1.1 200 OK\r
                Content-Length: %d\r
                Connection: close\r
                \r
                %s""".formatted(body.length(), body).getBytes());
            return;
        }

        var body = "Unauthorized user";
        clientSocket.getOutputStream().write("""
                HTTP/1.1 401 Unauthorized\r
                Content-Length: %d\r
                Connection: close\r
                \r
                %s""".formatted(body.length(), body).getBytes());
    }

    private static HashMap<String, String> readHeaders(Socket clientSocket) throws IOException {
        var headers = new HashMap<String, String>();

        String headerLine;
        while (!(headerLine = readLine(clientSocket).trim()).isEmpty()) {
            var parts = headerLine.split(":\\s*");
            headers.put(parts[0], parts[1]);
        }
        return headers;
    }

    private static void handleFileOnDisk(Socket clientSocket, Path path) throws IOException {
        var body = Files.readString(path);
        clientSocket.getOutputStream().write("""
                HTTP/1.1 200 OK\r
                Content-Length: %d\r
                Connection: close\r
                \r
                %s""".formatted(body.length(), body).getBytes());
    }

    private static void handleUnknownPath(Socket clientSocket, String requestTarget) throws IOException {
        var body = "Unknown path " + requestTarget;
        clientSocket.getOutputStream().write("""
                HTTP/1.1 404 Not found\r
                Content-Length: %d\r
                Connection: close\r
                \r
                %s""".formatted(body.length(), body).getBytes());
    }

    private static String readLine(Socket clientSocket) throws IOException {
        var line = new StringBuilder();
        int c;
        while ((c = clientSocket.getInputStream().read()) != '\n') {
            line.append((char) c);
        }

        return line.toString();
    }

    public URL getURL() throws MalformedURLException {
        return new URL("http", "localhost", serverSocket.getLocalPort(), "/");
    }
}
