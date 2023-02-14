package com.soprasteria.http;

import org.junit.jupiter.api.Test;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpServerTest {

    private final Path testDir;
    private final HttpServer server;
    private final SSLContext sslContext;

    public HttpServerTest() throws IOException, GeneralSecurityException {
        testDir = Path.of("target", "test-files", String.valueOf(System.currentTimeMillis()));
        Files.createDirectories(testDir);
        server = new HttpServer(0, testDir);

        var keyStore = KeyStore.getInstance("pkcs12");
        try (var certFile = new FileInputStream("servercert.p12")) {
            keyStore.load(certFile, "abc123".toCharArray());
        }
        var tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);
        sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);
    }

    @Test
    void shouldReturn404ForMissingPage() throws IOException {
        var path = "/no-such-file/" + System.currentTimeMillis();
        var connection = openConnection(path);
        assertEquals(404, connection.getResponseCode());
        assertEquals("Not found " + path, asString(connection.getErrorStream()));
    }

    @Test
    void shouldReturnFileOnDisk() throws IOException {
        var content = "File created at " + LocalTime.now();
        Files.writeString(testDir.resolve("index.html"), content);

        var connection = openConnection("/index.html");
        assertEquals(200, connection.getResponseCode());
        assertEquals(content, asString(connection.getInputStream()));
    }

    @Test
    void shouldSupportMultipleRequests() throws IOException {
        assertEquals(404, openConnection("/foo.txt").getResponseCode());
        assertEquals(404, openConnection("/foo.txt").getResponseCode());
    }

    @Test
    void shouldShowNonLoggedInUser() throws IOException {
        var connection = openConnection("/api/login");
        assertEquals(401, connection.getResponseCode());
        assertEquals("Please log in", asString(connection.getErrorStream()));
    }

    @Test
    void shouldLoginUser() throws IOException {
        var connection = openConnection("/api/login");
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");
        var username = "user" + System.currentTimeMillis();
        connection.setDoOutput(true);
        connection.getOutputStream().write(("username=" + username).getBytes());

        assertEquals(302, connection.getResponseCode());
        assertEquals("http://localhost:8080/index.html", connection.getHeaderField("Location"));
        assertEquals("session=" + username, connection.getHeaderField("Set-Cookie"));
    }

    @Test
    void shouldDisplayLoginInformation() throws IOException {
        var connection = openConnection("/api/login");
        var username = "user" + System.currentTimeMillis();
        connection.setRequestProperty("Cookie", "session=" + username);

        assertEquals(200, connection.getResponseCode());
        assertEquals("Logged in as " + username, asString(connection.getInputStream()));
    }

    private HttpURLConnection openConnection(String path) throws IOException {
        var connection = (HttpsURLConnection) new URL(server.getURL(), path).openConnection();
        connection.setSSLSocketFactory(sslContext.getSocketFactory());
        return connection;
    }

    private static String asString(InputStream inputStream) throws IOException {
        var buffer = new ByteArrayOutputStream();
        inputStream.transferTo(buffer);
        return buffer.toString();
    }

}