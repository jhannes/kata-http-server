package no.javabin.kristiansand;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.stream.Collectors;

public class HttpServer {
    private final ServerSocket serverSocket;
    private Path contentRoot = Path.of("src", "main", "resources");

    public HttpServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        new Thread(this::handleClients).start();
    }

    private void handleClients() {
        try {
            while (!Thread.interrupted()) {
                var clientSocket = serverSocket.accept();

                var requestLine = readLine(clientSocket);
                System.out.println(requestLine);
                var requestMethod = requestLine.split(" ")[0];
                var requestTarget = requestLine.split(" ")[1];
                var requestTargetPath = contentRoot.resolve(requestTarget.substring(1));

                if (requestTarget.equals("/api/login")) {
                    if (requestMethod.equals("POST")) {
                        var headers = readHeaders(clientSocket);

                        var contentLength = Integer.parseInt(headers.get("Content-Length"));
                        var content = new StringBuilder();
                        for (int i = 0; i < contentLength; i++) {
                            content.append((char) clientSocket.getInputStream().read());
                        }
                        var username = content.toString().split("=")[1];

                        var cookie = "session=" + username;
                        var response = "Logged in";
                        clientSocket.getOutputStream().write("""
                                HTTP/1.1 200 OK\r
                                Server: javaBin\r
                                Set-Cookie: %s
                                Content-Length: %d\r
                                Connection: close\r
                                \r
                                %s""".formatted(cookie, response.length(), response).getBytes());
                    } else {
                        handleApiLogin(clientSocket);
                    }
                } else if (Files.exists(requestTargetPath)) {
                    respondWithFile(clientSocket, requestTargetPath);
                } else {
                    respondWithNotFound(clientSocket, requestTarget);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void handleApiLogin(Socket clientSocket) throws IOException {
        var headers = readHeaders(clientSocket);
        var cookie = headers.get("Cookie");
        if (cookie != null) {
            var username = cookie.split("=")[1];
            var response = "Hello " + username;
            clientSocket.getOutputStream().write("""
                    HTTP/1.1 200 OK\r
                    Server: javaBin\r
                    Content-Length: %d\r
                    Connection: close\r
                    \r
                    %s""".formatted(response.length(), response).getBytes());
        } else {
            var response = "Please log in";
            clientSocket.getOutputStream().write("""
                HTTP/1.1 401 Unauthorized\r
                Server: javaBin\r
                Content-Length: %d\r
                Connection: close\r
                \r
                %s""".formatted(response.length(), response).getBytes());
        }
    }

    private static HashMap<String, String> readHeaders(Socket clientSocket) throws IOException {
        var headers = new HashMap<String, String>();
        String headerLine;
        while (!(headerLine = readLine(clientSocket)).isBlank()) {
            var parts = headerLine.split(":\\s*");
            String key = parts[0], value = parts[1];
            headers.put(key, value);
        }
        return headers;
    }

    private static void respondWithFile(Socket clientSocket, Path requestTargetPath) throws IOException {
        if (Files.isDirectory(requestTargetPath)) {
            if (Files.isRegularFile(requestTargetPath.resolve("index.html"))) {
                requestTargetPath = requestTargetPath.resolve("index.html");
            } else {
                String listing;
                try (var paths = Files.list(requestTargetPath)) {
                    listing = paths.map(p -> "    <li><a href=\"" + p.getFileName() + "\">" + p.getFileName() + "</a></li>")
                            .collect(Collectors.joining("\n"));
                }
                var response = "<ul>\n" + listing + "\n</ul>\n";
                clientSocket.getOutputStream().write("""
                HTTP/1.1 200 OK\r
                Server: javaBin\r
                Content-Type: text/html\r
                Content-Length: %d\r
                Connection: close\r
                \r
                %s""".formatted(response.length(), response).getBytes());
                return;
            }
        }
        clientSocket.getOutputStream().write("""
                HTTP/1.1 200 OK\r
                Server: javaBin\r
                Content-Length: %d\r
                Connection: close\r
                \r
                """.formatted(Files.size(requestTargetPath)).getBytes());

        try (var inputStream = Files.newInputStream(requestTargetPath)) {
            inputStream.transferTo(clientSocket.getOutputStream());
        }
    }

    private static void respondWithNotFound(Socket clientSocket, String requestTarget) throws IOException {
        var response = "Not found " + requestTarget;
        clientSocket.getOutputStream().write("""
                HTTP/1.1 404 Not Found\r
                Server: javaBin\r
                Content-Length: %d\r
                Connection: close\r
                \r
                %s""".formatted(response.length(), response).getBytes());
    }

    private static String readLine(Socket clientSocket) throws IOException {
        var requestLine = new StringBuilder();
        int c;
        while ((c = clientSocket.getInputStream().read()) != '\n') {
            requestLine.append((char)c);
        }
        return requestLine.toString().trim();
    }

    public URL getURL() throws MalformedURLException {
        return new URL("http", "localhost", serverSocket.getLocalPort(), "/");
    }

    public void setContentRoot(Path contentRoot) {
        this.contentRoot = contentRoot;
    }

    public static void main(String[] args) throws IOException {
        new HttpServer(8080);
    }
}
