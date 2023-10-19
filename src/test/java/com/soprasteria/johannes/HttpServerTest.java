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

    private final Path contentRoot = Path.of("target", "test", String.valueOf(System.currentTimeMillis()));
    private final HttpServer server = new HttpServer(0, contentRoot);

    HttpServerTest() throws IOException {
        Files.createDirectories(contentRoot);
        server.start();
    }

    @Test
    void shouldRespondWith404ToUnknownRequests() throws IOException {
        var path = "/unknown/path/" + System.currentTimeMillis();
        var conn = openConnection(path);
        assertEquals(404, conn.getResponseCode());
        assertEquals("Not found " + path, readOutput(conn.getErrorStream()));
    }

    @Test
    void shouldReturnFile() throws IOException {
        var testFile = "index.txt";
        String content = "File Content " + System.currentTimeMillis();
        Files.writeString(contentRoot.resolve(testFile), content);

        var conn = openConnection(testFile);
        assertEquals(200, conn.getResponseCode());
        assertEquals(content, readOutput(conn.getInputStream()));
    }

    @Test
    void shouldSupportMultipleRequests() throws IOException {
        assertEquals(404, openConnection("/test").getResponseCode());
        assertEquals(404, openConnection("/test").getResponseCode());
    }

    @Test
    void shouldReturn401WhenNotLoggedIn() throws IOException {
        var conn = openConnection("/api/login");
        assertEquals(401, conn.getResponseCode());
    }

    @Test
    void shouldShowUsernameFromCookie() throws IOException {
        var conn = openConnection("/api/login");
        var username = "Johannes-" + System.currentTimeMillis();
        conn.setRequestProperty("Cookie", "session=" + username);
        assertEquals(200, conn.getResponseCode());
        assertEquals("Welcome " + username, readOutput(conn.getInputStream()));
    }

    @Test
    void shouldSetCookieOnLogin() throws IOException {
        var conn = openConnection("/api/login");
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(false);
        var username = "Test-" + System.currentTimeMillis();
        conn.getOutputStream().write(("username=" + username).getBytes());

        assertEquals(200, conn.getResponseCode());
        var setCookie = conn.getHeaderField("Set-Cookie");
        assertEquals("session=" + username, setCookie);
    }


    private static String readOutput(InputStream stream) throws IOException {
        var buffer = new ByteArrayOutputStream();
        stream.transferTo(buffer);
        return buffer.toString();
    }

    private HttpURLConnection openConnection(String path) throws IOException {
        return (HttpURLConnection) new URL(server.getURL(), path).openConnection();
    }

}