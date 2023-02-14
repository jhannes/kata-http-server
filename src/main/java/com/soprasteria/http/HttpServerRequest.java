package com.soprasteria.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpServerRequest {
    private final String requestMethod;
    private final String requestTarget;
    private final Map<String, String> headers = new LinkedHashMap<>();
    private final InputStream inputStream;
    private final boolean secure;

    public HttpServerRequest(InputStream inputStream, boolean secure) throws IOException {
        this.inputStream = inputStream;
        this.secure = secure;
        var requestLine = readLine();
        var parts = requestLine.split(" ");
        this.requestMethod = parts[0];
        this.requestTarget = parts[1];
        readHeaders();
    }

    private void readHeaders() throws IOException {
        String line;
        while (!(line = readLine().trim()).isEmpty()) {
            var parts = line.split(":\\s*", 2);
            headers.put(parts[0], parts[1]);
        }
    }

    private String readLine() throws IOException {
        var line = new StringBuilder();
        int c;
        while ((c = inputStream.read()) != '\n') {
            if (c == -1 || c == 0) break;
            line.append((char) c);
        }
        return line.toString();
    }

    public String getTarget() {
        return requestTarget;
    }

    public boolean isPost() {
        return requestMethod.equals("POST");
    }

    public Map<String, String> getCookies() {
        var cookies = new HashMap<String, String>();
        var cookieHeader = getHeaders().get("Cookie");
        if (cookieHeader != null) {
            for (var cookie : cookieHeader.split(";\\s*")) {
                var parts = cookie.split("=");
                cookies.put(parts[0], parts[1]);
            }
        }
        return cookies;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> readBodyAsQueryString() throws IOException {
        return parseQueryString(readBody());
    }

    private String readBody() throws IOException {
        var result = new StringBuilder();
        for (int i = 0; i < getContentLength(); i++) {
            result.append((char) inputStream.read());
        }
        return result.toString();
    }


    private Map<String, String> parseQueryString(String queryString) {
        var parameters = new HashMap<String, String>();
        for (var parameter : queryString.split("&")) {
            var parts = parameter.split("=", 2);
            parameters.put(parts[0], parts[1]);
        }
        return parameters;
    }

    public String getHost() {
        return getHeaders().get("Host");
    }

    public String getRequest() {
        return requestMethod + " " + requestTarget;
    }

    public int getContentLength() {
        return Integer.parseInt(headers.get("Content-Length"));
    }

    public URL getServerURL() throws MalformedURLException {
        return new URL((secure ? "https" : "http") + "://" + getHost() + "/");
    }
}
