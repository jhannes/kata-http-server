package com.soprasteria.http;

import java.io.IOException;
import java.net.ServerSocket;

public class HttpServer {

    public static void main(String[] args) throws IOException {
        var serverSocket = new ServerSocket(8080);
        var clientSocket = serverSocket.accept();

        int c;
        while((c = clientSocket.getInputStream().read()) != -1) {
            System.out.print((char)c);
        }
    }
}
