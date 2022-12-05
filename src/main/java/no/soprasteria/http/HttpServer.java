package no.soprasteria.http;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
        var requestLine = readLine(clientSocket.getInputStream()).split(" ");
        var requestTarget = requestLine[1];

        if (requestTarget.equals("/api/login")) {
            var requestHeaders = readRequestHeaders(clientSocket.getInputStream());
            handlePostLogin(clientSocket, requestHeaders);
            return;
        }


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

    private void handlePostLogin(Socket clientSocket, Map<String, String> requestHeaders) throws IOException {
        var requestBody = readBody(clientSocket.getInputStream(), Integer.parseInt(requestHeaders.get("Content-Length")));
        var queryParameters = new HashMap<>();
        for (String queryParam : requestBody.split("&")) {
            var parts = queryParam.split("=", 2);
            queryParameters.put(parts[0], parts[1]);
        }
        var username = queryParameters.get("username");

        var host = requestHeaders.get("Host");
        var redirectUrl = "http://" + host + "/";
        clientSocket.getOutputStream().write("""
            HTTP/1.1 302 MOVED\r
            Connection: close\r
            Location: %s\r
            Set-Cookie: user=%s
            \r""".formatted(redirectUrl, username).getBytes());
    }

    private String readBody(InputStream inputStream, int length) throws IOException {
        var body = new StringBuilder();
        for (int i = 0; i < length; i++) {
            body.append((char) inputStream.read());
        }
        return body.toString();
    }

    private static Map<String, String> readRequestHeaders(InputStream inputStream) throws IOException {
        var result = new HashMap<String, String>();
        String line;
        while (!(line = readLine(inputStream)).isEmpty()) {
            var parts = line.split(":\\s+");
            result.put(parts[0], parts[1]);
        }
        return result;
    }

    private static String readLine(InputStream inputStream) throws IOException {
        var line = new StringBuilder();
        int c;
        while ((c = inputStream.read()) != '\r') {
            if (c == -1) break;
            line.append((char) c);
        }
        if (c == '\r') inputStream.read();
        return line.toString();
    }

    public URL getURL() throws MalformedURLException {
        return new URL("http", "localhost", serverSocket.getLocalPort(), "/");
    }
}
