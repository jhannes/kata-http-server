package no.soprasteria.http;

import java.io.IOException;
import java.net.ServerSocket;

public class HttpServer {

    public static void main(String[] args) throws IOException {
        var socket = new ServerSocket(8080);

        var clientSocket = socket.accept();
        int c;
        while ((c = clientSocket.getInputStream().read()) != -1) {
            System.out.println(c);
        }
    }
}
