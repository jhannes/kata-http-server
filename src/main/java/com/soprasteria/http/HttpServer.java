package com.soprasteria.http;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URL;

public class HttpServer {

    private final ServerSocket serverSocket;

    HttpServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);

        new Thread(this::runServer).start();
    }

    private void runServer() {
        try {
            var clientSocket = serverSocket.accept();

            var body = "Unknown path /unknown-path";
            clientSocket.getOutputStream().write("""
                    HTTP/1.1 404 Not found\r
                    Content-Length: %d\r
                    Connection: close\r
                    Content-type: text/html\r
                    \r
                    %s""".formatted(body.length(), body).getBytes());

            int c;
            while((c = clientSocket.getInputStream().read()) != -1) {
                System.out.print((char)c);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public URL getURL() throws MalformedURLException {
        return new URL("http", "localhost", serverSocket.getLocalPort(), "/");
    }

    public static void main(String[] args) throws IOException {
        new HttpServer(8080);
    }
}
