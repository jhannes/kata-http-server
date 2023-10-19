package com.soprasteria.johannes;

import org.junit.jupiter.api.Test;

import java.net.URLEncoder;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JohannesIsStubbornTest {

    @Test
    void shouldEncodeAsUtf() {
        var s = "blåbærsyltetøy + JORDBÆRSYLTETØY";
        assertEquals(URLEncoder.encode(s, UTF_8), stupidEncode(s));
    }

    @Test
    void shouldDecodeAsUtf() {
        var s = "blåbærsyltetøy ++ JORDBÆRSYLTETØY";
        assertEquals(s, stupidDecode(URLEncoder.encode(s, UTF_8)));
    }

    private String stupidDecode(String s) {
        s = s.replace('+', ' ');
        var UTF_16BIT = Pattern.compile("%([A-Z0-9]{2})%([A-Z0-9]{2})");
        var UTF_8BIT = Pattern.compile("%([A-Z0-9]{2})");
        s = UTF_16BIT.matcher(s).replaceAll(match -> new String(new byte[] {
                (byte) Integer.parseInt(match.group(1), 0x10),
                (byte) Integer.parseInt(match.group(2), 0x10),
        }, UTF_8));
        s = UTF_8BIT.matcher(s).replaceAll(match -> {
            var bytes = new byte[]{(byte) Integer.parseInt(match.group(1), 0x10),};
            return new String(bytes, UTF_8);
        });
        return s;
    }

    private String stupidEncode(String s) {
        var result = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == ' ') {
                result.append('+');
            } else if (c < 0x90 && c != '+') {
                result.append(c);
            } else {
                var bytes = Character.toString(c).getBytes(UTF_8);
                for (var b : bytes) {
                    result.append('%');
                    result.append(Integer.toString(0xff & b, 0x10).toUpperCase());
                }
            }
        }
        return result.toString();
    }

}
