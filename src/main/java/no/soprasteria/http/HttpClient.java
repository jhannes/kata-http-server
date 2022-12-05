package no.soprasteria.http;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.Socket;

public class HttpClient {

    public static void main(String[] args) throws IOException {
        var socket =  SSLSocketFactory.getDefault().createSocket("www.rfc-editor.org", 443);

        socket.getOutputStream().write("""
                GET / HTTP/1.1\r
                Host: www.rfc-editor.org\r
                Connection: close\r
                \r
                """.getBytes());
        int c;
        while ((c = socket.getInputStream().read()) != -1) {
            System.out.print((char)c);
        }

    }

}
