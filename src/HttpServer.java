import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        Socket clientSocket = serverSocket.accept();

        StringBuilder requestLine = new StringBuilder();
        int c;
        while ((c = clientSocket.getInputStream().read()) != '\r') {
            requestLine.append((char) c);
        }
        System.out.println(requestLine);
        String[] parts = requestLine.toString().split(" ", 3);
        String requestTarget = parts[1];

        String body = requestTarget + " not found";
        clientSocket.getOutputStream().write((
                "HTTP/1.1 404 Not Found\r\n" +
                "Connection: close\r\n" +
                "Content-type: text/html\r\n" +
                "Content-length: " + body.length() + "\r\n" +
                "\r\n" +
                body
        ).getBytes());


        while ((c = clientSocket.getInputStream().read()) != -1) {
            System.out.print((char)c);
        }
    }
}
