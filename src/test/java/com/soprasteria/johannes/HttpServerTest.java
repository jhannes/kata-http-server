package com.soprasteria.johannes;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
class HttpServerTest {

    private final HttpServer server;
    private final Path contentRoot;

    public HttpServerTest() throws IOException {
        contentRoot = Path.of("target", "test", String.valueOf(System.currentTimeMillis()));
        Files.createDirectories(contentRoot);
        server = new HttpServer(0, contentRoot);
        server.start();
    }

    @Test
    void itRespondWith404ForMissingPaths() throws IOException {
        var path = "/missing/link.txt-" + System.currentTimeMillis();
        var connection = openConnection(path);
        assertEquals(404, connection.getResponseCode());
        assertEquals("File not found " + path, readStream(connection.getErrorStream()));
    }

    @Test
    void itRespond200ForExistingFile() throws IOException {
        var path = "index.txt";

        var fileContent = "Hello Fredrikstad";
        Files.writeString(contentRoot.resolve(path), fileContent);
        var connection = openConnection(path);
        assertEquals(200, connection.getResponseCode());
        assertEquals(fileContent, readStream(connection.getInputStream()));
    }

    @Test
    void itShouldRespondToMoreThanOneRequest() throws IOException {
        assertEquals(404, openConnection("/index.html").getResponseCode());
        assertEquals(404, openConnection("/index.html").getResponseCode());
    }

    @Test
    void itReturns401ForUnauthenticatedUsers() throws IOException {
        assertEquals(401, openConnection("/api/login").getResponseCode());
    }


    @Test
    void itReturnsProfilePageForLoggedInUsers() throws IOException {
        var connection = openConnection("/api/login");
        connection.setRequestProperty("Cookie", "session=Live+in+Fredrikstad");
        assertEquals(200, connection.getResponseCode());
        assertEquals("Welcome! " + "Live+in+Fredrikstad", readStream(connection.getInputStream()));
    }

    @Test
    void itSetsCookieOnLogin() throws IOException {
        var connection = openConnection("/api/login");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.getOutputStream().write("username=hello+world".getBytes());

        assertEquals(200, connection.getResponseCode());
        assertEquals("session=hello+world", connection.getHeaderField("Set-Cookie"));
    }


    private static String readStream(InputStream stream) throws IOException {
        var buffer = new ByteArrayOutputStream();
        stream.transferTo(buffer);
        return buffer.toString();
    }

    private HttpURLConnection openConnection(String path) throws IOException {
        return (HttpURLConnection) new URL(server.getURL(), path).openConnection();
    }

}