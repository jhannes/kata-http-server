package no.soprasteria.http;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpServerTest {
    private static final Random random = new Random();

    @Test
    void shouldRespondWith404ToUnknownPages() throws IOException {
        var server = new HttpServer(0);
        server.startServer();
        var path = "/unknown-url-" + random.nextInt();
        var connection = (HttpURLConnection) new URL(server.getURL(), path).openConnection();
        assertEquals(404, connection.getResponseCode());

        var buffer = new ByteArrayOutputStream();
        connection.getErrorStream().transferTo(buffer);
        assertEquals("Unknown file " + path, buffer.toString());
    }
}