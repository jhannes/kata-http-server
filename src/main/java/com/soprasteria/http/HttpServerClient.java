package com.soprasteria.http;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpServerClient {
    private final Socket clientSocket;
    private final Path httpRoot;
    private final Map<String, String> headers = new LinkedHashMap<>();
    private String requestLine;

    public HttpServerClient(Socket clientSocket, Path httpRoot) {
        this.clientSocket = clientSocket;
        this.httpRoot = httpRoot;
    }

    void handleClient() throws IOException {
        requestLine = readLine();
        readHeaders();
        var requestTarget = requestLine.split(" ")[1];

        var requestFile = resolveRequestTarget(requestTarget);
        if (requestTarget.equals("/api/login")) {
            handleLoginRequest();
        } else if (Files.exists(requestFile)) {
            handleExistingFile(requestFile);
        } else {
            handleNotFound(requestTarget);
        }
    }

    private void readHeaders() throws IOException {
        String headerLine;
        while (!(headerLine = readLine().trim()).isEmpty()) {
            var parts = headerLine.split(":\\s*");
            headers.put(parts[0], parts[1]);
        }
    }

    private void handleLoginRequest() throws IOException {
        var requestMethod = requestLine.split(" ")[0];
        if (requestMethod.equals("GET")) {
            handleGetLogin();
        } else {
            var sessionCookie = "session=username";
            clientSocket.getOutputStream().write("""
                    HTTP/1.1 200 OK\r
                    Connection: close\r
                    Set-Cookie: %s\r
                    \r
                    """.formatted(sessionCookie).getBytes());
        }
    }

    private void handleGetLogin() throws IOException {
        var cookie = headers.get("Cookie");
        if (cookie != null) {
            var parts = cookie.split("=");
            var body = "Logged in as " + parts[1];
            clientSocket.getOutputStream().write("""
                    HTTP/1.1 200 OK\r
                    Content-Length: %d\r
                    Connection: close\r
                    \r
                    %s""".formatted(body.length(), body).getBytes());
        } else {
            var body = "Unauthorized user";
            clientSocket.getOutputStream().write("""
                    HTTP/1.1 401 Unauthorized\r
                    Content-Length: %d\r
                    Connection: close\r
                    \r
                    %s""".formatted(body.length(), body).getBytes());
        }
    }

    private void handleNotFound(String requestTarget) throws IOException {
        var body = "Unknown path " + requestTarget;
        clientSocket.getOutputStream().write("""
                HTTP/1.1 404 Not found\r
                Content-Length: %d\r
                Connection: close\r
                Content-type: text/html\r
                \r
                %s""".formatted(body.length(), body).getBytes());
    }

    private void handleExistingFile(Path requestFile) throws IOException {
        var body = Files.readString(requestFile);
        clientSocket.getOutputStream().write("""
                HTTP/1.1 200 OK\r
                Content-Length: %d\r
                Connection: close\r
                \r
                %s""".formatted(body.length(), body).getBytes());
    }

    private Path resolveRequestTarget(String requestTarget) {
        return httpRoot.resolve(requestTarget.substring(1));
    }

    private String readLine() throws IOException {
        var line = new StringBuilder();
        int c;
        while ((c = clientSocket.getInputStream().read()) != -1) {
            if (c == '\n') {
                break;
            }
            line.append((char) c);
        }
        return line.toString();
    }
}
