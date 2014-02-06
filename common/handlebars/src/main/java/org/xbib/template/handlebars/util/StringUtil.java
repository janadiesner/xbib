package org.xbib.template.handlebars.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * Some common string manipulation utilities.
 */
public class StringUtil {

    public static final String EMPTY_STRING = "";

    // \u3000 is the double-byte space character in UTF-8
    // \u00A0 is the non-breaking space character (&nbsp;)
    // \u2007 is the figure space character (&#8199;)
    // \u202F is the narrow non-breaking space character (&#8239;)
    private static final String WHITE_SPACES = " \r\n\t\u3000\u00A0\u2007\u202F";

    private static final Pattern htmlTagPattern =
            Pattern.compile("</?[a-zA-Z][^>]*>");

    private static final Pattern characterReferencePattern =
            Pattern.compile("&#?[a-zA-Z0-9]{1,8};");

    // This class should not be instantiated, hence the private constructor
    private StringUtil() {
    }

    /**
     * Split "str" by run of delimiters and return.
     */
    public static String[] split(String str, String delims) {
        return split(str, delims, false);
    }

    /**
     * Split "str" into tokens by delimiters and optionally remove white spaces
     * from the splitted tokens.
     *
     * @param trimTokens if true, then trim the tokens
     */
    private static String[] split(String str, String delims, boolean trimTokens) {
        StringTokenizer tokenizer = new StringTokenizer(str, delims);
        int n = tokenizer.countTokens();
        String[] list = new String[n];
        for (int i = 0; i < n; i++) {
            if (trimTokens) {
                list[i] = tokenizer.nextToken().trim();
            } else {
                list[i] = tokenizer.nextToken();
            }
        }
        return list;
    }

    /**
     * Short hand for <code>split(str, delims, true)</code>
     */
    public static String[] splitAndTrim(String str, String delims) {
        return split(str, delims, true);
    }

    /**
     * Concatenates the String representations of the elements of a
     * String[] array into one String, and inserts a delimiter between
     * each pair of elements.
     * <p/>
     * This includes the String[] case, because if s is a String, then
     * s.toString() returns s.
     */
    public static String join(Object[] tokens, String delimiter) {
        if (tokens == null || tokens.length == 0) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0 && delimiter != null) {
                result.append(delimiter);
            }

