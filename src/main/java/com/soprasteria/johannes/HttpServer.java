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
                handleServer();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private void handleServer() throws IOException {
        while (true) {
            var clientSocket = serverSocket.accept();
            handleClient(clientSocket);
        }
    }

    private void handleClient(Socket clientSocket) throws IOException {
        var requestLine = readLine(clientSocket);
        var requestLineParts = requestLine.split(" ", 3);
        var requestTarget = requestLineParts[1];
        var requestMethod = requestLineParts[0];

        var requestPath = contentRoot.resolve(requestTarget.substring(1));

        System.out.println(requestTarget);

        if (requestTarget.startsWith("/api/login")) {
            var headers = new HashMap<String, String>();
            String headerLine;
            while (!(headerLine = readLine(clientSocket)).isBlank()) {
                var splittedLine = headerLine.split(":\\s*", 2);
                headers.put(splittedLine[0], splittedLine[1].trim());
            }

            if (requestMethod.equals("POST")) {
                var requestBody = new StringBuilder();

                var contentLength = Integer.parseInt(headers.get("Content-Length"));
                for (int i = 0; i < contentLength; i++) {
                    requestBody.append((char)clientSocket.getInputStream().read());
                }

                var formParameterParts = requestBody.toString().split("=");


                var message = "You are now logged in!";

                var username = formParameterParts[1];
                var cookie = "session=" + username;

                clientSocket.getOutputStream().write("""
                HTTP/1.1 200 OK\r
                Connection: close\r
                Set-Cookie: %s\r
                Content-Length: %d\r
                
                %s""".formatted(cookie, message.length(), message).getBytes());
                return;
            }




            if (headers.containsKey("Cookie")) {
                var cookie = headers.get("Cookie");
                var cookieParts = cookie.split("=", 2);
                var username = cookieParts[1];
                var message = "Welcome! " + username;
                clientSocket.getOutputStream().write("""
                HTTP/1.1 200 OK\r
                Connection: close\r
                Content-Length: %d\r
                
                %s""".formatted(message.length(), message).getBytes());
                return;
            }


            var message = "You are not logged in";
            clientSocket.getOutputStream().write("""
                HTTP/1.1 401 Unauthenticated\r
                Connection: close\r
                Content-Length: %d\r
                
                %s""".formatted(message.length(), message).getBytes());
            return;
        }


        if (Files.exists(requestPath)) {
            var message = Files.readString(requestPath);
            clientSocket.getOutputStream().write("""
                HTTP/1.1 200 OK\r
                Connection: close\r
                Content-Length: %d\r
                
                %s""".formatted(message.length(), message).getBytes());
            return;
        }

        var message = "File not found " + requestTarget;
        clientSocket.getOutputStream().write("""
                HTTP/1.1 404 Not Found\r
                Connection: close\r
                Content-Length: %d\r
                
                %s""".formatted(message.length(), message).getBytes());


    }

    private static String readLine(Socket clientSocket) throws IOException {
        var requestLine = new StringBuilder();
        int c;
        while ((c = clientSocket.getInputStream().read()) != '\n') {
            requestLine.append((char)c);
        }
        return requestLine.toString();
    }

    public URL getURL() throws MalformedURLException {
        return new URL("http", "localhost", serverSocket.getLocalPort(), "/");
    }
}
