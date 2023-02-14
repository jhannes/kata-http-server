package com.soprasteria.http;

import java.io.IOException;
import java.net.Socket;

public class HttpClient {
    public static void main(String[] args) throws IOException {
        try (var socket = new Socket("www.rfc-editor.org", 80)) {

            socket.getOutputStream().write("""
                    GET / HTTP/1.1\r
                    Host: www.rfc-editor.org\r
                    Connection: close\r
                    Accept-Language: nb\r
                    \r
                    """.getBytes());

            int c;
            while ((c = socket.getInputStream().read()) != -1) {
                System.out.print((char) c);
            }
        }
    }
}
