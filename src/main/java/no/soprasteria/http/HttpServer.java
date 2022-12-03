package no.soprasteria.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("resource")
public class HttpServer {

    public static void main(String[] args) throws IOException {
        var socket = new ServerSocket(8080);

        var clientSocket = socket.accept();

        var body = "hallæ værden!";
        var response = "HTTP/1.1 200 OK\r\n" +
                       "Connection: close\r\n" +
                       "Content-Type: text/html; charset=utf-8\r\n" +
                       "Content-Length: " + body.length() + "\r\n" +
                       "\r\n" +
                       body;

        clientSocket.getOutputStream().write(response.getBytes(StandardCharsets.UTF_8));

        int c;
        while ((c = clientSocket.getInputStream().read()) != -1) {
            System.out.print((char)c);
        }
    }
}
