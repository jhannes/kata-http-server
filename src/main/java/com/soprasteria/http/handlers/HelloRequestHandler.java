package com.soprasteria.http.handlers;

import com.soprasteria.http.HttpServerRequest;
import com.soprasteria.http.HttpServerResponse;
import com.soprasteria.http.RequestHandler;

import java.io.IOException;

public class HelloRequestHandler implements RequestHandler {
    @Override
    public void handle(HttpServerRequest request, HttpServerResponse response) throws IOException {
        response.status(200)
                .contentType("text/plain; charset=utf-8")
                .sendBody("Hællæ verden");
    }
}
