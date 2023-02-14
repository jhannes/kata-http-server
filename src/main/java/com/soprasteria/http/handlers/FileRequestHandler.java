package com.soprasteria.http.handlers;

import com.soprasteria.http.HttpServerRequest;
import com.soprasteria.http.HttpServerResponse;
import com.soprasteria.http.RequestHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileRequestHandler implements RequestHandler {
    private final Path httpRoot;

    public FileRequestHandler(Path httpRoot) {
        this.httpRoot = httpRoot;
    }

    @Override
    public void handle(HttpServerRequest request, HttpServerResponse response) throws IOException {
        var requestFile = getRequestFile(request.getTarget());
        if (Files.exists(requestFile)) {
            response.status(200).streamOutput(Files.size(requestFile), os -> {
                try (var inputStream = Files.newInputStream(requestFile)) {
                    inputStream.transferTo(os);
                }
            });
        } else {
            response.status(404).sendBody("Not found " + request.getTarget());
        }
    }

    private Path getRequestFile(String requestTarget) {
        var requestFile = httpRoot.resolve(requestTarget.substring(1));
        return Files.isDirectory(requestFile) ? requestFile.resolve("index.html") : requestFile;
    }
}
