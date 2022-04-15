import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        Socket clientSocket = serverSocket.accept();
        System.out.println("Client connected");

        String body = "Hello world";
        clientSocket.getOutputStream().write((
                "HTTP/1.1 200 OK\r\n" +
                "Connection: close\r\n" +
                "Content-type: text/html\r\n" +
                "Content-length: " + body.length() + "\r\n" +
                "\r\n" +
                body
                ).getBytes());

        int c;
        while ((c = clientSocket.getInputStream().read()) != -1) {
            System.out.print((char)c);
        }
    }
}
