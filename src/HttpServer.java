import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class HttpServer {
    public static void main(String[] args) throws IOException {
        new HttpServer().run();
    }

    private void run() throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        while (true) {
            Socket clientSocket = serverSocket.accept();
            handleClient(clientSocket);
        }
    }

    private void handleClient(Socket clientSocket) throws IOException {
        // Read the start line of the request
        // E.g. on the format GET /index.html HTTP/1.1
        String requestLine = readLine(clientSocket);
        String[] parts = requestLine.split(" ", 3);
        String requestTarget = parts[1];

        // Separate out queryParameters from requestTarget
        // E.g. GET /hello?username=Johannes HTTP/1.1
        // requestTarget becomes /hello?username=Johannes
        // requestAction becomes /hello
        // and queryParameters becomes {"username": "Johannes"}
        String requestAction = requestTarget;
        Map<String, String> queryParameters = new HashMap<>();
        int questionPos = requestTarget.indexOf('?');
        if (questionPos != -1) {
            // Parse query parameters
            requestAction = requestTarget.substring(0, questionPos);
            for (String queryParameter : requestTarget.substring(questionPos + 1).split("&")) {
                int equalsPos = queryParameter.indexOf('=');
                String key = queryParameter.substring(equalsPos + 1);
                String value = queryParameter.substring(0, equalsPos);
                queryParameters.put(key, value);
            }
        }

        // Route the request based on the requestAction
        if (requestAction.equals("/hello")) {
            // handle hello action
            String body = "Hello " + queryParameters.get("userName");
            clientSocket.getOutputStream().write((
                    "HTTP/1.1 200 OK\r\n" +
                    "Connection: close\r\n" +
                    "Content-length: " + body.length() + "\r\n" +
                    "\r\n" +
                    body
            ).getBytes());
        }

            // NB: request target always starts with "/".
            // I want to serve the file from the current working
            // directory, not the filesystem root, so I have
            // to discard the first character
            File requestedFile = new File(requestAction.substring(1));
            if (requestedFile.exists()) {
                handleStaticFile(clientSocket, requestedFile);
            } else {
                handleNotFound(clientSocket, requestTarget);
            }

            int c;
            while ((c = clientSocket.getInputStream().read()) != -1) {
                System.out.print((char)c);
            }
        }

private Map<String, String> parseQuery(String queryString) {
    Map<String, String> queryParameters = new HashMap<>();
    for (String queryParameter : queryString.split("&")) {
        int equalsPos = queryParameter.indexOf('=');
        String key = queryParameter.substring(equalsPos + 1);
        String value = queryParameter.substring(0, equalsPos);
        queryParameters.put(key, value);
    }
    return queryParameters;
}

    private String readLine(Socket clientSocket) throws IOException {
        StringBuilder requestLine = new StringBuilder();
        int c;
        while ((c = clientSocket.getInputStream().read()) != '\r') {
            requestLine.append((char) c);
        }
        // We expect a \n after the \r
        clientSocket.getInputStream().read();
        return requestLine.toString();
    }

    private void handleNotFound(
            Socket clientSocket, String requestTarget
    ) throws IOException {
        String body = requestTarget + " not found";
        clientSocket.getOutputStream().write((
                "HTTP/1.1 404 Not Found\r\n" +
                "Connection: close\r\n" +
                "Content-type: text/html\r\n" +
                "Content-length: " + body.length() + "\r\n" +
                "\r\n" +
                body
        ).getBytes());
    }

    private void handleStaticFile(
            Socket clientSocket, File requestedFile
    ) throws IOException {
        clientSocket.getOutputStream().write((
                "HTTP/1.1 200 OK\r\n" +
                "Connection: close\r\n" +
                "Content-type: text/html; charset=utf-8\r\n" +
                "Content-length: " + requestedFile.length() + "\r\n" +
                "\r\n"
        ).getBytes());
        try (InputStream input = new FileInputStream(requestedFile)) {
            input.transferTo(clientSocket.getOutputStream());
        }
    }

    private void handleHelloAction(
            Socket clientSocket
    ) throws IOException {
        Map<String, String> headers = new HashMap<>();
        String headerLine;
        while (!(headerLine = readLine(clientSocket)).isBlank()) {
            int colonPos = headerLine.indexOf(':');
            headers.put(
                    headerLine.substring(0, colonPos).trim(),
                    headerLine.substring(colonPos+1).trim()
            );
        }
        int contentLength =
                Integer.parseInt(headers.get("Content-Length"));
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < contentLength; i++) {
            builder.append((char)clientSocket.getInputStream().read());
        }
        Map<String, String> queryParameters =
                parseQuery(builder.toString());

        String body = "Hello " + queryParameters.get("userName");
        clientSocket.getOutputStream().write((
                "HTTP/1.1 200 OK\r\n" +
                "Connection: close\r\n" +
                "Content-length: " + body.length() + "\r\n" +
                "\r\n" +
                body
        ).getBytes());
    }
}
