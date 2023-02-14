package com.soprasteria.http.handlers;

import com.soprasteria.http.HttpServerRequest;
import com.soprasteria.http.HttpServerResponse;
import com.soprasteria.http.RequestHandler;

import java.io.IOException;

public class LoginRequestHandler implements RequestHandler {
    private final RequestHandler getRequestHandler = new LoginGetRequestHandler();
    private final RequestHandler postRequestHandler = new LoginPostRequestHandler();


    @Override
    public void handle(HttpServerRequest request, HttpServerResponse response) throws IOException {
        if (request.isPost()) {
            postRequestHandler.handle(request, response);
        } else {
            getRequestHandler.handle(request, response);
        }
    }
}
