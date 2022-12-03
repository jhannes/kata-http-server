package no.soprasteria.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpServerTest {
    private static final Random random = new Random();
    private HttpServer server;
    private Path baseDir;

    @BeforeEach
    void setUp() throws IOException {
        baseDir = Path.of("target", "test-files", "http-" + random.nextInt());
        Files.createDirectories(baseDir);
        server = new HttpServer(0, baseDir);
        server.startServer();
    }

    @Test
    void shouldRespondWith404ToUnknownPages() throws IOException {
        var path = "/unknown-url-" + random.nextInt();
        var connection = getOpenConnection(path);
        assertEquals(404, connection.getResponseCode());
        assertEquals("Unknown file " + path, asString(connection.getErrorStream()));
    }

    @Test
    void shouldServeFile() throws IOException {
        var newFile = "new-file-" + random.nextInt();
        var content = LocalDateTime.now().toString();
        Files.writeString(baseDir.resolve(newFile), content);
        var connection = getOpenConnection("/" + newFile);
        assertEquals(200, connection.getResponseCode());
        assertEquals(content, asString(connection.getInputStream()));
    }

    @Test
    void shouldServeIndexHtml() throws IOException {
        var newDir = baseDir.resolve("dir-" + random.nextInt());
        Files.createDirectories(newDir);
        var content = LocalDateTime.now().toString();
        Files.writeString(newDir.resolve("index.html"), content);
        var connection = getOpenConnection("/" + newDir.getFileName());
        assertEquals(200, connection.getResponseCode());
        assertEquals(content, asString(connection.getInputStream()));
    }

    @Test
    void shouldResolveContentType() throws IOException {
        Files.writeString(baseDir.resolve("style.css"), "body { background: red; }");
        var connection = getOpenConnection("/style.css");
        assertEquals(200, connection.getResponseCode());
        assertEquals("text/css; charset=utf-8", connection.getHeaderField("Content-Type"));
    }

    @Test
    void shouldLogIn() throws IOException {
        var connection = getOpenConnection("/api/login");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(false);
        connection.getOutputStream().write("username=johannes".getBytes());
        assertEquals(302, connection.getResponseCode());
        assertEquals(server.getURL().toString(), connection.getHeaderField("Location"));
    }

    private static String asString(InputStream stream) throws IOException {
        var buffer = new ByteArrayOutputStream();
        stream.transferTo(buffer);
        return buffer.toString();
    }

    private HttpURLConnection getOpenConnection(String path) throws IOException {
        return (HttpURLConnection) new URL(server.getURL(), path).openConnection();
    }
}