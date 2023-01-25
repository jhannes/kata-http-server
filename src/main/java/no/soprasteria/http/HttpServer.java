package no.soprasteria.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class HttpServer {

    private final ServerSocket socket;
    private final Path baseDir;

    public HttpServer(int port, Path baseDir) throws IOException {
        socket = new ServerSocket(port);
        this.baseDir = baseDir;
    }

    public static void main(String[] args) throws IOException {
        new HttpServer(8080, Path.of("src", "main", "resources")).startServer();
    }

    void startServer() {
        new Thread(() -> {
            try {
                while (!Thread.interrupted()) {
                    startServerSocket();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void startServerSocket() throws IOException {
        var clientSocket = socket.accept();

        var requestLine = readLine(clientSocket);
        var requestLineParts = requestLine.split(" ");

        var requestMethod = requestLineParts[0];
        var requestTarget = requestLineParts[1];

        if (requestTarget.equals("/api/login")) {
            if (requestMethod.equals("POST")) {
                respondToLoginPost(clientSocket);
            } else {
                var requestHeaders = readHeaders(clientSocket);
                var body = requestHeaders.get("Cookie");
                clientSocket.getOutputStream().write("""
                        HTTP/1.1 200 OK\r
                        Content-Length: %d\r
                        Connection: close\r
                        \r
                        %s""".formatted(body.length(), body).getBytes());
            }
            return;
        }

        var requestPath = baseDir.resolve(requestTarget.substring(1));
        if (Files.isDirectory(requestPath)) {
            requestPath = requestPath.resolve("index.html");
        }
        if (Files.isRegularFile(requestPath)) {
            respondWithFile(clientSocket, requestPath);
            return;
        }

        respondWith404(clientSocket, requestTarget);
    }

    private void respondToLoginPost(Socket clientSocket) throws IOException {
        var requestHeaders = readHeaders(clientSocket);

        var host = requestHeaders.get("Host");
        var baseUrl = "http://" + host + "/";

        var contentLength = Integer.parseInt(requestHeaders.get("Content-Length"));
        var queryString = readCharacters(clientSocket.getInputStream(), contentLength);
        var formParameters = new HashMap<>();
        for (String queryParameter : queryString.split("&")) {
            var parts = queryParameter.split("=", 2);
            formParameters.put(parts[0], parts[1]);
        }

        var username = formParameters.get("username");
        clientSocket.getOutputStream().write("""
                HTTP/1.1 302 Moved\r
                Location: %s\r
                Set-Cookie: user=%s\r
                \r
                """.formatted(baseUrl, username).getBytes());
    }

    private static HashMap<String, String> readHeaders(Socket clientSocket) throws IOException {
        var requestHeaders = new HashMap<String, String>();
        String headerLine;
        while (!(headerLine = readLine(clientSocket).trim()).isBlank()) {
            var headerParts = headerLine.split(":\\s*", 2);
            requestHeaders.put(headerParts[0], headerParts[1]);
        }
        return requestHeaders;
    }

    private String readCharacters(InputStream inputStream, int contentLength) throws IOException {
        var buffer = new StringBuilder();
        for (int i = 0; i < contentLength; i++) {
            buffer.append((char) inputStream.read());
        }
        return buffer.toString();
    }

    private static String readLine(Socket accept) throws IOException {
        var requestLine = new StringBuilder();
        int c;
        while ((c = accept.getInputStream().read()) != '\n') {
            requestLine.append((char)c);
        }
        return requestLine.toString();
    }

    private static void respondWith404(Socket accept, String requestTarget) throws IOException {
        var body = "Not found " + requestTarget;
        accept.getOutputStream().write("""
                HTTP/1.1 404 Not Found\r
                Content-Length: %d\r
                Connection: close\r
                \r
                %s""".formatted(body.length(), body).getBytes());
    }

    private static void respondWithFile(Socket accept, Path requestPath) throws IOException {
        var body = Files.readString(requestPath);
        accept.getOutputStream().write("""
            HTTP/1.1 200 OK\r
            Content-Length: %d\r
            Connection: close\r
            \r
            %s""".formatted(body.length(), body).getBytes());
    }

    public URL getBaseURL() throws MalformedURLException {
        return new URL("http", "localhost", socket.getLocalPort(), "/");
    }
}
