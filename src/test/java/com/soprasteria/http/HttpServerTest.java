package com.soprasteria.http;

import org.junit.jupiter.api.Test;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpServerTest {

    private HttpServer server;
    private final Path tempDir = Path.of("target", "test", "http-root-" + System.currentTimeMillis());

    HttpServerTest() throws IOException {
        Files.createDirectories(tempDir);
        server = new HttpServer(tempDir, new ServerSocket(0));
    }

    @Test
    void shouldRespond404ToMissingFile() throws IOException {
        var path = "/no-such-file-" + System.currentTimeMillis();
        var connection = openConnection(path);
        assertEquals(404, connection.getResponseCode());
        assertEquals("Not found " + path, asString(connection.getErrorStream()));
    }

    @Test
    void shouldReturnFileContent() throws IOException {
        var content = "Test Content " + LocalTime.now();
        Files.writeString(tempDir.resolve("example.txt"), content);
        var connection = openConnection("/example.txt");
        assertEquals(200, connection.getResponseCode());
        assertEquals(content, asString(connection.getInputStream()));
    }

    @Test
    void shouldShowWelcomeFile() throws IOException {
        var content = "Test Content " + LocalTime.now();
        Files.writeString(tempDir.resolve("index.html"), content);
        var connection = openConnection("/");
        assertEquals(200, connection.getResponseCode());
        assertEquals(content, asString(connection.getInputStream()));
    }

    @Test
    void shouldShowFavicon() throws IOException {
        Files.copy(Path.of("src", "main", "resources", "favicon.ico"), tempDir.resolve("favicon.ico"));
        assertEquals(200, openConnection("/favicon.ico").getResponseCode());
    }

    @Test
    void shouldHandleMultipleRequests() throws IOException {
        assertEquals(404, openConnection("/index.html").getResponseCode());
        assertEquals(404, openConnection("/index.html").getResponseCode());
    }

    @Test
    void shouldDisplay401ForUnauthorizedUsers() throws IOException {
        var connection = openConnection("/api/login");
        connection.setRequestProperty("Cookie", "otherCookie=something");
        assertEquals(401, connection.getResponseCode());
    }

    @Test
    void shouldShowUsernameForAuthorizedUsers() throws IOException {
        var connection = openConnection("/api/login");
        var username = "my-user:name-" + System.currentTimeMillis();
        connection.setRequestProperty("Cookie", "somecookie=foo; session=" + username + "; othercookie=bar");
        assertEquals(200, connection.getResponseCode());
        assertEquals("Logged in as " + username, asString(connection.getInputStream()));
    }

    @Test
    void shouldSetCookieOnLogin() throws IOException {
        var username = "my+user+name-" + System.currentTimeMillis();

        var connection = openConnection("/api/login");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(false);
        connection.getOutputStream().write(("username=" + username + "&password=abc123").getBytes());
        assertEquals(302, connection.getResponseCode());
        assertEquals(server.getURL().toString(), connection.getHeaderField("Location"));
        assertEquals("session=" + username, connection.getHeaderField("Set-Cookie"));
    }

    @Test
    void shouldEncodeNorwegianCharacters() throws IOException {
        assertEquals("Hællæ verden", asString(openConnection("/api/hello").getInputStream()));
    }

    @Test
    void shouldSupportSecureConnection() throws IOException, GeneralSecurityException {
        var sslContext = SSLContext.getInstance("TLS");
        var keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        var keyStore = KeyStore.getInstance("pkcs12");
        keyStore.load(Files.newInputStream(Path.of("src", "test", "resources", "keyStore.p12")), "changeit".toCharArray());
        keyManagerFactory.init(keyStore, "changeit".toCharArray());
        sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
        var serverSocket = sslContext.getServerSocketFactory().createServerSocket(0);

        this.server = new HttpServer(tempDir, serverSocket);
        var connection = (HttpsURLConnection)openConnection("/api/hello");
        var clientContext = SSLContext.getInstance("TLS");
        var trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        clientContext.init(null, trustManagerFactory.getTrustManagers(), null);
        connection.setSSLSocketFactory(clientContext.getSocketFactory());
        assertEquals(200, connection.getResponseCode());
        assertEquals("Hællæ verden", asString(connection.getInputStream()));
    }

    private static String asString(InputStream inputStream) throws IOException {
        var buffer = new ByteArrayOutputStream();
        inputStream.transferTo(buffer);
        return buffer.toString();
    }

    private HttpURLConnection openConnection(String path) throws IOException {
        return (HttpURLConnection) new URL(server.getURL(), path).openConnection();
    }

}