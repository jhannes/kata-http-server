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
        ServerSocket serverSocket = new ServerSocket(8080);
        while (true) {
            Socket clientSocket = serverSocket.accept();
            handleClient(clientSocket);
        }
    }

    private static void handleClient(Socket clientSocket) throws IOException {
        StringBuilder requestLine = new StringBuilder();
        int c;
        while ((c = clientSocket.getInputStream().read()) != '\r') {
            requestLine.append((char) c);
        }
        String[] parts = requestLine.toString().split(" ", 3);
        String requestTarget = parts[1];

        String requestAction = requestTarget;
        Map<String, String> queryParameters = new HashMap<>();
        int questionPos = requestTarget.indexOf('?');
        if (questionPos != -1) {
            requestAction = requestTarget.substring(0, questionPos);
            String[] query = requestTarget.substring(questionPos+1).split("&");
            for (String queryParameter : query) {
                int equalsPos = queryParameter.indexOf('=');
                queryParameters.put(
                        queryParameter.substring(0, equalsPos),
                        queryParameter.substring(equalsPos+1)
                );
            }
        }
        System.out.println(requestLine + ": requestAction=" + requestAction + " query=" + queryParameters);

        // NB: request target always starts with "/".
        // I want to serve the file from the current working
        // directory, not the filesystem root, so I have
        // to discard the first character
        File requestedFile = new File(requestAction.substring(1));

        if (requestAction.equals("/hello")) {
            handleHelloAction(clientSocket, queryParameters);
        } else if (requestedFile.exists()) {
            handleStaticFile(clientSocket, requestedFile);
        } else {
            handleNotFound(clientSocket, requestTarget);
        }

        while ((c = clientSocket.getInputStream().read()) != -1) {
            System.out.print((char)c);
        }
    }

    private static void handleNotFound(Socket clientSocket, String requestTarget) throws IOException {
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

    private static void handleStaticFile(Socket clientSocket, File requestedFile) throws IOException {
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

    private static void handleHelloAction(Socket clientSocket, Map<String, String> queryParameters) throws IOException {
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
