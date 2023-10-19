package com.soprasteria.johannes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

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

        var requestPath = contentRoot.resolve(requestTarget.substring(1));
        System.out.println("Request path " + requestPath.toAbsolutePath());
        if (Files.isDirectory(requestPath)) {
            requestPath = requestPath.resolve("index.html");
        }

        if (requestTarget.equals("/api/login")) {
            handleApiLogin(requestMethod, clientSocket, headers);
        } else if (Files.exists(requestPath) && requestPath.normalize().startsWith(contentRoot)) {
            respondWithFile(clientSocket, requestPath);
        } else {
            respondWithNotFound(clientSocket, requestTarget);
        }
    }

    private static void handleApiLogin(String requestMethod, Socket clientSocket, HashMap<String, String> headers) throws IOException {
        var cookieHeader = headers.get("Cookie");
        if (requestMethod.equals("POST")) {
            handlePostApiLogin(clientSocket, headers);
        } else if (cookieHeader == null) {
            respondWithUnauthorized(clientSocket);
        } else {
            handleGetApiLogin(clientSocket, cookieHeader);
        }

    }

    private static void handleGetApiLogin(Socket clientSocket, String cookieHeader) throws IOException {
        var cookies = new HashMap<String, String>();
        for (var cookieString : cookieHeader.split(";\\s*")) {
            var cookieParts = cookieString.split("=", 2);
            cookies.put(cookieParts[0], cookieParts[1]);
        }
        var username = cookies.get("session").replace('+', ' ');
        var encodingPattern = Pattern.compile("%([A-Z0-9]{2})%([A-Z0-9]{2})");
        username = encodingPattern.matcher(username).replaceAll(match -> new String(new byte[] {
                (byte) Integer.parseInt(match.group(1), 0x10),
                (byte) Integer.parseInt(match.group(2), 0x10),
        }, UTF_8));
        var response = "Welcome " + username;
        clientSocket.getOutputStream().write(
                """
                HTTP/1.1 200 OK\r
                Content-Length: %d\r
                Connection: close\r
                \r
                %s""".formatted(response.length(), response).getBytes(ISO_8859_1)
        );
    }


    private static void handlePostApiLogin(Socket clientSocket, HashMap<String, String> headers) throws IOException {
        var contentLength = Integer.parseInt(headers.get("Content-Length"));

        var body = readBody(clientSocket, contentLength);
        var form = new HashMap<String, String>();
        for (var parameter : body.split("&")) {
            var parameterParts = parameter.split("=", 2);
            var paramName = parameterParts[0];
            var paramValue = parameterParts[1];
            form.put(paramName, paramValue);
        }

        var location = "http://" + headers.get("Host") + "/";
        clientSocket.getOutputStream().write(
                """
                HTTP/1.1 302 Found\r
                Connection: close\r
                Set-Cookie: session=%s\r
                Location: %s\r
                \r
                """.formatted(form.get("username"), location).getBytes()
        );
    }

    private static void respondWithFile(Socket clientSocket, Path requestPath) throws IOException {
        clientSocket.getOutputStream().write(
                """
                HTTP/1.1 200 OK\r
                Content-Length: %d\r
                Connection: close\r
                \r
                """.formatted(Files.size(requestPath)).getBytes()
        );
        try (var fileStream = Files.newInputStream(requestPath)) {
            fileStream.transferTo(clientSocket.getOutputStream());
        }
    }

    private static void respondWithUnauthorized(Socket clientSocket) throws IOException {
        var response = "Not logged in";
        clientSocket.getOutputStream().write(
                """
                HTTP/1.1 401 Unauthenticated\r
                Content-Length: %d\r
                Connection: close\r
                \r
                %s""".formatted(response.length(), response).getBytes()
        );
    }

    private static void respondWithNotFound(Socket clientSocket, String requestTarget) throws IOException {
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

    private static String readBody(Socket clientSocket, int contentLength) throws IOException {
        var requestBody = new StringBuilder();
        for (int i = 0; i < contentLength; i++) {
            requestBody.append((char) clientSocket.getInputStream().read());
        }
        return requestBody.toString();
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