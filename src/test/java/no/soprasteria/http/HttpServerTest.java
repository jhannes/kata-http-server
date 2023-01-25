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

    @TempDir
    private Path baseDir;

    private HttpServer server;

    @BeforeEach
    void setUp() throws IOException {
        server = new HttpServer(0, baseDir);
        server.startServer();
    }

    @Test
    void shouldReturn404ForUnknownUrl() throws IOException {
        var connection = openConnection("/no-such-path");
        assertEquals(404, connection.getResponseCode());
        assertEquals("Not found /no-such-path", asString(connection.getErrorStream()));
    }

    @Test
    void shouldReturn200ForFileOnDisk() throws IOException {
        var content = LocalDateTime.now().toString();
        Files.writeString(baseDir.resolve("index.html"), content);
        var connection = openConnection("/index.html");
        assertEquals(200, connection.getResponseCode());
        assertEquals(content, asString(connection.getInputStream()));
        assertEquals(200, openConnection("/index.html").getResponseCode());
    }

    @Test
    void shouldReturnWelcomeFile() throws IOException {
        var content = LocalDateTime.now().toString();
        Files.writeString(baseDir.resolve("index.html"), content);
        var connection = openConnection("/");
        assertEquals(200, connection.getResponseCode());
    }

    @Test
    void shouldRedirectPostToApiLogin() throws IOException {
        var connection = openConnection("/api/login");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.getOutputStream().write("username=my+user+name".getBytes());
        connection.setInstanceFollowRedirects(false);
        assertEquals(302, connection.getResponseCode());
        assertEquals(server.getBaseURL().toString(), connection.getHeaderField("Location"));
        assertEquals("user=my+user+name", connection.getHeaderField("Set-Cookie"));
    }


    private static String asString(InputStream errorStream) throws IOException {
        var buffer = new ByteArrayOutputStream();
        errorStream.transferTo(buffer);
        return buffer.toString();
    }

    private HttpURLConnection openConnection(String path) throws IOException {
        return (HttpURLConnection) new URL(server.getBaseURL(), path).openConnection();
    }

}