package com.soprasteria.http;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

public class HttpServer {

    private final ServerSocket serverSocket;
    private final Path rootDir;

    public HttpServer(int port, Path rootDir) throws IOException, GeneralSecurityException {
        var keyStore = KeyStore.getInstance("pkcs12");
        try (var certFile = new FileInputStream("servercert.p12")) {
            keyStore.load(certFile, "abc123".toCharArray());
        }
        var kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, "abc123".toCharArray());
        var sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);
        this.serverSocket = sslContext.getServerSocketFactory().createServerSocket(port);
        this.rootDir = rootDir;

        new Thread(this::handleClients).start();
    }

    private void handleClients() {
        try {
            while (!Thread.interrupted()) {
                handleClient(serverSocket.accept());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleClient(Socket clientSocket) throws IOException {
        var requestLine = readLine(clientSocket);
        System.out.println(requestLine);

        var requestMethod = requestLine.split(" ")[0];
        var requestTarget = requestLine.split(" ")[1];

        var file = rootDir.resolve(requestTarget.substring(1));
        if (requestTarget.equals("/api/hallo")) {
            var body = "Hællæ værden";
            clientSocket.getOutputStream().write("""
                        HTTP/1.1 200 OK\r
                        Connection: close\r
                        Content-type: text/plain; charset=utf-8\r
                        Content-Length: %d\r
                        \r
                        %s""".formatted(body.getBytes().length, body).getBytes());
        } else if (requestTarget.equals("/api/login")) {
            if (requestMethod.equals("POST")) {
                var requestHeaders = readHeaders(clientSocket);
                var body = readBytes(
                        Integer.parseInt(requestHeaders.get("Content-Length")),
                        clientSocket
                );
                var parameters = parseQuery(body);
                var username = parameters.get("username");

                clientSocket.getOutputStream().write("""
                        HTTP/1.1 302 Found\r
                        Connection: close\r
                        Location: http://localhost:8080/index.html\r
                        Set-Cookie: session=%s\r
                        Content-Length: 0\r
                        \r
                        """.formatted(username).getBytes());
            } else {
                var requestHeaders = readHeaders(clientSocket);

                var cookie = requestHeaders.get("Cookie");
                if (cookie != null) {
                    var cookieParts = cookie.split("=");
                    var body = "Logged in as " + cookieParts[1];
                    clientSocket.getOutputStream().write("""
                        HTTP/1.1 200 OK\r
                        Connection: close\r
                        Content-Length: %d\r
                        \r
                        %s""".formatted(body.length(), body).getBytes());
                } else {
                    var body = "Please log in";
                    clientSocket.getOutputStream().write("""
                        HTTP/1.1 401 Unauthorized\r
                        Connection: close\r
                        Content-Length: %d\r
                        \r
                        %s""".formatted(body.length(), body).getBytes());
                }
            }
        } else if (Files.exists(file)) {
            clientSocket.getOutputStream().write("""
                    HTTP/1.1 200 OK\r
                    Connection: close\r
                    Content-Length: %d\r
                    \r
                    """.formatted(Files.size(file)).getBytes());
            try (var fileInputStream = new FileInputStream(file.toFile())) {
                fileInputStream.transferTo(clientSocket.getOutputStream());
            }
        } else {
            var body = "Not found " + requestTarget;
            clientSocket.getOutputStream().write("""
                    HTTP/1.1 404 Not found\r
                    Connection: close\r
                    Content-Length: %d\r
                    \r
                    %s""".formatted(body.length(), body).getBytes());
        }
    }

    private Map<String, String> parseQuery(String query) {
        var parameters = new HashMap<String, String>();
        for (String parameter : query.split("&")) {
            var parts = parameter.split("=");
            parameters.put(parts[0], parts[1]);
        }
        return parameters;
    }

    private String readBytes(int contentLength, Socket socket) throws IOException {
        var requestLine = new StringBuilder();
        for (int i = 0; i < contentLength; i++) {
            requestLine.append((char) socket.getInputStream().read());
        }
        return requestLine.toString();
    }

    private Map<String, String> readHeaders(Socket socket) throws IOException {
        var headers = new HashMap<String, String>();
        String line;
        while (!(line = readLine(socket).trim()).isEmpty()) {
            var parts = line.split(":\\s*");
            headers.put(parts[0], parts[1]);
        }
        return headers;
    }

    private static String readLine(Socket clientSocket) throws IOException {
        var requestLine = new StringBuilder();
        int c;
        while ((c = clientSocket.getInputStream().read()) != '\n') {
            requestLine.append((char) c);
        }
        return requestLine.toString();
    }

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        var serverSocket = new HttpServer(8443, Path.of("src", "main", "resources"));
    }

    public URL getURL() throws MalformedURLException {
        return new URL("https", "localhost", serverSocket.getLocalPort(), "/");
    }
}
