package no.soprasteria.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class HttpServerTest {

    private HttpServer server;

    @BeforeEach
    void setUp() throws IOException {
        server = new HttpServer(0);
        server.start();
    }

    @Test
    void shouldReturn404ForUnknownTarget() throws IOException {
        var connection = openConnection("/no/such/file");
        assertEquals(404, connection.getResponseCode());
        assertEquals("Not found /no/such/file", asString(connection.getErrorStream()));
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