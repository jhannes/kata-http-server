package no.soprasteria.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HttpServerTest {

    private HttpServer server;

    @TempDir
    private Path dir;

    @BeforeEach
    void setUp() throws IOException {
        server = new HttpServer(0, dir);
        server.start();
    }

    @Test
    void shouldReturn404ForUnknownTarget() throws IOException {
        var connection = openConnection("/no/such/file");
        assertEquals(404, connection.getResponseCode());
        assertEquals("Not found /no/such/file", asString(connection.getErrorStream()));
    }

    @Test
    void shouldHandleMultipleRequests() throws IOException {
        assertEquals(404, openConnection("/no/such/file").getResponseCode());
        assertEquals(404, openConnection("/no/such/file").getResponseCode());
    }

    @Test
    void shouldReturn200ForKnownFile() throws IOException {
        var filename = "test.txt";
        var content = LocalDateTime.now().toString();
        Files.writeString(dir.resolve(filename), content);

        var connection = openConnection("/" + filename);
        assertEquals(200, connection.getResponseCode());
        assertEquals(content, asString(connection.getInputStream()));
    }

    @Test
    void shouldServeWelcomeFile() throws IOException {
        var content = LocalDateTime.now().toString();
        Files.writeString(dir.resolve("index.html"), content);
        var connection = openConnection("/");
        assertEquals(200, connection.getResponseCode());
        assertEquals(content, asString(connection.getInputStream()));
    }

    private static String asString(InputStream inputStream) throws IOException {
        var responseBuffer = new ByteArrayOutputStream();
        inputStream.transferTo(responseBuffer);
        return responseBuffer.toString();
    }

    private HttpURLConnection openConnection(String path) throws IOException {
        return (HttpURLConnection) new URL(server.getURL(), path).openConnection();
    }
}