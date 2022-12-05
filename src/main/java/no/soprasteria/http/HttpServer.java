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

    void start() throws IOException {
        var clientSocket = serverSocket.accept();

        handleClient(clientSocket);
    }

    private static void handleClient(Socket clientSocket) throws IOException {
        var content = "Hello There";
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

    public URL getURL() throws MalformedURLException {
        return new URL("http", "localhost", serverSocket.getLocalPort(), "/");
    }
}
