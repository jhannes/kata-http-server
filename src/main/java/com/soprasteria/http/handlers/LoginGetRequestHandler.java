package com.soprasteria.http.handlers;

import com.soprasteria.http.HttpServerRequest;
import com.soprasteria.http.HttpServerResponse;
import com.soprasteria.http.RequestHandler;

import java.io.IOException;

public class LoginGetRequestHandler implements RequestHandler {
    @Override
    public void handle(HttpServerRequest request, HttpServerResponse response) throws IOException {
        var session = request.getCookies().get("session");
        if (session != null) {
            response.status(200).sendBody("Logged in as " + session);
        } else {
            response.status(401).sendBody("Unauthorized");
        }
    }
}
