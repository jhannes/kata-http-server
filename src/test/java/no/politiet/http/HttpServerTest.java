package no.politiet.http;

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

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpServerTest {

    private HttpServer httpServer;
    private Path httpRoot;

    @BeforeEach
    void setUp() throws IOException {
        httpRoot = Path.of("target", "test", "filefiles-" + System.currentTimeMillis());
        Files.createDirectories(httpRoot);
        httpServer = new HttpServer(0, httpRoot);
    }

    @Test
    void shouldReturn404ForNotFound() throws IOException {
        var path = "/non-existing-file-" + System.currentTimeMillis();
        var connection = openConnection(path);
        assertEquals(404, connection.getResponseCode());
        assertEquals("Not found " + path, readContent(connection.getErrorStream()));
    }

    @Test
    void shouldReturn200ForExistingFile() throws IOException {
        var content = "Here is some text created " + LocalDateTime.now();
        Files.writeString(httpRoot.resolve("index.html"), content);

        var connection = openConnection("/index.html");
        assertEquals(200, connection.getResponseCode());
        assertEquals(content, readContent(connection.getInputStream()));
    }

    @Test
    void shouldHandleMultipleRequests() throws IOException {
        var path = "/non-existing-file-" + System.currentTimeMillis();
        assertEquals(404, openConnection(path).getResponseCode());
        assertEquals(404, openConnection(path).getResponseCode());
    }

    @Test
    void shouldReturn401ForNotLoggedInUsers() throws IOException {
        assertEquals(401, openConnection("/api/login").getResponseCode());
    }

    @Test
    void shouldReturnUserNameForAuthorizedUsers() throws IOException {
        var connection = openConnection("/api/login");
        connection.setRequestProperty("Cookie", "session=Johannes+Was+Here");
        assertEquals(200, connection.getResponseCode());
        assertEquals("Username is Johannes+Was+Here", readContent(connection.getInputStream()));
    }

    @Test
    void shouldSetCookieOnLogin() throws IOException {
        var connection = openConnection("/api/login");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.getOutputStream().write("username=Johannes+was+here".getBytes());
        assertEquals(200, connection.getResponseCode());
        assertEquals("session=Johannes+was+here", connection.getHeaderField("Set-Cookie"));
    }


    private static String readContent(InputStream errorStream) throws IOException {
        var buffer = new ByteArrayOutputStream();
        errorStream.transferTo(buffer);
        return buffer.toString();
    }

    private HttpURLConnection openConnection(String path) throws IOException {
        var url = new URL(httpServer.getBaseUrl(), path);
        return (HttpURLConnection)url.openConnection();
    }

}
