package no.soprasteria.http;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class HttpServerTest {

    @Test
    void shouldReturn404ForUnknownTarget() throws IOException {
        var server = new HttpServer(0);
        server.start();
        var connection = (HttpURLConnection) new URL(server.getURL(), "/no/such/file").openConnection();
        assertEquals(404, connection.getResponseCode());
    }
}