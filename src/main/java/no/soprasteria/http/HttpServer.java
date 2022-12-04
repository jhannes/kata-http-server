package no.soprasteria.http;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HttpServer {

    public static void main(String[] args) throws IOException {
        new HttpServer(8080, Path.of("src", "main", "resources", "web")).startServer();
    }

    private final ServerSocket socket;
    private final Path baseDir;

    public HttpServer(int port, Path baseDir) throws IOException {
        socket = new ServerSocket(port);
        this.baseDir = baseDir;
    }

    void startServer() {
        new Thread(() -> {
            while (!Thread.interrupted()) {
                try (var clientSocket = socket.accept()) {
                    handleRequest(clientSocket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Terminated server");
        }).start();
    }

    private void handleRequest(Socket clientSocket) throws IOException {
        var requestLine = readLine(clientSocket.getInputStream());
        System.out.println(requestLine);
        var parts = requestLine.split(" ");
        var requestMethod = parts[0];
        var requestTarget = parts[1];

        var headers = readHeaders(clientSocket.getInputStream());

        if (requestTarget.equals("/api/login")) {
            if (requestMethod.equals("POST")) {
                doPostLogin(clientSocket, headers);
            } else if (requestMethod.equals("GET")) {
                doGetLogin(clientSocket, headers);
            }
            return;
        }

        var resolvedPath = baseDir.resolve(requestTarget.substring(1));
        if (Files.isDirectory(resolvedPath)) {
            resolvedPath = resolvedPath.resolve("index.html");
        }
        if (Files.exists(resolvedPath)) {
            writeHeader(clientSocket, 200, "OK", getContentType(resolvedPath));
            clientSocket.getOutputStream().write((Long.toHexString(Files.size(resolvedPath)) + "\r\n").getBytes());
            try (var inputStream = new FileInputStream(resolvedPath.toFile())) {
                inputStream.transferTo(clientSocket.getOutputStream());
            }
            clientSocket.getOutputStream().write(("\r\n0\r\n\r\n").getBytes());
        } else {
            System.out.println("404");
            writeHeader(clientSocket, 404, "NOT FOUND", "text/html; charset=utf-8");
            writeResponseBody(clientSocket, "Unknown file " + requestTarget);
        }
    }

    private void doGetLogin(Socket clientSocket, Map<String, String> headers) throws IOException {
        var cookies = parseCookies(headers.get("Cookie"));
        var user = URLDecoder.decode(cookies.get("user"), UTF_8);
        writeHeader(clientSocket, 200, "OK", "text/html; charset=utf-8");
        writeResponseBody(clientSocket, "Username: " + user);
    }

    private void doPostLogin(Socket clientSocket, Map<String, String> headers) throws IOException {
        var postBody = readBody(clientSocket.getInputStream(), Integer.parseInt(headers.get("Content-Length")));
        var formParams = parseQueryParams(postBody);
        var username = formParams.get("username");
        var host = headers.get("Host");
        var location = "http://" + host + "/";
        var responseHeader = "HTTP/1.1 " + 302 + " " + "MOVED" + "\r\n" +
                             "Connection: close\r\n" +
                             "Location: " + location + "\r\n" +
                             "Set-Cookie: user=" + username + "\r\n" +
                             "\r\n";
        clientSocket.getOutputStream().write(responseHeader.getBytes());
    }

    private static String getContentType(Path resolvedPath) {
        var filename = resolvedPath.getFileName().toString();
        if (filename.endsWith(".css")) {
            return "text/css; charset=utf-8";
        }
        return "text/html; charset=utf-8";
    }

    private Map<String, String> readHeaders(InputStream inputStream) throws IOException {
        var result = new HashMap<String, String>();
        String line;
        while (!(line = readLine(inputStream)).isEmpty()) {
            var parts = line.split(":\\s*", 2);
            result.put(parts[0], parts[1]);
        }
        return result;
    }

    private Map<String, String> parseQueryParams(String query) {
        var result = new HashMap<String, String>();
        for (String queryParam : query.split("&")) {
            var parts = queryParam.split("\\s*=\\s*", 2);
            result.put(parts[0], parts[1]);
        }
        return result;
    }

    private Map<String, String> parseCookies(String cookie) {
        var result = new HashMap<String, String>();
        if (cookie != null) {
            for (String cookieString : cookie.split(";\\s*")) {
                var parts = cookieString.split("=", 2);
                result.put(parts[0], parts[1]);
            }
        }
        return result;
    }


    private String readBody(InputStream inputStream, int contentLength) throws IOException {
        var result = new StringBuilder();
        for (int i = 0; i < contentLength; i++) {
            result.append((char) inputStream.read());
        }
        return result.toString();
    }

    private static void writeResponseBody(Socket clientSocket, String body) throws IOException {
        var contentLength = body.getBytes().length;
        clientSocket.getOutputStream().write((Integer.toHexString(contentLength) + "\r\n" +
                                              body + "\r\n" +
                                              0 + "\r\n\r\n").getBytes(UTF_8));
    }

    private static void writeHeader(Socket clientSocket, int responseCode, String responseMessage, String contentType) throws IOException {
        var responseHeader = "HTTP/1.1 " + responseCode + " " + responseMessage + "\r\n" +
                             "Connection: close\r\n" +
                             "Transfer-Encoding: chunked\r\n" +
                             "Content-Type: " + contentType + "\r\n" +
                             "\r\n";
        clientSocket.getOutputStream().write(responseHeader.getBytes(UTF_8));
    }

    private static String readLine(InputStream inputStream) throws IOException {
        var result = new StringBuilder();
        int c;
        while ((c = inputStream.read()) != '\r') {
            result.append((char) c);
        }
        //noinspection ResultOfMethodCallIgnored
        inputStream.read();
        return result.toString();
    }

    public URL getURL() throws MalformedURLException {
        return new URL("http", "localhost", socket.getLocalPort(), "/");
    }
}
