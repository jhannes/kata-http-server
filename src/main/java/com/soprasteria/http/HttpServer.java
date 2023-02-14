package com.soprasteria.http;

import com.soprasteria.http.handlers.FileRequestHandler;
import com.soprasteria.http.handlers.HelloRequestHandler;
import com.soprasteria.http.handlers.LoginRequestHandler;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Map;

public class HttpServer {

    private final ServerSocket serverSocket;
    private final Map<String, RequestHandler> requestHandlers = Map.of(
            "/api/hello", new HelloRequestHandler(),
            "/api/login", new LoginRequestHandler()
    );
    private final RequestHandler defaultHandler;

    public HttpServer(Path httpRoot, ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.defaultHandler = new FileRequestHandler(httpRoot);
        new Thread(this::runServer).start();
    }

    private void runServer() {
        try {
            while (!Thread.interrupted()) {
                var clientSocket = serverSocket.accept();
                HttpServerRequest request;
                HttpServerResponse response;
                try {
                    request = new HttpServerRequest(clientSocket.getInputStream(), isSecure());
                    response = new HttpServerResponse(clientSocket.getOutputStream());
                } catch (IOException e) {
                    System.err.println("Failed read request");
                    e.printStackTrace();
                    continue;
                }
                handleRequest(request, response);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleRequest(HttpServerRequest request, HttpServerResponse response) {
        try {
            requestHandlers.getOrDefault(request.getTarget(), defaultHandler)
                    .handle(request, response);
            System.out.println(response.getStatusCode() + " " + request.getRequest());
        } catch (IOException e) {
            System.out.println("Error while handling " + request.getRequest());
            e.printStackTrace();
        }
    }

    public URL getURL() throws MalformedURLException {
        return new URL(isSecure() ? "https" : "http", "localhost", serverSocket.getLocalPort(), "/");
    }

    private boolean isSecure() {
        return serverSocket instanceof SSLServerSocket;
    }

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        //var serverSocket = new ServerSocket(8080);

        // Created with `keytool.exe -genkeypair -storepass changeit -keyalg rsa -dname cn=localhost -ext san=dns:localhost -keystore serverKeyStore.p12`
        var sslContext = SSLContext.getInstance("TLS");
        var keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        var keyStore = KeyStore.getInstance("pkcs12");
        keyStore.load(Files.newInputStream(Path.of("serverKeyStore.p12")), "changeit".toCharArray());
        keyManagerFactory.init(keyStore, "changeit".toCharArray());
        sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
        var serverSocket = sslContext.getServerSocketFactory().createServerSocket(8443);

        new HttpServer(Path.of("src", "main", "resources"), serverSocket);
    }

}
