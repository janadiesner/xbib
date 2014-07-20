package org.asynchttpclient.util;

/**
 * Convenience class that encapsulates details of "percent encoding"
 * (as per RFC-3986, see [http://www.ietf.org/rfc/rfc3986.txt]).
 */
public class UTF8UrlEncoder {
    private static final boolean encodeSpaceUsingPlus =
            System.getProperty("com.UTF8UrlEncoder.encodeSpaceUsingPlus") != null;

    /**
     * Encoding table used for figuring out ascii characters that must be escaped
     * (all non-Ascii characters need to be encoded anyway)
     */
    private final static int[] SAFE_ASCII = new int[128];

    static {
        for (int i = 'a'; i <= 'z'; ++i) {
            SAFE_ASCII[i] = 1;
        }
        for (int i = 'A'; i <= 'Z'; ++i) {
            SAFE_ASCII[i] = 1;
        }
        for (int i = '0'; i <= '9'; ++i) {
            SAFE_ASCII[i] = 1;
        }
        SAFE_ASCII['-'] = 1;
        SAFE_ASCII['.'] = 1;
        SAFE_ASCII['_'] = 1;
        SAFE_ASCII['~'] = 1;
    }

    private final static char[] HEX = "0123456789ABCDEF".toCharArray();

    private UTF8UrlEncoder() {
    }

    public static String encode(String input) {
        StringBuilder sb = new StringBuilder(input.length() + 16);
        appendEncoded(sb, input);
        return sb.toString();
    }

    public static StringBuilder appendEncoded(StringBuilder sb, String input) {
        final int[] safe = SAFE_ASCII;

        for (int c, i = 0, len = input.length(); i < len; i+= Character.charCount(c)) {
            c = input.codePointAt(i);
            if (c <= 127) {
                if (safe[c] != 0) {
                    sb.append((char) c);
                } else {
                    appendSingleByteEncoded(sb, c);
                }
            } else {
                appendMultiByteEncoded(sb, c);
            }
        }
        return sb;
    }

    private static void appendSingleByteEncoded(StringBuilder sb, int value) {

        if (encodeSpaceUsingPlus && value == 32) {
            sb.append('+');
            return;
        }

        sb.append('%');
        sb.append(HEX[value >> 4]);
        sb.append(HEX[value & 0xF]);
    }

    private static void appendMultiByteEncoded(StringBuilder sb, int value) {
        if (value < 0x800) {
            appendSingleByteEncoded(sb, (0xc0 | (value >> 6)));
            appendSingleByteEncoded(sb, (0x80 | (value & 0x3f)));
        } else if (value < 0x10000) {
            appendSingleByteEncoded(sb, (0xe0 | (value >> 12)));
            appendSingleByteEncoded(sb, (0x80 | ((value >> 6) & 0x3f)));
            appendSingleByteEncoded(sb, (0x80 | (value & 0x3f)));
        } else {
            appendSingleByteEncoded(sb, (0xf0 | (value >> 18)));
            appendSingleByteEncoded(sb, (0x80 | (value >> 12) & 0x3f));
            appendSingleByteEncoded(sb, (0x80 | (value >> 6) & 0x3f));
            appendSingleByteEncoded(sb, (0x80 | (value & 0x3f)));
        }
    }

}
