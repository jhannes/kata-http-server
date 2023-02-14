package com.soprasteria.http;

import java.io.IOException;
import java.net.Socket;

public class HttpClient {

    public static void main(String[] args) throws IOException {
        var socket = new Socket("www.rfc-editor.org", 80);

        socket.getOutputStream().write("""
                GET /rfc/rfc7230 HTTP/1.1\r
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
