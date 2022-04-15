import java.io.IOException;
import java.net.Socket;

public class HttpClient {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("example.org", 80);
        socket.getOutputStream().write(
                ("GET / HTTP/1.1\r\n" +
                "Host: example.org\r\n" +
                "\r\n").getBytes()
        );
        int c;
        while ((c = socket.getInputStream().read()) != -1) {
            System.out.print((char)c);
        }
    }
}
