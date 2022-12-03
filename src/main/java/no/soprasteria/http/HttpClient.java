package no.soprasteria.http;

import java.io.IOException;
import java.net.Socket;

public class HttpClient {

    public static void main(String[] args) throws IOException {
        var socket = new Socket("127.0.0.1", 3000);

        var body = """
                GET / HTTP/1.1\r
                Host: 127.0.0.1:3000\r
                Connection: close\r
                \r
                """;
        socket.getOutputStream().write(body.getBytes());

        int c;
        while ((c = socket.getInputStream().read()) != -1) {
            System.out.print((char)c);
        }

    }
}
