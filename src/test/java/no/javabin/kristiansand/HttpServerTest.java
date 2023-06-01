package no.javabin.kristiansand;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpServerTest {

    private final HttpServer server = new HttpServer(0);

    HttpServerTest() throws IOException {
    }

    @Test
    void shouldRespond404ForUnknownPage() throws IOException {
        var requestTarget = "/some/file/" + System.currentTimeMillis() + ".txt";
        var connection = openConnection(requestTarget);
        assertEquals(404, connection.getResponseCode());
        assertEquals("Not found " + requestTarget, readContent(connection.getErrorStream()));
    }

    @Test
    void shouldReturn200ForExistingPage() throws IOException {
        var contentRoot = Path.of("target", "test", "server");
        Files.createDirectories(contentRoot);

        var content = "Hello World " + UUID.randomUUID();
        Files.writeString(contentRoot.resolve("hello.txt"), content);
        server.setContentRoot(contentRoot);

        var connection = openConnection("/hello.txt");
        assertEquals(200, connection.getResponseCode());
        assertEquals(content, readContent(connection.getInputStream()));
    }

    @Test
    void shouldReturnIndexHtml() throws IOException {
        var contentRoot = Path.of("target", "test", UUID.randomUUID().toString());
        Files.createDirectories(contentRoot.resolve("subdir"));

        var content = "<h1>Hello World " + UUID.randomUUID() + "</h1>";
        Files.writeString(contentRoot.resolve("subdir/index.html"), content);
        server.setContentRoot(contentRoot);

        var connection = openConnection("/subdir");
        assertEquals(200, connection.getResponseCode());
        assertEquals(content, readContent(connection.getInputStream()));
    }

    @Test
    void shouldShowDirectoryContentOnMissingIndex() throws IOException {
        var contentRoot = Path.of("target", "test", UUID.randomUUID().toString());
        Files.createDirectories(contentRoot.resolve("subdir"));

        Files.writeString(contentRoot.resolve("subdir/hello.txt"), "");
        Files.writeString(contentRoot.resolve("subdir/world.txt"), "");
        server.setContentRoot(contentRoot);

        var connection = openConnection("/subdir");
        assertEquals(200, connection.getResponseCode());
        assertEquals("""
                <ul>
                    <li><a href="hello.txt">hello.txt</a></li>
                    <li><a href="world.txt">world.txt</a></li>
                </ul>
                """, readContent(connection.getInputStream()));
    }

    @Test
    void shouldReturnBinaryContent() throws IOException {
        var contentRoot = Path.of("target", "test", UUID.randomUUID().toString());
        Files.createDirectories(contentRoot);
        Files.copy(Path.of("src", "main", "resources", "favicon.ico"), contentRoot.resolve("favicon.ico"));

        assertEquals(200, openConnection("/favicon.ico").getResponseCode());
    }

    @Test
    void shouldHandleMultipleRequests() throws IOException {
        assertEquals(404, openConnection("/missing-file.txt").getResponseCode());
        assertEquals(404, openConnection("/missing-file.txt").getResponseCode());
    }

    @Test
    void shouldRespondWith401ForUnauthenticatedUsers() throws IOException {
        var connection = openConnection("/api/login");
        assertEquals(401, connection.getResponseCode());
        assertEquals("Please log in", readContent(connection.getErrorStream()));
    }

    @Test
    void shouldRespondWithSessionUsernameForApiLogin() throws IOException {
        var connection = openConnection("/api/login");
        connection.setRequestProperty("Cookie", "session=javaBin+kristiansand");
        assertEquals(200, connection.getResponseCode());
        assertEquals("Hello javaBin+kristiansand", readContent(connection.getInputStream()));
    }

    @Test
    void shouldRespondWithCookieWhenUserLogsIn() throws IOException {
        var connection = openConnection("/api/login");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.getOutputStream().write("username=hei+paa+deg".getBytes());

        assertEquals(200, connection.getResponseCode());
        assertEquals("session=hei+paa+deg", connection.getHeaderField("Set-Cookie"));
    }


    private static String readContent(InputStream inputStream) throws IOException {
        var buffer = new ByteArrayOutputStream();
        inputStream.transferTo(buffer);
        return buffer.toString();
    }

    private HttpURLConnection openConnection(String requestTarget) throws IOException {
        var url = new URL(server.getURL(), requestTarget);
        return (HttpURLConnection) url.openConnection();
    }


}