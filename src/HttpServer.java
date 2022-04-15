import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        Socket clientSocket = serverSocket.accept();
        System.out.println("Client connected");
        int c;
        while ((c = clientSocket.getInputStream().read()) != -1) {
            System.out.print((char)c);
        }
    }
}
