package com.soprasteria.http;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.file.Path;

public class HttpServer {

    private final ServerSocket serverSocket;
    private final Path httpRoot;

    HttpServer(int port, Path httpRoot) throws IOException {
        serverSocket = new ServerSocket(port);
        this.httpRoot = httpRoot;

        new Thread(this::runServer).start();
    }

    private void runServer() {
        try {
            new HttpServerClient(serverSocket.accept(), httpRoot).handleClient();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public URL getURL() throws MalformedURLException {
        return new URL("http", "localhost", serverSocket.getLocalPort(), "/");
    }

    public static void main(String[] args) throws IOException {
        new HttpServer(8080, Path.of("src", "main", "resources"));
    }
}
