package no.soprasteria.http;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

public class HttpServer {

    private final ServerSocket serverSocket;

    public HttpServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public static void main(String[] args) throws IOException {
        new HttpServer(8080).start();
    }

    void start() {
        new Thread(() -> {
            try (var clientSocket = serverSocket.accept()) {
                handleClient(clientSocket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void handleClient(Socket clientSocket) throws IOException {
        var line = new StringBuilder();
        int c;
        while ((c = clientSocket.getInputStream().read()) != '\r') {
            line.append((char) c);
        }
        var requestLine = line.toString().split(" ");
        var requestTarget = requestLine[1];

        var content = "Not found " + requestTarget;
        clientSocket.getOutputStream().write("""
                HTTP/1.1 404 NOT FOUND\r
                Content-Length: %d\r
                Connection: close\r
                \r
                %s""".formatted(content.length(), content).getBytes());
    }

    public URL getURL() throws MalformedURLException {
        return new URL("http", "localhost", serverSocket.getLocalPort(), "/");
    }
}
