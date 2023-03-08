package no.politiet.http;

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
    private final Path httpRoot;

    public HttpServer(int port, Path httpRoot) throws IOException {
        serverSocket = new ServerSocket(port);
        this.httpRoot = httpRoot;
        new Thread(this::handleServerSocket).start();
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

        if (requestTarget.equals("/api/login")) {

            if (requestMethod.equals("POST")) {
                var headers = readHttpHeaders(clientSocket);

                var contentLength = Integer.parseInt(headers.get("Content-Length"));

                var requestBody = new StringBuilder();
                for (int i = 0; i < contentLength; i++) {
                     requestBody.append((char) clientSocket.getInputStream().read());
                }

                var username = requestBody.toString().split("=")[1];

                var cookie = "session=" + username;

                var body = "You are now logged in";
                clientSocket.getOutputStream().write("""
                        HTTP/1.1 200 OK\r
                        Set-Cookie: %s\r
                        Connection: close\r
                        Content-Length: %d\r
                        \r
                        %s""".formatted(cookie, body.length(), body).getBytes());
                return;
            }

            var headers = readHttpHeaders(clientSocket);

            if (headers.containsKey("Cookie")) {
                var cookieHeader = headers.get("Cookie");
                var cookieValue = cookieHeader.split("=")[1];
                var body = "Username is " + cookieValue;
                clientSocket.getOutputStream().write("""
                        HTTP/1.1 200 OK\r
                        Content-Length: %d\r
                        Connection: close\r
                        \r
                        %s""".formatted(body.length(), body).getBytes());
                return;
            }


            var body = "You are not logged in";
            clientSocket.getOutputStream().write("""
                    HTTP/1.1 401 Unauthorized\r
                    Content-Length: %d\r
                    Connection: close\r
                    \r
                    %s""".formatted(body.length(), body).getBytes());
            return;
        }


        var requestedPath = httpRoot.resolve(requestTarget.substring(1));
        if (Files.exists(requestedPath)) {
            var body = Files.readString(requestedPath);
            clientSocket.getOutputStream().write("""
                    HTTP/1.1 200 OK\r
                    Content-Length: %d\r
                    Connection: close\r
                    \r
                    %s""".formatted(body.length(), body).getBytes());
            return;
        }

        var body = "Not found " + requestTarget;
        clientSocket.getOutputStream().write("""
                HTTP/1.1 404 Not Found\r
                Content-Length: %d\r
                Connection: close\r
                Content-Type: text/plain\r
                \r
                %s""".formatted(body.length(), body).getBytes());
    }

    private static HashMap<String, String> readHttpHeaders(Socket clientSocket) throws IOException {
        var headers = new HashMap<String, String>();
        String headerLine;
        while (!(headerLine = readLine(clientSocket).trim()).isEmpty()) {
            var headerParts = headerLine.split(": *");
            headers.put(headerParts[0], headerParts[1]);
        }
        return headers;
    }

    private static String readLine(Socket clientSocket) throws IOException {
        var requestLine = new StringBuilder();
        int read;
        while ((read = clientSocket.getInputStream().read()) != '\n') {
            requestLine.append((char) read);
        }
        return requestLine.toString();
    }

    public static void main(String[] args) throws IOException {
        new HttpServer(8080, Path.of("src", "main", "resources"));
    }

    public URL getBaseUrl() throws MalformedURLException {
        return new URL("http", "localhost", serverSocket.getLocalPort(), "/");
    }
}
