package no.soprasteria.http;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("resource")
public class HttpServer {

    public static void main(String[] args) throws IOException {
        new HttpServer(8080).startServer();
    }

    private final ServerSocket socket;

    public HttpServer(int port) throws IOException {
        socket = new ServerSocket(port);
    }

    void startServer() throws IOException {
        while (!Thread.interrupted()) {
            var clientSocket = socket.accept();
            handleRequest(clientSocket);
        }
    }

    private static void handleRequest(Socket clientSocket) throws IOException {
        var body = "hallæ værden!";
        var contentLength = body.getBytes(StandardCharsets.UTF_8).length;
        var response = "HTTP/1.1 200 OK\r\n" +
                       "Connection: close\r\n" +
                       "Content-Type: text/html; charset=utf-8\r\n" +
                       "Transfer-Encoding: chunked\r\n" +
                       "\r\n" +
                       Integer.toHexString(contentLength) + "\r\n" +
                       body + "\r\n" +
                       0 + "\r\n\r\n";

        clientSocket.getOutputStream().write(response.getBytes(StandardCharsets.UTF_8));

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
