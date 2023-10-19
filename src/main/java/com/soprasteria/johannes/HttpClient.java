package com.soprasteria.johannes;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;

public class HttpClient {
    public static void main(String[] args) throws IOException {
//        var socket = new Socket("www.rfc-editor.org", 80);
        try (var socket = SSLSocketFactory.getDefault().createSocket("www.rfc-editor.org", 443)) {

            socket.getOutputStream().write("""
                    GET /rfc/rfc7230 HTTP/1.1\r
                    Connection: close\r
                    Host: www.rfc-editor.org\r
                    \r
                    """.getBytes());

            int c;
            while ((c = socket.getInputStream().read()) != -1) {
                System.out.print((char) c);
            }
        }
    }
}
