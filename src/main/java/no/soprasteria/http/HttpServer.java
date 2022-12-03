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

        int c;
        while ((c = clientSocket.getInputStream().read()) != -1) {
            System.out.print((char)c);
        }
    }
}
