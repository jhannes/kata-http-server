package com.soprasteria.http;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpServerResponse {

    private final Map<String, String> headers = new LinkedHashMap<>();
    private final OutputStream outputStream;

    public HttpServerResponse header(String headerName, String headerValue) {
        headers.put(headerName, headerValue);
        return this;
    }


    @FunctionalInterface
    public interface OutputStreamCallback {
        void apply(OutputStream outputStream) throws IOException;
    }

    private int statusCode = 404;

    public HttpServerResponse(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public HttpServerResponse status(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public HttpServerResponse contentType(String contentType) {
        return header("Content-Type", contentType);
    }

    public void sendBody(String body) throws IOException {
        sendHeaders();
        writeLine("Content-Length: " + body.getBytes().length);
        writeLine("");
        outputStream.write(body.getBytes());
    }

    public void complete() throws IOException {
        sendHeaders();
        writeLine("");
    }

    public void streamOutput(long contentLength, OutputStreamCallback callback) throws IOException {
        sendHeaders();
        writeLine("Content-Length: " + contentLength);
        writeLine("");
        callback.apply(outputStream);
    }

    public int getStatusCode() {
        return statusCode;
    }

    private void sendHeaders() throws IOException {
        writeResponseLine();
        writeLine("Connection: close");
        for (var header : headers.entrySet()) {
            writeLine(header.getKey() + ": " + header.getValue());
        }
    }

    private void writeResponseLine() throws IOException {
        writeLine("HTTP/1.1 " + statusCode + " " + getStatusMessage());
    }

    private void writeLine(String headerLine) throws IOException {
        outputStream.write((headerLine + "\r\n").getBytes());
    }

    private String getStatusMessage() {
        return switch (statusCode) {
            case 200 -> "OK";
            case 302 -> "Moved";
            case 401 -> "Unauthorized";
            case 404 -> "Not Found";
            default -> "WRONG STATUS CODE";
        };
    }
}
