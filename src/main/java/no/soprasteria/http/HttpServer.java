package no.soprasteria.http;

import java.io.IOException;
import java.net.ServerSocket;

public class HttpServer {

    public static void main(String[] args) throws IOException {
        new HttpServer().start();
    }

    private void start() throws IOException {
        @SuppressWarnings("resource") var serverSocket = new ServerSocket(8080);

        var clientSocket = serverSocket.accept();

        var content = "Hello World";
        clientSocket.getOutputStream().write("""
                HTTP/1.1 200 OK\r
                Content-Length: %d\r
                Connection: close\r
                \r
                %s
                """.formatted(content.length(), content).getBytes());


        int c;
        while ((c = clientSocket.getInputStream().read()) != -1) {
            System.out.print((char)c);
        }
    }
}
