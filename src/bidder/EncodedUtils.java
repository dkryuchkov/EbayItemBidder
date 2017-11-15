/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bidder;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.annotation.Immutable;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeaderValueParser;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.message.ParserCursor;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EntityUtils;

@Immutable
public class EncodedUtils {

    public static final String CONTENT_TYPE = "application/x-www-form-urlencoded";
    private static final char QP_SEP_A = '&';
    private static final char QP_SEP_S = ';';
    private static final String NAME_VALUE_SEPARATOR = "=";

    public static List<NameValuePair> parse(URI uri, String charset) {
        String query = uri.getRawQuery();
        if ((query != null) && (query.length() > 0)) {
            List<NameValuePair> result = new ArrayList();
            Scanner scanner = new Scanner(query);
            parse(result, scanner, QP_SEP_PATTERN, charset);
            return result;
        }
        return Collections.emptyList();
    }

    public static List<NameValuePair> parse(HttpEntity entity)
            throws IOException {
        ContentType contentType = ContentType.get(entity);
        if ((contentType != null) && (contentType.getMimeType().equalsIgnoreCase("application/x-www-form-urlencoded"))) {
            String content = EntityUtils.toString(entity, Consts.ASCII);
            if ((content != null) && (content.length() > 0)) {
                Charset charset = contentType.getCharset();
                if (charset == null) {
                    charset = HTTP.DEF_CONTENT_CHARSET;
                }
                return parse(content, charset, QP_SEPS);
            }
        }
        return Collections.emptyList();
    }

    public static boolean isEncoded(HttpEntity entity) {
        Header h = entity.getContentType();
        if (h != null) {
            HeaderElement[] elems = h.getElements();
            if (elems.length > 0) {
                String contentType = elems[0].getName();
                return contentType.equalsIgnoreCase("application/x-www-form-urlencoded");
            }
        }
        return false;
    }

    public static void parse(List<NameValuePair> parameters, Scanner scanner, String charset) {
        parse(parameters, scanner, QP_SEP_PATTERN, charset);
    }

    public static void parse(List<NameValuePair> parameters, Scanner scanner, String parameterSepartorPattern, String charset) {
        scanner.useDelimiter(parameterSepartorPattern);
        while (scanner.hasNext()) {
            String name = null;
            String value = null;
            String token = scanner.next();
            int i = token.indexOf("=");
            if (i != -1) {
                name = decodeFormFields(token.substring(0, i).trim(), charset);
                value = decodeFormFields(token.substring(i + 1).trim(), charset);
            } else {
                name = decodeFormFields(token.trim(), charset);
            }
            parameters.add(new BasicNameValuePair(name, value));
        }
    }

    private static final char[] QP_SEPS = {'&', ';'};
    private static final String QP_SEP_PATTERN = "[" + new String(QP_SEPS) + "]";

    public static List<NameValuePair> parse(String s, Charset charset) {
        return parse(s, charset, QP_SEPS);
    }

    public static List<NameValuePair> parse(String s, Charset charset, char... parameterSeparator) {
        if (s == null) {
            return Collections.emptyList();
        }
        BasicHeaderValueParser parser = BasicHeaderValueParser.INSTANCE;
        CharArrayBuffer buffer = new CharArrayBuffer(s.length());
        buffer.append(s);
        ParserCursor cursor = new ParserCursor(0, buffer.length());
        List<NameValuePair> list = new ArrayList();
        while (!cursor.atEnd()) {
            NameValuePair nvp = parser.parseNameValuePair(buffer, cursor, parameterSeparator);
            if (nvp.getName().length() > 0) {
                list.add(new BasicNameValuePair(decodeFormFields(nvp.getName(), charset), decodeFormFields(nvp.getValue(), charset)));
            }
        }
        return list;
    }

    public static String format(List<? extends NameValuePair> parameters, String charset) {
        return format(parameters, '&', charset);
    }

    public static String format(List<? extends NameValuePair> parameters, char parameterSeparator, String charset) {
        StringBuilder result = new StringBuilder();
        for (NameValuePair parameter : parameters) {
            String encodedName = parameter.getName();
            String encodedValue = parameter.getName().equals("description") ? encodeFormFields(parameter.getValue().replace("\r", "").replace("&lt;br&gt;", "\n"), charset) : parameter.getValue();
            if (result.length() > 0) {
                result.append(parameterSeparator);
            }
            result.append(encodedName);
            if (encodedValue != null) {
                result.append("=");
                result.append(encodedValue);
            }
        }
        return result.toString();
    }

    public static String format(Iterable<? extends NameValuePair> parameters, Charset charset) {
        return format(parameters, '&', charset);
    }

    public static String format(Iterable<? extends NameValuePair> parameters, char parameterSeparator, Charset charset) {
        StringBuilder result = new StringBuilder();
        for (NameValuePair parameter : parameters) {
            String encodedName = parameter.getName();
            String encodedValue = parameter.getName().equals("description") ? encodeFormFields(parameter.getValue().replace("\r", "").replace("&lt;br&gt;", "\n"), charset) : parameter.getValue();
            if (result.length() > 0) {
                result.append(parameterSeparator);
            }
            result.append(encodedName);
            if (encodedValue != null) {
                result.append("=");
                result.append(encodedValue);
            }
        }
        return result.toString();
    }

    private static final BitSet UNRESERVED = new BitSet(256);
    private static final BitSet PUNCT = new BitSet(256);
    private static final BitSet USERINFO = new BitSet(256);
    private static final BitSet PATHSAFE = new BitSet(256);
    private static final BitSet URIC = new BitSet(256);
    private static final BitSet RESERVED = new BitSet(256);
    private static final BitSet URLENCODER = new BitSet(256);
    private static final int RADIX = 16;

