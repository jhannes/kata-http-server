package com.soprasteria.http;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpServerTest {

    @Test
    void shouldReturn404ForUnknownPath() throws IOException {
        var server = new HttpServer(0);
        var path = "/unknown-path";
        var connection = (HttpURLConnection) new URL(server.getURL(), path).openConnection();
        assertEquals(404, connection.getResponseCode());
        var buffer = new ByteArrayOutputStream();
        connection.getErrorStream().transferTo(buffer);
        var responseBody = buffer.toString();
        assertEquals("Unknown path " + path, responseBody);
    }

}