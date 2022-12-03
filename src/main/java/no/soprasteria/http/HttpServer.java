package no.soprasteria.http;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HttpServer {

    public static void main(String[] args) throws IOException {
        new HttpServer(8080).startServer();
    }

    private final ServerSocket socket;

    public HttpServer(int port) throws IOException {
        socket = new ServerSocket(port);
    }

    void startServer() {
        new Thread(() -> {
            while (!Thread.interrupted()) {
                try (var clientSocket = socket.accept()) {
                    handleRequest(clientSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static void handleRequest(Socket clientSocket) throws IOException {
        var responseHeader = """
                HTTP/1.1 404 NOT FOUND\r
                Connection: close\r
                Transfer-Encoding: chunked\r
                Content-Type: text/html; charset=utf-8\r
                \r
                """;
        clientSocket.getOutputStream().write(responseHeader.getBytes(StandardCharsets.UTF_8));

        var body = "Unknown file /unknown-url";
        var contentLength = body.getBytes(StandardCharsets.UTF_8).length;
        clientSocket.getOutputStream().write((Integer.toHexString(contentLength) + "\r\n" +
                                              body + "\r\n" +
                                              0 + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));

        /*
        int c;
        while ((c = clientSocket.getInputStream().read()) != -1) {
            System.out.print((char)c);
        }

         */
    }

    public URL getURL() throws MalformedURLException {
        return new URL("http", "localhost", socket.getLocalPort(), "/");
    }
}
