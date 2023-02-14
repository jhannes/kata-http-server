package com.soprasteria.http;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpServerTest {

    @Test
    void shouldReturn404ForUnknownPath() throws IOException {
        var server = new HttpServer(0);
        var connection = (HttpURLConnection) new URL(server.getURL(), "/unknown-path").openConnection();
        assertEquals(404, connection.getResponseCode());
    }

}