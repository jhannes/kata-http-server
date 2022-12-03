package no.soprasteria.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpServerTest {
    private static final Random random = new Random();
    private HttpServer server;

    @BeforeEach
    void setUp() throws IOException {
        server = new HttpServer(0);
        server.startServer();
    }

    @Test
    void shouldRespondWith404ToUnknownPages() throws IOException {
        var path = "/unknown-url-" + random.nextInt();
        var connection = getOpenConnection(path);
        assertEquals(404, connection.getResponseCode());
        assertEquals("Unknown file " + path, asString(connection.getErrorStream()));
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