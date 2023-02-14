package com.soprasteria.http;

import java.io.IOException;
import java.net.ServerSocket;

public class HttpServer {

    private final ServerSocket serverSocket;

    HttpServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);

        var clientSocket = serverSocket.accept();

        var body = "Hello there";
        clientSocket.getOutputStream().write("""
                HTTP/1.1 200 OK\r
                Content-Length: %d\r
                Connection: close\r
                Content-type: text/html\r
                \r
                %s""".formatted(body.length(), body).getBytes());

        int c;
        while((c = clientSocket.getInputStream().read()) != -1) {
            System.out.print((char)c);
        }
    }

    public static void main(String[] args) throws IOException {
        new HttpServer(8080);
    }
}
