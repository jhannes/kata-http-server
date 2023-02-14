package com.soprasteria.http;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
        assertEquals("Unknown path " + path, asString(connection.getErrorStream()));
    }

    @Test
    void shouldReturn200ForFoundFile() throws IOException {
        var content = "Hello World " + LocalTime.now();
        Files.writeString(tempDir.resolve("plain.txt"), content);
        var connection = openConnection("plain.txt");
        assertEquals(200, connection.getResponseCode());
        assertEquals(content, asString(connection.getInputStream()));
    }

    @Test
    void shouldHandleMultipleRequests() throws IOException {
        assertEquals(404, openConnection("/some-path").getResponseCode());
        assertEquals(404, openConnection("/other-path").getResponseCode());
    }

    @Test
    void shouldReturn401ForUnauthorizedUsers() throws IOException {
        assertEquals(401, openConnection("/api/login").getResponseCode());
    }

    @Test
    void shouldReturnUsernameForAuthorizedUsers() throws IOException {
        var connection = openConnection("/api/login");
        var username = "johannes+brodwall+" + System.currentTimeMillis();
        connection.setRequestProperty("Cookie", "session=" + username);
        assertEquals(200, connection.getResponseCode());
        assertEquals("Logged in as " + username, asString(connection.getInputStream()));
    }

    @Test
    void shouldSetCookieOnLogin() throws IOException {
        var username = "Johannes+Brodwall";
        var connection = openConnection("/api/login");
        connection.setRequestMethod("POST");
        connection.setInstanceFollowRedirects(false);
        connection.setDoOutput(true);
        connection.getOutputStream().write(("username=" + username).getBytes());

        assertEquals(302, connection.getResponseCode());
        assertEquals("session=" + username, connection.getHeaderField("Set-Cookie"));
        assertEquals(server.getURL().toString(), connection.getHeaderField("Location"));
    }

    private static String asString(InputStream inputStream) throws IOException {
        var buffer = new ByteArrayOutputStream();
        inputStream.transferTo(buffer);
        return buffer.toString();
    }

    private HttpURLConnection openConnection(String path) throws IOException {
        return (HttpURLConnection) new URL(server.getURL(), path).openConnection();
    }

}