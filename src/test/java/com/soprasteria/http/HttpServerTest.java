package com.soprasteria.http;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpServerTest {

    private final Path tempDir = Path.of("target", "test-files", "http-" + System.currentTimeMillis());
    private final HttpServer server;

    HttpServerTest() throws IOException {
        Files.createDirectories(tempDir);
        server = new HttpServer(0, tempDir);
    }

    @Test
    void shouldReturn404ForUnknownPath() throws IOException {
        var path = "/unknown-path-" + System.currentTimeMillis();
        var connection = openConnection(path);
        assertEquals(404, connection.getResponseCode());
        assertEquals("Unknown path " + path, asString(connection));
    }

    @Test
    void shouldReturn200ForFoundFile() throws IOException {
        var content = "Hello World " + LocalTime.now();
        Files.writeString(tempDir.resolve("plain.txt"), content);
        var connection = openConnection("plain.txt");
        assertEquals(200, connection.getResponseCode());
        assertEquals(content, asString(connection));
    }

    private static String asString(HttpURLConnection connection) throws IOException {
        var buffer = new ByteArrayOutputStream();
        connection.getErrorStream().transferTo(buffer);
        return buffer.toString();
    }

    private HttpURLConnection openConnection(String path) throws IOException {
        return (HttpURLConnection) new URL(server.getURL(), path).openConnection();
    }

}