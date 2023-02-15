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

    private final HttpServer server;
    private final Path tempDir;

    public HttpServerTest() throws IOException {
        tempDir = Path.of("target", "test-files", "http-root-" + System.currentTimeMillis());
        server = new HttpServer(0, tempDir);
        Files.createDirectories(tempDir);
    }

    @Test
    void shouldReturn404ForUnknownPaths() throws IOException {
        var path = "/unknown-path-" + System.currentTimeMillis();
        var connection = openConnection(path);
        assertEquals(404, connection.getResponseCode());
        assertEquals("Unknown path " + path, asString(connection.getErrorStream()));
    }

    @Test
    void shouldReturnExistingFile() throws IOException {
        var fileContent = "Hello world " + LocalTime.now();
        Files.writeString(tempDir.resolve("plain.txt"), fileContent);
        var connection = openConnection("plain.txt");
        assertEquals(200, connection.getResponseCode());
        assertEquals(fileContent, asString(connection.getInputStream()));
    }

    @Test
    void shouldSupportMultipleRequests() throws IOException {
        assertEquals(404, openConnection("/hei").getResponseCode());
        assertEquals(404, openConnection("/hallo").getResponseCode());
    }

    @Test
    void shouldReturn401ForUnauthorizedUser() throws IOException {
        assertEquals(401, openConnection("/api/login").getResponseCode());
    }

    @Test
    void shouldReturnUsernameForAuthorizedUsers() throws IOException {
        var connection = openConnection("/api/login");
        connection.setRequestProperty("Cookie", "session=Johannes+Brodwall");
        assertEquals(200, connection.getResponseCode());
        assertEquals("Logged in as Johannes+Brodwall", asString(connection.getInputStream()));
    }

    @Test
    void shouldSetCookieWhenUserLogsIn() throws IOException {
        var connection = openConnection("/api/login");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.getOutputStream().write("username=Welcome+to+DevMeetup".getBytes());

        assertEquals(200, connection.getResponseCode());
        assertEquals(
                "session=Welcome+to+DevMeetup",
                connection.getHeaderField("Set-Cookie")
        );
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