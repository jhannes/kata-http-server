package com.soprasteria.http;

import java.io.IOException;

public interface RequestHandler {
    void handle(HttpServerRequest request, HttpServerResponse response) throws IOException;

}
