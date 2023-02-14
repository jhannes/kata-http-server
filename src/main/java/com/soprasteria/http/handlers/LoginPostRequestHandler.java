package com.soprasteria.http.handlers;

import com.soprasteria.http.HttpServerRequest;
import com.soprasteria.http.HttpServerResponse;
import com.soprasteria.http.RequestHandler;

import java.io.IOException;

public class LoginPostRequestHandler implements RequestHandler {
    @Override
    public void handle(HttpServerRequest request, HttpServerResponse response) throws IOException {
        var parameters = request.readBodyAsQueryString();
        response.status(302)
                .header("Location", request.getServerURL().toString())
                .header("Set-Cookie", "session=" + parameters.get("username"))
                .complete();
    }
}