            if (tokens[i] != null) {
                result.append(tokens[i].toString());
            }
        }
        return result.toString();
    }

    /**
     * Same as {@link #join(Object[], String)}, but takes a {@link Collection}
     * instead.
     */
    public static String join(Collection tokens, String delimiter) {
        return join(tokens.toArray(), delimiter);
    }

    public static String join(Iterator tokens, String delimiter) {
        if (tokens == null) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (tokens.hasNext()) {
            if (i > 0 && delimiter != null) {
                result.append(delimiter);
            }
            Object o = tokens.next();
            if (o != null) {
                result.append(o.toString());
            }
            i++;
        }
        return result.toString();
    }

    public static String join(Iterable tokens, String delimiter) {
        if (tokens == null) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        int i = 0;
        for (Object o : tokens) {
            if (i > 0 && delimiter != null) {
                result.append(delimiter);
            }
            if (o != null) {
                result.append(o.toString());
            }
            i++;
        }
        return result.toString();
    }

    public static String join(Object[] array, char separator, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        int bufSize = (endIndex - startIndex);
        if (bufSize <= 0) {
            return EMPTY_STRING;
        }
        bufSize *= ((array[startIndex] == null ? 16 : array[startIndex].toString().length()) + 1);
        StringBuilder buf = new StringBuilder(bufSize);
        for (int i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                buf.append(separator);
            }
            if (array[i] != null) {
                buf.append(array[i]);
            }
        }
        return buf.toString();
    }

    /**
     * Reformats the given string to a fixed width by inserting
     * carriage returns and trimming unnecessary whitespace.
     *
     * @param str   the string to format
     * @param width the fixed width (in characters)
     */
    public static String fixedWidth(String str, int width) {
        String[] lines = split(str, "\n");
        return fixedWidth(lines, width);
    }

    /**
     * Reformats the given array of lines to a fixed width by inserting
     * carriage returns and trimming unnecessary whitespace.
     *
     * @param lines - array of lines to format
     * @param width - the fixed width (in characters)
     */
    public static String fixedWidth(String[] lines, int width) {
        StringBuilder formatStr = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            int curWidth = 0;
            if (i != 0) {
                formatStr.append("\n");
            }
            // a small optimization
            if (lines[i].length() <= width) {
                formatStr.append(lines[i]);
                continue;
            }
            String[] words = splitAndTrim(lines[i], WHITE_SPACES);
            for (String word : words) {
                if (curWidth == 0 || (curWidth + word.length()) < width) {
                    // add a space if we're not at the beginning of a line
                    if (curWidth != 0) {
                        formatStr.append(" ");
                        curWidth += 1;
                    }
                    curWidth += word.length();
                    formatStr.append(word);
                } else {
                    formatStr.append("\n");
                    curWidth = word.length();
                    formatStr.append(word);
                }
            }
        }

        return formatStr.toString();
    }

    /**
     * Inserts spaces every splitLen characters so that the string will wrap.
     *
     * @param lineLen  the length of the substrings to separate with spaces.
     * @param original the original String
     * @return original String with spaces inserted every lineLen characters.
     */
    public static String insertBreakingWhitespace(int lineLen, String original) {
        if (original == null || lineLen <= 0) {
            throw new IllegalArgumentException();
        }

        int length = original.length();
        if (length <= lineLen)
        // we can avoid the overhead of instantiating a StringBuilder
        {
            return original;
        }
        int currPos = 0;
        StringBuilder retval = new StringBuilder();
        while (length - currPos > lineLen) {
            retval.append(original.substring(currPos, currPos + lineLen));
            currPos += lineLen;
            retval.append(" ");
        }
        retval.append(original.substring(currPos, length));
        return retval.toString();
    }

    /**
     * This is a both way strip
     *
     * @param str   the string to strip
     * @param left  strip from left
     * @param right strip from right
     * @param what  character(s) to strip
     * @return the stripped string
     *
     */
    public static String megastrip(String str,
                                   boolean left, boolean right,
                                   String what) {
        if (str == null) {
            return null;
        }

        int limitLeft = 0;
        int limitRight = str.length() - 1;

        while (left && limitLeft <= limitRight &&
                what.indexOf(str.charAt(limitLeft)) >= 0) {
            limitLeft++;
        }
        while (right && limitRight >= limitLeft &&
                what.indexOf(str.charAt(limitRight)) >= 0) {
            limitRight--;
        }
        return str.substring(limitLeft, limitRight + 1);
    }

    /**
     * strip - strips both ways
     *
     * @param str what to strip
     * @return String the striped string
     */
    public static String strip(String str) {
        return megastrip(str, true, true, WHITE_SPACES);
    }

    /**
     * Like String.indexOf() except that it will look for any of the
     * characters in 'chars' (similar to C's strpbrk)
     */
    public static int indexOfChars(String str, String chars, int fromIndex) {
        final int len = str.length();

        for (int pos = fromIndex; pos < len; pos++) {
            if (chars.indexOf(str.charAt(pos)) >= 0) {
                return pos;
            }
        }

        return -1;
    }

    /**
     * Like String.indexOf() except that it will look for any of the
     * characters in 'chars' (similar to C's strpbrk)
     */
    public static int indexOfChars(String str, String chars) {
        return indexOfChars(str, chars, 0);
    }

    /**
     * Like String.replace() except that it accepts any number of old chars.
     * Replaces any occurrances of 'oldchars' in 'str' with 'newchar'.
     * Example: replaceChars("Hello, world!", "H,!", ' ') returns " ello  world "
     */
    public static String replaceChars(String str, String oldchars, char newchar) {
        int pos = indexOfChars(str, oldchars);
        if (pos == -1) {
            return str;
        }

        StringBuilder buf = new StringBuilder(str);
        do {
            buf.setCharAt(pos, newchar);
            pos = indexOfChars(str, oldchars, pos + 1);
        } while (pos != -1);

        return buf.toString();
    }


    /**
     * Returns a string consisting of "s", plus enough copies of "pad_ch" on the
     * left hand side to make the length of "s" equal to or greater than len (if
     * "s" is already longer than "len", then "s" is returned).
     */
    public static String padLeft(String s, int len, char pad_ch) {
        if (s.length() >= len) {
            return s;
        } else {
            StringBuilder sb = new StringBuilder();
            int n = len - s.length();
            for (int i = 0; i < n; i++) {
                sb.append(pad_ch);
            }
            sb.append(s);
            return sb.toString();
        }
    }

    /**
     * Returns a string consisting of "s", plus enough copies of "pad_ch" on the
     * right hand side to make the length of "s" equal to or greater than len (if
     * "s" is already longer than "len", then "s" is returned).
     */
    public static String padRight(String s, int len, char pad_ch) {
        if (s.length() >= len) {
            return s;
        } else {
            StringBuilder sb = new StringBuilder();
            int n = len - s.length();
            sb.append(s);
            for (int i = 0; i < n; i++) {
                sb.append(pad_ch);
            }
            return sb.toString();
        }
    }

    private static boolean isOctal(char c) {
        return (c >= '0') && (c <= '7');
    }

    private static boolean isHex(char c) {
        return ((c >= '0') && (c <= '9')) ||
                ((c >= 'a') && (c <= 'f')) ||
                ((c >= 'A') && (c <= 'F'));
    }

    private static int hexValue(char c) {
        if ((c >= '0') && (c <= '9')) {
            return (c - '0');
        } else if ((c >= 'a') && (c <= 'f')) {
            return (c - 'a') + 10;
        } else {
            return (c - 'A') + 10;
        }
    }

    /**
     * Unescape any C escape sequences (\n, \r, \\, \ooo, etc) and return the
     * resulting string.
     */
    public static String unescapeCString(String s) {
        if (s.indexOf('\\') < 0) {
            // Fast path: nothing to unescape
            return s;
        }

        StringBuilder sb = new StringBuilder();
        int len = s.length();
        for (int i = 0; i < len; ) {
            char c = s.charAt(i++);
            if (c == '\\' && (i < len)) {
                c = s.charAt(i++);
                switch (c) {
                    case 'a':
                        c = '\007';
                        break;
                    case 'b':
                        c = '\b';
                        break;
                    case 'f':
                        c = '\f';
                        break;
                    case 'n':
                        c = '\n';
                        break;
                    case 'r':
                        c = '\r';
                        break;
                    case 't':
                        c = '\t';
                        break;
                    case 'v':
                        c = '\013';
                        break;
                    case '\\':
                        c = '\\';
                        break;
                    case '?':
                        c = '?';
                        break;
                    case '\'':
                        c = '\'';
                        break;
                    case '"':
                        c = '\"';
                        break;

                    default: {
                        if ((c == 'x') && (i < len) && isHex(s.charAt(i))) {
                            // "\xXX"
                            int v = hexValue(s.charAt(i++));
                            if ((i < len) && isHex(s.charAt(i))) {
                                v = v * 16 + hexValue(s.charAt(i++));
                            }
                            c = (char) v;
                        } else if (isOctal(c)) {
                            // "\OOO"
                            int v = (c - '0');
                            if ((i < len) && isOctal(s.charAt(i))) {
                                v = v * 8 + (s.charAt(i++) - '0');
                            }
                            if ((i < len) && isOctal(s.charAt(i))) {
                                v = v * 8 + (s.charAt(i++) - '0');
                            }
                            c = (char) v;
                        } else {
                            // Propagate unknown escape sequences.
                            sb.append('\\');
                        }
                        break;
                    }
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Unescape any MySQL escape sequences.
     * See MySQL language reference Chapter 6 at
     * <a href="http://www.mysql.com/doc/">http://www.mysql.com/doc/</a>.
     * This function will <strong>not</strong> work for other SQL-like
     * dialects.
     *
     * @param s string to unescape, with the surrounding quotes.
     * @return unescaped string, without the surrounding quotes.
     * @throws IllegalArgumentException if s is not a valid MySQL string.
     */
    public static String unescapeMySQLString(String s)
            throws IllegalArgumentException {
        // note: the same buffer is used for both reading and writing
        // it works because the writer can never outrun the reader
        char chars[] = s.toCharArray();

        // the string must be quoted 'like this' or "like this"
        if (chars.length < 2 || chars[0] != chars[chars.length - 1] ||
                (chars[0] != '\'' && chars[0] != '"')) {
            throw new IllegalArgumentException("not a valid MySQL string: " + s);
        }

        // parse the string and decode the backslash sequences; in addition,
        // quotes can be escaped 'like this: ''', "like this: """, or 'like this: "'
        int j = 1;  // write position in the string (never exceeds read position)
        int f = 0;  // state: 0 (normal), 1 (backslash), 2 (quote)
        for (int i = 1; i < chars.length - 1; i++) {
            if (f == 0) {             // previous character was normal
                if (chars[i] == '\\') {
                    f = 1;  // backslash
                } else if (chars[i] == chars[0]) {
                    f = 2;  // quoting character
                } else {
                    chars[j++] = chars[i];
                }
            } else if (f == 1) {      // previous character was a backslash
                switch (chars[i]) {
                    case '0':
                        chars[j++] = '\0';
                        break;
                    case '\'':
                        chars[j++] = '\'';
                        break;
                    case '"':
                        chars[j++] = '"';
                        break;
                    case 'b':
                        chars[j++] = '\b';
                        break;
                    case 'n':
                        chars[j++] = '\n';
                        break;
                    case 'r':
                        chars[j++] = '\r';
                        break;
                    case 't':
                        chars[j++] = '\t';
                        break;
                    case 'z':
                        chars[j++] = '\032';
                        break;
                    case '\\':
                        chars[j++] = '\\';
                        break;
                    default:
                        // if the character is not special, backslash disappears
                        chars[j++] = chars[i];
                        break;
                }
                f = 0;
            } else {                  // previous character was a quote
                // quoting characters must be doubled inside a string
                if (chars[i] != chars[0]) {
                    throw new IllegalArgumentException("not a valid MySQL string: " + s);
                }
                chars[j++] = chars[0];
                f = 0;
            }
        }
        // string contents cannot end with a special character
        if (f != 0) {
            throw new IllegalArgumentException("not a valid MySQL string: " + s);
        }

        // done
        return new String(chars, 1, j - 1);
    }

    static Map<String, Character> escapeStrings;

    static {
        // HTML character entity references as defined in HTML 4
        // see http://www.w3.org/TR/REC-html40/sgml/entities.html
        escapeStrings = new HashMap<String, Character>(252);

        escapeStrings.put("&nbsp;", '\u00A0');
        escapeStrings.put("&iexcl;", '\u00A1');
        escapeStrings.put("&cent;", '\u00A2');
        escapeStrings.put("&pound;", '\u00A3');
        escapeStrings.put("&curren;", '\u00A4');
        escapeStrings.put("&yen;", '\u00A5');
        escapeStrings.put("&brvbar;", '\u00A6');
        escapeStrings.put("&sect;", '\u00A7');
        escapeStrings.put("&uml;", '\u00A8');
        escapeStrings.put("&copy;", '\u00A9');
        escapeStrings.put("&ordf;", '\u00AA');
        escapeStrings.put("&laquo;", '\u00AB');
        escapeStrings.put("&not;", '\u00AC');
        escapeStrings.put("&shy;", '\u00AD');
        escapeStrings.put("&reg;", '\u00AE');
        escapeStrings.put("&macr;", '\u00AF');
        escapeStrings.put("&deg;", '\u00B0');
        escapeStrings.put("&plusmn;", '\u00B1');
        escapeStrings.put("&sup2;", '\u00B2');
        escapeStrings.put("&sup3;", '\u00B3');
        escapeStrings.put("&acute;", '\u00B4');
        escapeStrings.put("&micro;", '\u00B5');
        escapeStrings.put("&para;", '\u00B6');
        escapeStrings.put("&middot;", '\u00B7');
        escapeStrings.put("&cedil;", '\u00B8');
        escapeStrings.put("&sup1;", '\u00B9');
        escapeStrings.put("&ordm;", '\u00BA');
        escapeStrings.put("&raquo;", '\u00BB');
        escapeStrings.put("&frac14;", '\u00BC');
        escapeStrings.put("&frac12;", '\u00BD');
        escapeStrings.put("&frac34;", '\u00BE');
        escapeStrings.put("&iquest;", '\u00BF');
        escapeStrings.put("&Agrave;", '\u00C0');
        escapeStrings.put("&Aacute;", '\u00C1');
        escapeStrings.put("&Acirc;", '\u00C2');
        escapeStrings.put("&Atilde;", '\u00C3');
        escapeStrings.put("&Auml;", '\u00C4');
        escapeStrings.put("&Aring;", '\u00C5');
        escapeStrings.put("&AElig;", '\u00C6');
        escapeStrings.put("&Ccedil;", '\u00C7');
        escapeStrings.put("&Egrave;", '\u00C8');
        escapeStrings.put("&Eacute;", '\u00C9');
        escapeStrings.put("&Ecirc;", '\u00CA');
        escapeStrings.put("&Euml;", '\u00CB');
        escapeStrings.put("&Igrave;", '\u00CC');
        escapeStrings.put("&Iacute;", '\u00CD');
        escapeStrings.put("&Icirc;", '\u00CE');
        escapeStrings.put("&Iuml;", '\u00CF');
        escapeStrings.put("&ETH;", '\u00D0');
        escapeStrings.put("&Ntilde;", '\u00D1');
        escapeStrings.put("&Ograve;", '\u00D2');
        escapeStrings.put("&Oacute;", '\u00D3');
        escapeStrings.put("&Ocirc;", '\u00D4');
        escapeStrings.put("&Otilde;", '\u00D5');
        escapeStrings.put("&Ouml;", '\u00D6');
        escapeStrings.put("&times;", '\u00D7');
        escapeStrings.put("&Oslash;", '\u00D8');
        escapeStrings.put("&Ugrave;", '\u00D9');
        escapeStrings.put("&Uacute;", '\u00DA');
        escapeStrings.put("&Ucirc;", '\u00DB');
        escapeStrings.put("&Uuml;", '\u00DC');
        escapeStrings.put("&Yacute;", '\u00DD');
        escapeStrings.put("&THORN;", '\u00DE');
        escapeStrings.put("&szlig;", '\u00DF');
        escapeStrings.put("&agrave;", '\u00E0');
        escapeStrings.put("&aacute;", '\u00E1');
        escapeStrings.put("&acirc;", '\u00E2');
        escapeStrings.put("&atilde;", '\u00E3');
        escapeStrings.put("&auml;", '\u00E4');
        escapeStrings.put("&aring;", '\u00E5');
        escapeStrings.put("&aelig;", '\u00E6');
        escapeStrings.put("&ccedil;", '\u00E7');
        escapeStrings.put("&egrave;", '\u00E8');
        escapeStrings.put("&eacute;", '\u00E9');
        escapeStrings.put("&ecirc;", '\u00EA');
        escapeStrings.put("&euml;", '\u00EB');
        escapeStrings.put("&igrave;", '\u00EC');
        escapeStrings.put("&iacute;", '\u00ED');
        escapeStrings.put("&icirc;", '\u00EE');
        escapeStrings.put("&iuml;", '\u00EF');
        escapeStrings.put("&eth;", '\u00F0');
        escapeStrings.put("&ntilde;", '\u00F1');
        escapeStrings.put("&ograve;", '\u00F2');
        escapeStrings.put("&oacute;", '\u00F3');
        escapeStrings.put("&ocirc;", '\u00F4');
        escapeStrings.put("&otilde;", '\u00F5');
        escapeStrings.put("&ouml;", '\u00F6');
        escapeStrings.put("&divide;", '\u00F7');
        escapeStrings.put("&oslash;", '\u00F8');
        escapeStrings.put("&ugrave;", '\u00F9');
        escapeStrings.put("&uacute;", '\u00FA');
        escapeStrings.put("&ucirc;", '\u00FB');
        escapeStrings.put("&uuml;", '\u00FC');
        escapeStrings.put("&yacute;", '\u00FD');
        escapeStrings.put("&thorn;", '\u00FE');
        escapeStrings.put("&yuml;", '\u00FF');
        escapeStrings.put("&fnof;", '\u0192');
        escapeStrings.put("&Alpha;", '\u0391');
        escapeStrings.put("&Beta;", '\u0392');
        escapeStrings.put("&Gamma;", '\u0393');
        escapeStrings.put("&Delta;", '\u0394');
        escapeStrings.put("&Epsilon;", '\u0395');
        escapeStrings.put("&Zeta;", '\u0396');
        escapeStrings.put("&Eta;", '\u0397');
        escapeStrings.put("&Theta;", '\u0398');
        escapeStrings.put("&Iota;", '\u0399');
        escapeStrings.put("&Kappa;", '\u039A');
        escapeStrings.put("&Lambda;", '\u039B');
        escapeStrings.put("&Mu;", '\u039C');
        escapeStrings.put("&Nu;", '\u039D');
        escapeStrings.put("&Xi;", '\u039E');
        escapeStrings.put("&Omicron;", '\u039F');
        escapeStrings.put("&Pi;", '\u03A0');
        escapeStrings.put("&Rho;", '\u03A1');
        escapeStrings.put("&Sigma;", '\u03A3');
        escapeStrings.put("&Tau;", '\u03A4');
        escapeStrings.put("&Upsilon;", '\u03A5');
        escapeStrings.put("&Phi;", '\u03A6');
        escapeStrings.put("&Chi;", '\u03A7');
        escapeStrings.put("&Psi;", '\u03A8');
        escapeStrings.put("&Omega;", '\u03A9');
        escapeStrings.put("&alpha;", '\u03B1');
        escapeStrings.put("&beta;", '\u03B2');
        escapeStrings.put("&gamma;", '\u03B3');
        escapeStrings.put("&delta;", '\u03B4');
        escapeStrings.put("&epsilon;", '\u03B5');
        escapeStrings.put("&zeta;", '\u03B6');
        escapeStrings.put("&eta;", '\u03B7');
        escapeStrings.put("&theta;", '\u03B8');
        escapeStrings.put("&iota;", '\u03B9');
        escapeStrings.put("&kappa;", '\u03BA');
        escapeStrings.put("&lambda;", '\u03BB');
        escapeStrings.put("&mu;", '\u03BC');
        escapeStrings.put("&nu;", '\u03BD');
        escapeStrings.put("&xi;", '\u03BE');
        escapeStrings.put("&omicron;", '\u03BF');
        escapeStrings.put("&pi;", '\u03C0');
        escapeStrings.put("&rho;", '\u03C1');
        escapeStrings.put("&sigmaf;", '\u03C2');
        escapeStrings.put("&sigma;", '\u03C3');
        escapeStrings.put("&tau;", '\u03C4');
        escapeStrings.put("&upsilon;", '\u03C5');
        escapeStrings.put("&phi;", '\u03C6');
        escapeStrings.put("&chi;", '\u03C7');
        escapeStrings.put("&psi;", '\u03C8');
        escapeStrings.put("&omega;", '\u03C9');
        escapeStrings.put("&thetasym;", '\u03D1');
        escapeStrings.put("&upsih;", '\u03D2');
        escapeStrings.put("&piv;", '\u03D6');
        escapeStrings.put("&bull;", '\u2022');
        escapeStrings.put("&hellip;", '\u2026');
        escapeStrings.put("&prime;", '\u2032');
        escapeStrings.put("&Prime;", '\u2033');
        escapeStrings.put("&oline;", '\u203E');
        escapeStrings.put("&frasl;", '\u2044');
        escapeStrings.put("&weierp;", '\u2118');
        escapeStrings.put("&image;", '\u2111');
        escapeStrings.put("&real;", '\u211C');
        escapeStrings.put("&trade;", '\u2122');
        escapeStrings.put("&alefsym;", '\u2135');
        escapeStrings.put("&larr;", '\u2190');
        escapeStrings.put("&uarr;", '\u2191');
        escapeStrings.put("&rarr;", '\u2192');
        escapeStrings.put("&darr;", '\u2193');
        escapeStrings.put("&harr;", '\u2194');
        escapeStrings.put("&crarr;", '\u21B5');
        escapeStrings.put("&lArr;", '\u21D0');
        escapeStrings.put("&uArr;", '\u21D1');
        escapeStrings.put("&rArr;", '\u21D2');
        escapeStrings.put("&dArr;", '\u21D3');
        escapeStrings.put("&hArr;", '\u21D4');
        escapeStrings.put("&forall;", '\u2200');
        escapeStrings.put("&part;", '\u2202');
        escapeStrings.put("&exist;", '\u2203');
        escapeStrings.put("&empty;", '\u2205');
        escapeStrings.put("&nabla;", '\u2207');
        escapeStrings.put("&isin;", '\u2208');
        escapeStrings.put("&notin;", '\u2209');
        escapeStrings.put("&ni;", '\u220B');
        escapeStrings.put("&prod;", '\u220F');
        escapeStrings.put("&sum;", '\u2211');
        escapeStrings.put("&minus;", '\u2212');
        escapeStrings.put("&lowast;", '\u2217');
        escapeStrings.put("&radic;", '\u221A');
        escapeStrings.put("&prop;", '\u221D');
        escapeStrings.put("&infin;", '\u221E');
        escapeStrings.put("&ang;", '\u2220');
        escapeStrings.put("&and;", '\u2227');
        escapeStrings.put("&or;", '\u2228');
        escapeStrings.put("&cap;", '\u2229');
        escapeStrings.put("&cup;", '\u222A');
        escapeStrings.put("&int;", '\u222B');
        escapeStrings.put("&there4;", '\u2234');
        escapeStrings.put("&sim;", '\u223C');
        escapeStrings.put("&cong;", '\u2245');
        escapeStrings.put("&asymp;", '\u2248');
        escapeStrings.put("&ne;", '\u2260');
        escapeStrings.put("&equiv;", '\u2261');
        escapeStrings.put("&le;", '\u2264');
        escapeStrings.put("&ge;", '\u2265');
        escapeStrings.put("&sub;", '\u2282');
        escapeStrings.put("&sup;", '\u2283');
        escapeStrings.put("&nsub;", '\u2284');
        escapeStrings.put("&sube;", '\u2286');
        escapeStrings.put("&supe;", '\u2287');
        escapeStrings.put("&oplus;", '\u2295');
        escapeStrings.put("&otimes;", '\u2297');
        escapeStrings.put("&perp;", '\u22A5');
        escapeStrings.put("&sdot;", '\u22C5');
        escapeStrings.put("&lceil;", '\u2308');
        escapeStrings.put("&rceil;", '\u2309');
        escapeStrings.put("&lfloor;", '\u230A');
        escapeStrings.put("&rfloor;", '\u230B');
        escapeStrings.put("&lang;", '\u2329');
        escapeStrings.put("&rang;", '\u232A');
        escapeStrings.put("&loz;", '\u25CA');
        escapeStrings.put("&spades;", '\u2660');
        escapeStrings.put("&clubs;", '\u2663');
        escapeStrings.put("&hearts;", '\u2665');
        escapeStrings.put("&diams;", '\u2666');
        escapeStrings.put("&quot;", '\u0022');
        escapeStrings.put("&amp;", '\u0026');
        escapeStrings.put("&lt;", '\u003C');
        escapeStrings.put("&gt;", '\u003E');
        escapeStrings.put("&OElig;", '\u0152');
        escapeStrings.put("&oelig;", '\u0153');
        escapeStrings.put("&Scaron;", '\u0160');
        escapeStrings.put("&scaron;", '\u0161');
        escapeStrings.put("&Yuml;", '\u0178');
        escapeStrings.put("&circ;", '\u02C6');
        escapeStrings.put("&tilde;", '\u02DC');
        escapeStrings.put("&ensp;", '\u2002');
        escapeStrings.put("&emsp;", '\u2003');
        escapeStrings.put("&thinsp;", '\u2009');
        escapeStrings.put("&zwnj;", '\u200C');
        escapeStrings.put("&zwj;", '\u200D');
        escapeStrings.put("&lrm;", '\u200E');
        escapeStrings.put("&rlm;", '\u200F');
        escapeStrings.put("&ndash;", '\u2013');
        escapeStrings.put("&mdash;", '\u2014');
        escapeStrings.put("&lsquo;", '\u2018');
        escapeStrings.put("&rsquo;", '\u2019');
        escapeStrings.put("&sbquo;", '\u201A');
        escapeStrings.put("&ldquo;", '\u201C');
        escapeStrings.put("&rdquo;", '\u201D');
        escapeStrings.put("&bdquo;", '\u201E');
        escapeStrings.put("&dagger;", '\u2020');
        escapeStrings.put("&Dagger;", '\u2021');
        escapeStrings.put("&permil;", '\u2030');
        escapeStrings.put("&lsaquo;", '\u2039');
        escapeStrings.put("&rsaquo;", '\u203A');
        escapeStrings.put("&euro;", '\u20AC');
    }

    /**
     * Replace all the occurences of HTML escape strings with the
     * respective characters.
     *
     * @param s a <code>String</code> value
     * @return a <code>String</code> value
     */
    public static String unescapeHTML(String s) {
        char[] chars = s.toCharArray();
        char[] escaped = new char[chars.length];

        // Note: escaped[pos] = end of the escaped char array.
        int pos = 0;

        for (int i = 0; i < chars.length; ) {
            if (chars[i] != '&') {
                escaped[pos++] = chars[i++];
                continue;
            }

            // Allow e.g. &#123;
            int j = i + 1;
            if (j < chars.length && chars[j] == '#') {
                j++;
            }

            // Scan until we find a char that is not letter or digit.
            for (; j < chars.length; j++) {
                if (!Character.isLetterOrDigit(chars[j])) {
                    break;
                }
            }

            boolean replaced = false;
            if (j < chars.length && chars[j] == ';') {
                if (s.charAt(i + 1) == '#') { // Check for &#D; and &#xD; pattern
                    try {
                        long charcode = 0;
                        char ch = s.charAt(i + 2);
                        if (ch == 'x' || ch == 'X') {
                            charcode = Long.parseLong(new String(chars, i + 3, j - i - 3),
                                    16);
                        } else if (Character.isDigit(ch)) {
                            charcode = Long.parseLong(new String(chars, i + 2, j - i - 2));
                        }
                        if (charcode > 0 && charcode < 65536) {
                            escaped[pos++] = (char) charcode;
                            replaced = true;
                        }
                    } catch (NumberFormatException ex) {
                        // Failed, not replaced.
                    }

                } else {
                    String key = new String(chars, i, j - i + 1);
                    Character repl = escapeStrings.get(key);
                    if (repl != null) {
                        escaped[pos++] = repl;
                        replaced = true;
                    }
                }
                j++;                            // Skip over ';'
            }

            if (!replaced) {
                // Not a recognized escape sequence, leave as-is
                System.arraycopy(chars, i, escaped, pos, j - i);
                pos += j - i;
            }
            i = j;
        }
        return new String(escaped, 0, pos);
    }

    /**
     * Given a <code>String</code>, returns an equivalent <code>String</code> with
     * all HTML tags stripped. Note that HTML entities, such as "&amp;amp;" will
     * still be preserved.
     */
    public static String stripHtmlTags(String string) {
        if ((string == null) || "".equals(string)) {
            return string;
        }
        return htmlTagPattern.matcher(string).replaceAll("");
    }

    /**
     * We escape some characters in s to be able to make the string executable
     * from a python string
     */
    public static String pythonEscape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\"':
                    sb.append("\\\"");
                    break;
                case '\'':
                    sb.append("\\\'");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * We escape some characters in s to be able to insert strings into JavaScript
     * code. Also, make sure that we don't write out --> or </scrip, which may
     * close a script tag.
     */
    public static String javaScriptEscape(String s) {
        return javaScriptEscapeHelper(s, false);
    }

    /**
     * We escape some characters in s to be able to insert strings into JavaScript
     * code. Also, make sure that we don't write out --> or &lt;/scrip, which may
     * close a script tag. Turns all non-ascii characters into ASCII javascript
     * escape sequences (eg \udddd)
     */
    public static String javaScriptEscapeToAscii(String s) {
        return javaScriptEscapeHelper(s, true);
    }

    private static final String[] UNSAFE_TAGS = {"script", "style",
            "object", "applet", "!--"};

    /**
     * Helper for javaScriptEscape and javaScriptEscapeToAscii
     */
    private static String javaScriptEscapeHelper(String s,
                                                 boolean escapeToAscii) {

    /*
     * IMPORTANT: If you change the semantics of this method (by escaping
     * extra characters, for example), please make similar changes to
     *   com.google.javascript.util.Escape.toJsString
     */
        StringBuilder sb = new StringBuilder(s.length() * 9 / 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\"':
                    sb.append("\\\"");
                    break;
                case '\'':
                    sb.append("\\\'");
                    break;

                // escape '=' so that javascript won't be executed within tags
                case '=':
                    appendHexJavaScriptRepresentation(sb, c);
                    break;

                case '<':  // for text that could potentially be interpreted as an
                case '/':  // unsafe opening or closing tag, escape the char to hex
                    boolean isUnsafe = false;
                    for (String tag : UNSAFE_TAGS) {
                        if (s.regionMatches(true, i + 1, tag, 0, tag.length())) {
                            isUnsafe = true;
                            break;
                        }
                    }
                    if (isUnsafe) {
                        appendHexJavaScriptRepresentation(sb, c);
                    } else {
                        sb.append(c);
                    }
                    break;
                case '>':
                    if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '-') {
                        sb.append('\\');
                    }
                    sb.append(c);
                    break;
                // Note: Mozilla browser treats the line/paragraph separator
                // as string terminators, so we need to escape them.
                case '\u2028':
                    sb.append("\\u2028");
                    break;
                case '\u2029':
                    sb.append("\\u2029");
                    break;
                default:
                    if (c >= 128 && escapeToAscii) {
                        appendHexJavaScriptRepresentation(sb, c);
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    /**
     * Returns a javascript representation of the character in a hex escaped
     * format. Although this is a rather specific method, it is made public
     * because it is also used by the JSCompiler.
     *
     * @param sb The buffer to which the hex representation should be appended.
     * @param c  The character to be appended.
     */
    public static void appendHexJavaScriptRepresentation(StringBuilder sb,
                                                         char c) {
        sb.append("\\u");
        String val = Integer.toHexString(c);
        for (int j = val.length(); j < 4; j++) {
            sb.append('0');
        }
        sb.append(val);
    }


    /**
     * Undo escaping as performed in javaScriptEscape(.)
     * Throws an IllegalArgumentException if the string contains
     * bad escaping.
     */
    public static String javaScriptUnescape(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); ) {
            char c = s.charAt(i);
            if (c == '\\') {
                i = javaScriptUnescapeHelper(s, i + 1, sb);
            } else {
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
    }

    /**
     * Looks for an escape code starting at index i of s,
     * and appends it to sb.
     *
     * @return the index of the first character in s
     * after the escape code.
     * @throws IllegalArgumentException if the escape code
     *                                  is invalid
     */
    private static int javaScriptUnescapeHelper(String s, int i,
                                                StringBuilder sb) {
        if (i >= s.length()) {
            throw new IllegalArgumentException(
                    "End-of-string after escape character in [" + s + "]");
        }

        char c = s.charAt(i++);
        switch (c) {
            case 'n':
                sb.append('\n');
                break;
            case 'r':
                sb.append('\r');
                break;
            case 't':
                sb.append('\t');
                break;
            case '\\':
            case '\"':
            case '\'':
            case '>':
                sb.append(c);
                break;
            case 'u':
                String hexCode;
                try {
                    hexCode = s.substring(i, i + 4);
                } catch (IndexOutOfBoundsException ioobe) {
                    throw new IllegalArgumentException(
                            "Invalid unicode sequence [" + s.substring(i) + "] at index " + i +
                                    " in [" + s + "]");
                }
                int unicodeValue;
                try {
                    unicodeValue = Integer.parseInt(hexCode, 16);
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException(
                            "Invalid unicode sequence [" + hexCode + "] at index " + i +
                                    " in [" + s + "]");
                }
                sb.append((char) unicodeValue);
                i += 4;
                break;
            default:
                throw new IllegalArgumentException(
                        "Unknown escape code [" + c + "] at index " + i + " in [" + s + "]");
        }

        return i;
    }

    /**
     * Escape a string for use inside as XML element content. This escapes
     * less-than and ampersand, only.
     */
    public static String xmlContentEscape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;

                case '\000':
                case '\001':
                case '\002':
                case '\003':
                case '\004':
                case '\005':
                case '\006':
                case '\007':
                case '\010':
                case '\013':
                case '\014':
                case '\016':
                case '\017':
                case '\020':
                case '\021':
                case '\022':
                case '\023':
                case '\024':
                case '\025':
                case '\026':
                case '\027':
                case '\030':
                case '\031':
                case '\032':
                case '\033':
                case '\034':
                case '\035':
                case '\036':
                case '\037':
                    // do nothing, these are disallowed characters
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Escape a string for use inside as XML single-quoted attributes. This
     * escapes less-than, single-quote, ampersand, and (not strictly necessary)
     * newlines.
     */
    public static String xmlSingleQuotedEscape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\'':
                    sb.append("&quot;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '\n':
                    sb.append("&#xA;");
                    break;

                case '\000':
                case '\001':
                case '\002':
                case '\003':
                case '\004':
                case '\005':
                case '\006':
                case '\007':
                case '\010':
                case '\013':
                case '\014':
                case '\016':
                case '\017':
                case '\020':
                case '\021':
                case '\022':
                case '\023':
                case '\024':
                case '\025':
                case '\026':
                case '\027':
                case '\030':
                case '\031':
                case '\032':
                case '\033':
                case '\034':
                case '\035':
                case '\036':
                case '\037':
                    // do nothing, these are disallowed characters
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }


    /**
     * We escape some characters in s to be able to insert strings into Java code
     */
    public static String javaEscape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\"':
                    sb.append("\\\"");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '\'':
                    sb.append("\\\'");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Escape a string so that it can be safely placed as value of an
     * attribute.  This is essentially similar to the
     * javaEscape(java.lang.String) except that it escapes double quote
     * to the HTML literal &amp;quot;. This is to prevent the double
     * quote from being interpreted as the character closing the
     * attribute.
     */
    public static String javaEscapeWithinAttribute(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\"':
                    sb.append("&quot;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '\'':
                    sb.append("\\\'");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Returns a form of "s" appropriate for including in an XML document, after
     * escaping certain special characters (e.g. '&' => '&amp;', etc.)
     */
    public static String xmlEscape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '"':
                    sb.append("&quot;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                case '\'':
                    sb.append("&apos;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '\n':
                    sb.append("&#xA;");
                    break;
                case '\r':
                    sb.append("&#xD;");
                    break;
                case '\t':
                    sb.append("&#x9;");
                    break;
                case '\0':
                    // \0 is not a valid XML char - skip it
                    break;
                default:
                    sb.append(ch);
                    break;
            }
        }
        return sb.toString();
    }

    /**
     * Escapes special characters (& < > ") from a string so it can safely be
     * included in an HTML document. (same as <code>xmlEscape</code> except that
     * <code>htmlEscape</code> does not escape the apostrophe character).
     */
    public static String htmlEscape(String s) {
        // This method gets called A LOT so it has to be excruciatingly efficient.
        // Older versions were responsible for several percent of all objects
        // created on the heap, and 10% of total execution time.
        // In particular, if the String has no characters that need escaping, this
        // method should return its argument.

        StringBuilder sb = null;
        String replacement;
        int start = 0; // the earliest input position we haven't copied yet.
        for (int i = 0; i < s.length(); i++) {
            switch (s.charAt(i)) {
                case '"':
                    replacement = "&quot;";
                    break;
                case '&':
                    replacement = "&amp;";
                    break;
                case '<':
                    replacement = "&lt;";
                    break;
                case '>':
                    replacement = "&gt;";
                    break;
                case '`':
                    replacement = "&#x60;";
                    break;
                case '\'':
                    replacement = "&#x27;";
                    break;
                default:
                    replacement = null;
                    break;
            }

            if (replacement != null) {
                if (sb == null) {
                    // This is the first time we have found a replacement. Allocate the
                    // StringBuilder now.
                    // This initial size for the StringBuilder below will be exact if
                    // this initial replacement is the only one. If not, sb will expand.
                    sb = new StringBuilder(s.length() + replacement.length() - 1);
                }
                if (i > start) {
                    // we have to copy some of the earlier string.
                    sb.append(s.substring(start, i));
                }
                sb.append(replacement);
                start = i + 1;
            }
        }
        // now possibly also copy what's leftover in the input string.
        if (start > 0) {
            sb.append(s.substring(start));
        }

        if (sb != null) {
            return sb.toString();
        }
        return s;
    }

    /**
     * Escapes the special characters from a string so it can be used as part of
     * a regex pattern. This method is for use on gnu.regexp style regular
     * expressions.
     */
    public static String regexEscape(String s) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            // Test if c is an escapable character
            if ("()|*+?.{}[]$^\\".indexOf(c) != -1) {
                sb.append('\\');
                sb.append(c);
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    public static String csvEscape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == ',') {
                sb.append('\\');
                sb.append(c);
            } else if (c == '\"') {
                sb.append("\"\"\"");
            } else if (c == '\r') {
                sb.append("\\r");
            } else if (c == '\n') {
                sb.append("\\n");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Converts a delimited string to a collection of strings. Substrings between
     * delimiters are extracted from the string and added to a collection that is
     * provided by the caller.
     *
     * @param in         The delimited input string to process
     * @param delimiter  The string delimiting entries in the input string.
     * @param doStrip    Whether to strip the substrings before adding to the
     *                   collection
     * @param collection The collection to which the strings will be added. If
     *                   <code>null</code>, a new <code>List</code> will be created.
     * @return The collection to which the substrings were added. This is
     * syntactic sugar to allow call chaining.
     */
    public static Collection<String> string2Collection(String in,
                                                       String delimiter,
                                                       boolean doStrip,
                                                       Collection<String> collection) {
        if (in == null) {
            return null;
        }
        if (collection == null) {
            collection = new ArrayList<String>();
        }
        if (delimiter == null || delimiter.length() == 0) {
            collection.add(in);
            return collection;
        }

        int fromIndex = 0;
        int pos;
        while ((pos = in.indexOf(delimiter, fromIndex)) >= 0) {
            String interim = in.substring(fromIndex, pos);
            if (doStrip) {
                interim = strip(interim);
            }
            if (!doStrip || interim.length() > 0) {
                collection.add(interim);
            }

            fromIndex = pos + delimiter.length();
        }

        String interim = in.substring(fromIndex);
        if (doStrip) {
            interim = strip(interim);
        }
        if (!doStrip || interim.length() > 0) {
            collection.add(interim);
        }

        return collection;
    }

    /**
     * Replaces any string of adjacent whitespace characters with the whitespace
     * character " ".
     *
     * @param str the string you want to munge
     * @return String with no more excessive whitespace!
     */
    public static String collapseWhitespace(String str) {
        return collapse(str, WHITE_SPACES, " ");
    }

    /**
     * Replaces any string of matched characters with the supplied string.<p>
     * <p/>
     * This is a more general version of collapseWhitespace.
     * <p/>
     * <pre>
     *   E.g. collapse("hello     world", " ", "::")
     *   will return the following string: "hello::world"
     * </pre>
     *
     * @param str         the string you want to munge
     * @param chars       all of the characters to be considered for munge
     * @param replacement the replacement string
     * @return String munged and replaced string.
     */
    public static String collapse(String str, String chars, String replacement) {
        if (str == null) {
            return null;
        }

        StringBuilder newStr = new StringBuilder();

        boolean prevCharMatched = false;
        char c;
        for (int i = 0; i < str.length(); i++) {
            c = str.charAt(i);
            if (chars.indexOf(c) != -1) {
                // this character is matched
                if (prevCharMatched) {
                    // apparently a string of matched chars, so don't append anything
                    // to the string
                    continue;
                }
                prevCharMatched = true;
                newStr.append(replacement);
            } else {
                prevCharMatched = false;
                newStr.append(c);
            }
        }

        return newStr.toString();
    }

    /**
     * Read a String of up to maxLength bytes from an InputStream
     *
     * @param is        input stream
     * @param maxLength max number of bytes to read from "is". If this is -1, we
     *                  read everything.
     * @return String up to maxLength bytes, read from "is"
     */
    public static String stream2String(InputStream is, int maxLength) throws IOException {
        byte[] buffer = new byte[4096];
        int totalRead = 0;
        int read = 0;
        StringWriter sw = new StringWriter();
        do {
            sw.write(new String(buffer, 0, read));
            totalRead += read;
            read = is.read(buffer, 0, buffer.length);
        } while (((-1 == maxLength) || (totalRead < maxLength)) && (read != -1));
        return sw.toString();
    }


    /**
     * Helper function for null and empty string testing.
     *
     * @return true iff s == null or s.equals("");
     */
    public static boolean isEmpty(String s) {
        return makeSafe(s).length() == 0;
    }

    /**
     * Helper function for making null strings safe for comparisons, etc.
     *
     * @return (s == null) ? "" : s;
     */
    public static String makeSafe(String s) {
        return (s == null) ? "" : s;
    }

    /**
     * Compares two strings, guarding against nulls If both Strings are null we
     * return true
     */
    public static boolean equals(String s1, String s2) {
        if (s1 == s2) {
            return true; // Either both the same String, or both null
        }
        if (s1 != null) {
            if (s2 != null) {
                return s1.equals(s2);
            }
        }
        return false;
    }

    /**
     * Determines if a string contains only ascii characters
     */
    public static boolean allAscii(String s) {
        int len = s.length();
        for (int i = 0; i < len; ++i) {
            if ((s.charAt(i) & 0xff80) != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determines if a string is a Hebrew word. A string is considered to be
     * a Hebrew word if {@link #isHebrew(int)} is true for any of its characters.
     */
    public static boolean isHebrew(String s) {
        int len = s.length();
        for (int i = 0; i < len; ++i) {
            if (isHebrew(s.codePointAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if a character is a Hebrew character.
     */
    public static boolean isHebrew(int codePoint) {
        return Character.UnicodeBlock.HEBREW.equals(
                Character.UnicodeBlock.of(codePoint));
    }

    /**
     * Determines if a string is a CJK word. A string is considered to be CJK
     * if {@link #isCjk(char)} is true for any of its characters.
     */
    public static boolean isCjk(String s) {
        int len = s.length();
        for (int i = 0; i < len; ++i) {
            if (isCjk(s.codePointAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Unicode code blocks containing CJK characters.
     */
    private static final Set<Character.UnicodeBlock> CJK_BLOCKS;

    static {
        Set<Character.UnicodeBlock> set = new HashSet<Character.UnicodeBlock>();
        set.add(Character.UnicodeBlock.HANGUL_JAMO);
        set.add(Character.UnicodeBlock.CJK_RADICALS_SUPPLEMENT);
        set.add(Character.UnicodeBlock.KANGXI_RADICALS);
        set.add(Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION);
        set.add(Character.UnicodeBlock.HIRAGANA);
        set.add(Character.UnicodeBlock.KATAKANA);
        set.add(Character.UnicodeBlock.BOPOMOFO);
        set.add(Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO);
        set.add(Character.UnicodeBlock.KANBUN);
        set.add(Character.UnicodeBlock.BOPOMOFO_EXTENDED);
        set.add(Character.UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS);
        set.add(Character.UnicodeBlock.ENCLOSED_CJK_LETTERS_AND_MONTHS);
        set.add(Character.UnicodeBlock.CJK_COMPATIBILITY);
        set.add(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A);
        set.add(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS);
        set.add(Character.UnicodeBlock.HANGUL_SYLLABLES);
        set.add(Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS);
        set.add(Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS);
        set.add(Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS);
        set.add(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B);
        set.add(Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT);
        CJK_BLOCKS = Collections.unmodifiableSet(set);
    }

    /**
     * Determines if a character is a CJK ideograph or a character typically
     * used only in CJK text.
     * <p/>
     * Note: This function cannot handle supplementary characters. To handle all
     * Unicode characters, including supplementary characters, use the function
     * {@link #isCjk(int)}.
     */
    public static boolean isCjk(char ch) {
        return isCjk((int) ch);
    }

    /**
     * Determines if a character is a CJK ideograph or a character typically
     * used only in CJK text.
     */
    public static boolean isCjk(int codePoint) {
        // Time-saving early exit for all Latin-1 characters.
        if ((codePoint & 0xFFFFFF00) == 0) {
            return false;
        }

        return CJK_BLOCKS.contains(Character.UnicodeBlock.of(codePoint));
    }

    /**
     * Replaces each non-ascii character in s with its Unicode escape sequence
     * \\uxxxx where xxxx is a hex number. Existing escape sequences won't be
     * affected.
     */
    public static String unicodeEscape(String s) {
        if (allAscii(s)) {
            return s;
        }
        StringBuilder sb = new StringBuilder(s.length());
        int len = s.length();
        for (int i = 0; i < len; ++i) {
            char ch = s.charAt(i);
            if (ch <= 127) {
                sb.append(ch);
            } else {
                sb.append("\\u");
                String hexString = Integer.toHexString(ch);
                // Pad with zeros if necessary
                int numZerosToPad = 4 - hexString.length();
                for (int j = 0; j < numZerosToPad; ++j) {
                    sb.append('0');
                }
                sb.append(hexString);
            }
        }
        return sb.toString();
    }


    /**
     * @return a string representation of the given native array.
     */
    public static String toString(float[] iArray) {
        if (iArray == null) {
            return "NULL";
        }

        StringBuilder buffer = new StringBuilder();
        buffer.append("[");
        for (int i = 0; i < iArray.length; i++) {
            buffer.append(iArray[i]);
            if (i != (iArray.length - 1)) {
                buffer.append(", ");
            }
        }
        buffer.append("]");
        return buffer.toString();
    }

    /**
     * @return a string representation of the given native array.
     */
    public static String toString(long[] iArray) {
        if (iArray == null) {
            return "NULL";
        }

        StringBuilder buffer = new StringBuilder();
        buffer.append("[");
        for (int i = 0; i < iArray.length; i++) {
            buffer.append(iArray[i]);
            if (i != (iArray.length - 1)) {
                buffer.append(", ");
            }
        }
        buffer.append("]");
        return buffer.toString();
    }

    /**
     * @return a string representation of the given native array
     */
    public static String toString(int[] iArray) {
        if (iArray == null) {
            return "NULL";
        }

        StringBuilder buffer = new StringBuilder();
        buffer.append("[");
        for (int i = 0; i < iArray.length; i++) {
            buffer.append(iArray[i]);
            if (i != (iArray.length - 1)) {
                buffer.append(", ");
            }
        }
        buffer.append("]");
        return buffer.toString();
    }

    /**
     * @return a string representation of the given array.
     */
    public static String toString(String[] iArray) {
        if (iArray == null) {
            return "NULL";
        }
        StringBuilder buffer = new StringBuilder();
        buffer.append("[");
        for (int i = 0; i < iArray.length; i++) {
            buffer.append("'").append(iArray[i]).append("'");
            if (i != iArray.length - 1) {
                buffer.append(", ");
            }
        }
        buffer.append("]");
        return buffer.toString();
    }

    /**
     * Returns the string, in single quotes, or "NULL". Intended only for
     * logging.
     *
     * @param s - the string
     * @return the string, in single quotes, or the string "null" if it's null.
     */
    public static String toString(String s) {
        if (s == null) {
            return "NULL";
        } else {
            return "'" + s + "'";
        }
    }

    /**
     * @return a string representation of the given native array
     */
    public static String toString(int[][] iArray) {
        if (iArray == null) {
            return "NULL";
        }

        StringBuilder buffer = new StringBuilder();
        buffer.append("[");
        for (int i = 0; i < iArray.length; i++) {
            buffer.append("[");
            for (int j = 0; j < iArray[i].length; j++) {
                buffer.append(iArray[i][j]);
                if (j != (iArray[i].length - 1)) {
                    buffer.append(", ");
                }
            }
            buffer.append("]");
            if (i != iArray.length - 1) {
                buffer.append(" ");
            }
        }
        buffer.append("]");
        return buffer.toString();
    }

    /**
     * @return a string representation of the given native array.
     */
    public static String toString(long[][] iArray) {
        if (iArray == null) {
            return "NULL";
        }

        StringBuilder buffer = new StringBuilder();
        buffer.append("[");
        for (int i = 0; i < iArray.length; i++) {
            buffer.append("[");
            for (int j = 0; j < iArray[i].length; j++) {
                buffer.append(iArray[i][j]);
                if (j != (iArray[i].length - 1)) {
                    buffer.append(", ");
                }
            }
            buffer.append("]");
            if (i != iArray.length - 1) {
                buffer.append(" ");
            }
        }
        buffer.append("]");
        return buffer.toString();
    }

    /**
     * @return a String representation of the given object array.
     * The strings are obtained by calling toString() on the
     * underlying objects.
     */
    public static String toString(Object[] obj) {
        if (obj == null) {
            return "NULL";
        }
        StringBuilder tmp = new StringBuilder();
        tmp.append("[");
        for (int i = 0; i < obj.length; i++) {
            tmp.append(obj[i].toString());
            if (i != obj.length - 1) {
                tmp.append(",");
            }
        }
        tmp.append("]");
        return tmp.toString();
    }

    private static char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};


    /**
     * Convert a byte array to a hex-encoding string with the specified
     * delimiter: "a3&lt;delimiter&gt;3b&lt;delimiter&gt;ff..."
     */
    public static String bytesToHexString(final byte[] bytes,
                                          Character delimiter) {
        StringBuilder hex = new StringBuilder(bytes.length * (delimiter == null ? 2 : 3));
        int nibble1, nibble2;
        for (int i = 0; i < bytes.length; i++) {
            nibble1 = (bytes[i] >>> 4) & 0xf;
            nibble2 = bytes[i] & 0xf;
            if (i > 0 && delimiter != null) {
                hex.append(delimiter.charValue());
            }
            hex.append(hexChars[nibble1]);
            hex.append(hexChars[nibble2]);
        }
        return hex.toString();
    }

    /**
     * Convert a byte array to a String using the specified encoding.
     *
     * @param encoding the encoding to use
     * @return the decoded String or null if ba is null
     */
    private static String bytesToEncoding(final byte[] ba, final String encoding) {
        if (ba == null) {
            return null;
        }

        try {
            return new String(ba, encoding);
        } catch (final UnsupportedEncodingException e) {
            throw new Error(encoding + " not supported! Original exception: " + e);
        }
    }

    /**
     * Convert a String to a byte array using the specified encoding.
     *
     * @param encoding the encoding to use
     * @return the encoded byte array or null if str is null
     */
    public static byte[] encodingToBytes(
            final String str, final String encoding) {

        if (str == null) {
            return null;
        }

        try {
            return str.getBytes(encoding);
        } catch (final UnsupportedEncodingException e) {
            throw new Error(encoding + " not supported! Original exception: " + e);
        }
    }

    /**
     * Returns sourceString concatenated together 'factor' times.
     *
     * @param sourceString The string to repeat
     * @param factor       The number of times to repeat it.
     */
    public static String repeat(String sourceString, int factor) {
        if (factor < 1) {
            return "";
        }
        if (factor == 1) {
            return sourceString;
        }

        StringBuilder sb = new StringBuilder(factor * sourceString.length());

        while (factor > 0) {
            sb.append(sourceString);
            factor--;
        }

        return sb.toString();
    }

    /**
     * Returns a string that is equivalent to the specified string with its
     * first character converted to uppercase as by {@link String#toUpperCase}.
     * The returned string will have the same value as the specified string if
     * its first character is non-alphabetic, if its first character is already
     * uppercase, or if the specified string is of length 0.
     * <p/>
     * <p>For example:
     * <pre>
     *    capitalize("foo bar").equals("Foo bar");
     *    capitalize("2b or not 2b").equals("2b or not 2b")
     *    capitalize("Foo bar").equals("Foo bar");
     *    capitalize("").equals("");
     * </pre>
     *
     * @param str the string whose first character is to be uppercased
     * @return a string equivalent to <tt>s</tt> with its first character
     * converted to uppercase
     * @throws NullPointerException if <tt>s</tt> is null
     */
    public static String uncapitalize(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }
        return new StringBuilder(strLen)
                .append(Character.toLowerCase(str.charAt(0)))
                .append(str.substring(1))
                .toString();
    }

    public static String center(String str, int size, char padChar) {
        if (str == null || size <= 0) {
            return str;
        }
        int strLen = str.length();
        int pads = size - strLen;
        if (pads <= 0) {
            return str;
        }
        str = leftPad(str, strLen + pads / 2, padChar);
        str = rightPad(str, size, padChar);
        return str;
    }

    public static String center(String str, int size, String padStr) {
        if (str == null || size <= 0) {
            return str;
        }
        if (isEmpty(padStr)) {
            padStr = " ";
        }
        int strLen = str.length();
        int pads = size - strLen;
        if (pads <= 0) {
            return str;
        }
        str = leftPad(str, strLen + pads / 2, padStr);
        str = rightPad(str, size, padStr);
        return str;
    }

    public static String leftPad(String str, int size, char padChar) {
        if (str == null) {
            return null;
        }
        int pads = size - str.length();
        if (pads <= 0) {
            return str; // returns original String when possible
        }
        if (pads > PAD_LIMIT) {
            return leftPad(str, size, String.valueOf(padChar));
        }
        return padding(pads, padChar).concat(str);
    }

    public static String leftPad(String str, int size, String padStr) {
        if (str == null) {
            return null;
        }
        if (isEmpty(padStr)) {
            padStr = " ";
        }
        int padLen = padStr.length();
        int strLen = str.length();
        int pads = size - strLen;
        if (pads <= 0) {
            return str; // returns original String when possible
        }
        if (padLen == 1 && pads <= PAD_LIMIT) {
            return leftPad(str, size, padStr.charAt(0));
        }

        if (pads == padLen) {
            return padStr.concat(str);
        } else if (pads < padLen) {
            return padStr.substring(0, pads).concat(str);
        } else {
            char[] padding = new char[pads];
            char[] padChars = padStr.toCharArray();
            for (int i = 0; i < pads; i++) {
                padding[i] = padChars[i % padLen];
            }
            return new String(padding).concat(str);
        }
    }

    public static String rightPad(String str, int size, char padChar) {
        if (str == null) {
            return null;
        }
        int pads = size - str.length();
        if (pads <= 0) {
            return str; // returns original String when possible
        }
        if (pads > PAD_LIMIT) {
            return rightPad(str, size, String.valueOf(padChar));
        }
        return str.concat(padding(pads, padChar));
    }

    public static String rightPad(String str, int size, String padStr) {
        if (str == null) {
            return null;
        }
        if (isEmpty(padStr)) {
            padStr = " ";
        }
        int padLen = padStr.length();
        int strLen = str.length();
        int pads = size - strLen;
        if (pads <= 0) {
            return str; // returns original String when possible
        }
        if (padLen == 1 && pads <= PAD_LIMIT) {
            return rightPad(str, size, padStr.charAt(0));
        }

        if (pads == padLen) {
            return str.concat(padStr);
        } else if (pads < padLen) {
            return str.concat(padStr.substring(0, pads));
        } else {
            char[] padding = new char[pads];
            char[] padChars = padStr.toCharArray();
            for (int i = 0; i < pads; i++) {
                padding[i] = padChars[i % padLen];
            }
            return str.concat(new String(padding));
        }
    }

    private static String padding(int repeat, char padChar) throws IndexOutOfBoundsException {
        if (repeat < 0) {
            throw new IndexOutOfBoundsException("Cannot pad a negative amount: " + repeat);
        }
        final char[] buf = new char[repeat];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = padChar;
        }
        return new String(buf);
    }

    private static final int PAD_LIMIT = 8192;

    public static String abbreviate(String str, int offset, int maxWidth) {
        if (str == null) {
            return null;
        }
        if (maxWidth < 4) {
            throw new IllegalArgumentException("Minimum abbreviation width is 4");
        }
        if (str.length() <= maxWidth) {
            return str;
        }
        if (offset > str.length()) {
            offset = str.length();
        }
        if ((str.length() - offset) < (maxWidth - 3)) {
            offset = str.length() - (maxWidth - 3);
        }
        if (offset <= 4) {
            return str.substring(0, maxWidth - 3) + "...";
        }
        if (maxWidth < 7) {
            throw new IllegalArgumentException("Minimum abbreviation width with offset is 7");
        }
        if ((offset + (maxWidth - 3)) < str.length()) {
            return "..." + abbreviate(str.substring(offset), maxWidth - 3);
        }
        return "..." + str.substring(str.length() - (maxWidth - 3));
    }

    public static String abbreviate(String str, int maxWidth) {
        return abbreviate(str, 0, maxWidth);
    }

    public static String capitalize(String str) {
        return capitalize(str, null);
    }

    public static String capitalize(String str, char[] delimiters) {
        if (str == null || str.length() == 0) {
            return str;
        }
        int strLen = str.length();
        StringBuilder buffer = new StringBuilder(strLen);
        int delimitersLen = 0;
        if (delimiters != null) {
            delimitersLen = delimiters.length;
        }
        boolean capitalizeNext = true;
        for (int i = 0; i < strLen; i++) {
            char ch = str.charAt(i);

            boolean isDelimiter = false;
            if (delimiters == null) {
                isDelimiter = Character.isWhitespace(ch);
            } else {
                for (int j = 0; j < delimitersLen; j++) {
                    if (ch == delimiters[j]) {
                        isDelimiter = true;
                        break;
                    }
                }
            }
            if (isDelimiter) {
                buffer.append(ch);
                capitalizeNext = true;
            } else if (capitalizeNext) {
                buffer.append(Character.toTitleCase(ch));
                capitalizeNext = false;
            } else {
                buffer.append(ch);
            }
        }
        return buffer.toString();
    }

    public static String capitalizeFully(String str) {
        return capitalizeFully(str, null);
    }

    public static String capitalizeFully(String str, char[] delimiters) {
        if (str == null || str.length() == 0) {
            return str;
        }
        str = str.toLowerCase();
        return capitalize(str, delimiters);
    }

}