import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

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
        System.out.println(requestLine);
        String[] parts = requestLine.toString().split(" ", 3);
        String requestTarget = parts[1];

        // NB: request target always starts with "/".
        // I want to serve the file from the current working
        // directory, not the filesystem root, so I have
        // to discard the first character
        File requestedFile = new File(requestTarget.substring(1));

        if (requestedFile.exists()) {
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
        } else {
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

        while ((c = clientSocket.getInputStream().read()) != -1) {
            System.out.print((char)c);
        }
    }
}