    static {
        for (int i = 97; i <= 122; i++) {
            UNRESERVED.set(i);
        }
        for (int i = 65; i <= 90; i++) {
            UNRESERVED.set(i);
        }
        for (int i = 48; i <= 57; i++) {
            UNRESERVED.set(i);
        }
        UNRESERVED.set(95);
        UNRESERVED.set(45);
        UNRESERVED.set(46);
        UNRESERVED.set(42);
        URLENCODER.or(UNRESERVED);
        UNRESERVED.set(33);
        UNRESERVED.set(126);
        UNRESERVED.set(39);
        UNRESERVED.set(40);
        UNRESERVED.set(41);

        PUNCT.set(44);
        PUNCT.set(59);
        PUNCT.set(58);
        PUNCT.set(36);
        PUNCT.set(38);
        PUNCT.set(43);
        PUNCT.set(61);

        USERINFO.or(UNRESERVED);
        USERINFO.or(PUNCT);

        PATHSAFE.or(UNRESERVED);
        PATHSAFE.set(47);
        PATHSAFE.set(59);
        PATHSAFE.set(58);
        PATHSAFE.set(64);
        PATHSAFE.set(38);
        PATHSAFE.set(61);
        PATHSAFE.set(43);
        PATHSAFE.set(36);
        PATHSAFE.set(44);

        RESERVED.set(59);
        RESERVED.set(47);
        RESERVED.set(63);
        RESERVED.set(58);
        RESERVED.set(64);
        RESERVED.set(38);
        RESERVED.set(61);
        RESERVED.set(43);
        RESERVED.set(36);
        RESERVED.set(44);
        RESERVED.set(91);
        RESERVED.set(93);

        URIC.or(RESERVED);
        URIC.or(UNRESERVED);
    }

    private static String urlEncode(String content, Charset charset, BitSet safechars, boolean blankAsPlus) {
        if (content == null) {
            return null;
        }
        //content = removeHtmlSpecialCharacters(content);
        StringBuilder buf = new StringBuilder();
        ByteBuffer bb = charset.encode(content);
        while (bb.hasRemaining()) {
            int b = bb.get() & 0xFF;
            if (safechars.get(b)) {
                buf.append((char) b);
            } else if ((blankAsPlus) && (b == 32)) {
                buf.append('+');
            } else {
                buf.append("%");
                char hex1 = Character.toUpperCase(Character.forDigit(b >> 4 & 0xF, 16));
                char hex2 = Character.toUpperCase(Character.forDigit(b & 0xF, 16));
                buf.append(hex1);
                buf.append(hex2);
            }
        }
        return buf.toString();
    }

    private static String urlDecode(String content, Charset charset, boolean plusAsBlank) {
        if (content == null) {
            return null;
        }
        //content = removeHtmlSpecialCharacters(content);
        ByteBuffer bb = ByteBuffer.allocate(content.length());
        CharBuffer cb = CharBuffer.wrap(content);
        while (cb.hasRemaining()) {
            char c = cb.get();
            if ((c == '%') && (cb.remaining() >= 2)) {
                char uc = cb.get();
                char lc = cb.get();
                int u = Character.digit(uc, 16);
                int l = Character.digit(lc, 16);
                if ((u != -1) && (l != -1)) {
                    bb.put((byte) ((u << 4) + l));
                } else {
                    bb.put((byte) 37);
                    bb.put((byte) uc);
                    bb.put((byte) lc);
                }
            } else if ((plusAsBlank) && (c == '+')) {
                bb.put((byte) 32);
            } else {
                bb.put((byte) c);
            }
        }
        bb.flip();
        return charset.decode(bb).toString();
    }

    private static String decodeFormFields(String content, String charset) {
        if (content == null) {
            return null;
        }
        return urlDecode(content, charset != null ? Charset.forName(charset) : Consts.UTF_8, true);
    }

    private static String decodeFormFields(String content, Charset charset) {
        if (content == null) {
            return null;
        }
        return urlDecode(content, charset != null ? charset : Consts.UTF_8, true);
    }

    private static String encodeFormFields(String content, String charset) {
        if (content == null) {
            return null;
        }
        return urlEncode(content, charset != null ? Charset.forName(charset) : Consts.UTF_8, URLENCODER, true);
    }

    private static String encodeFormFields(String content, Charset charset) {
        if (content == null) {
            return null;
        }
        return urlEncode(content, charset != null ? charset : Consts.UTF_8, URLENCODER, true);
    }

    static String encUserInfo(String content, Charset charset) {
        return urlEncode(content, charset, USERINFO, false);
    }

    static String encUric(String content, Charset charset) {
        return urlEncode(content, charset, URIC, false);
    }

    static String encPath(String content, Charset charset) {
        return urlEncode(content, charset, PATHSAFE, false);
    }

    public static String removeHtmlSpecialCharacters(String text) {
        text = replaceAll(text, "&quot;", "\"");
        text = replaceAll(text, "&amp;", "&");
        text = replaceAll(text, "&rsquo;", "’");
        return text;
    }

    public static String replaceAll(String source, String pattern, String replacement) {
        if (source == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        int index;
        int patIndex = 0;
        while ((index = source.indexOf(pattern, patIndex)) != -1) {
            sb.append(source.substring(patIndex, index));
            sb.append(replacement);
            patIndex = index + pattern.length();
        }
        sb.append(source.substring(patIndex));
        return sb.toString();
    }
}
