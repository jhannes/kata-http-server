package no.soprasteria.http;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpServerTest {

    @Test
    void shouldRespondWith404ToUnknownPages() throws IOException {
        var server = new HttpServer(0);
        server.startServer();
        var connection = (HttpURLConnection) new URL(server.getURL(), "/unknown-url").openConnection();
        assertEquals(404, connection.getResponseCode());

        var buffer = new ByteArrayOutputStream();
        connection.getErrorStream().transferTo(buffer);
        assertEquals("Unknown file /unknown-url", buffer.toString());
    }
}