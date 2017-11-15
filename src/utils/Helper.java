/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author dmitry
 */
public class Helper {

    public static List<String> cookieStr = new ArrayList<String>();
    static final java.util.logging.Logger LOG = Logger.global;
    public static final String any_patern = "(.*?)";
    private static final String tag_id_start = "id=" + Helper.any_space + "\"<id>\"";
    private static final String tag_end = "\"" + Helper.any_space + "value=\"";
    public static final String any_patern_tag = "([.[^><]])*";
    public static final String any_space = "([ \\t\\n\\r]*)";
    public static final String any_row_break = "([\\t\\n\\r]*)";
    private static final String[][] xmlsc = new String[][]{{"&", "&amp;"}, {"<", "&lt;"}, {">", "&gt;"}, {"\"", "&quot;"}, {"'", "&apos;"}};
    private static final String HTTP503 = "HTTP response code: 503";
    private static final String _session = "";
    static public final String baseURL = "https://www.graysonline.com";
    static private final String loginURL = "https://www.graysonline.com/login.aspx?el=1&ReturnUrl=%2f";
    static private final String login = "https://www.graysonline.com/?login=1";
    public static final String mygrase = "https://www.graysonline.com/mygrays/auctions/biddingon.aspx";

    public static final CookieStore cookieStore = new BasicCookieStore();
    public static final HttpClientContext context = HttpClientContext.create();

    static CloseableHttpClient httpClient = null;

    private static final boolean init = false;
    private static final bidder.ItemBidder itemBidder = new bidder.ItemBidder();

    static {
        context.setCookieStore(cookieStore);
        itemBidder.httpclient = HttpClients.custom().setDefaultRequestConfig(itemBidder.config).setDefaultCookieStore(cookieStore).build();

        //httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
    }

    public static String padRight(String s, int n) {
        return String.format("%1$-" + n + "s", s);
    }

    public static String padLeft(String s, int n) {
        return String.format("%1$" + n + "s", s);
    }

    public static String removeAnyLineBreakSymbols(String str) {
        return str.replaceAll(any_row_break, "");
    }

    public static List<String> split(List<String> strs, String tag1, String tag2) {
        List<String> list = new ArrayList<>();
        for (String str : strs) {
            list.addAll(split(str, tag1, tag2));
        }
        return list;
    }

    public static String mergeList(String[] list) {
        StringBuilder sb = new StringBuilder();
        for (String item : removeEmpty(list)) {
            if (sb.toString().length() > 0) {
                sb.append(",");
            }
            sb.append(item);
        }
        return sb.toString();
    }

    public static List<String> split(String str, String tag1, String tag2) {
        tag1 = removeRegEx(tag1);
        tag2 = removeRegEx(tag2);
        //str = removeAnyLineBreakSymbols(str);

        List<String> list = new ArrayList<String>();
        int start = 0;
        while (start > -1 && str.contains(tag1) && !str.equals(tag1)) {
            start = str.indexOf(tag1);
            str = str.substring(start);
            start = 0;
            String str1 = str;
            start = str.indexOf(tag2, start + tag1.length());
            while (start > -1 && str1.contains(tag2)) {
                str1 = str.substring(0, start + tag2.length());
                if (!getTagName(tag2).isEmpty() && (count(str1, "<" + getTagName(tag1))) > count(str1, "</" + getTagName(tag2) + ">")) {
                    start = str.indexOf(tag2, start) + tag2.length();
                    str1 = str.substring(0, start);
                    continue;
                }
                list.add(str1);
                str = str.substring(start + tag2.length());
                str1 = str;
                break;
            }
        }
        return list;
    }

    public static String replaceXmlSpecChars(String xml) {
        for (String[] specs : xmlsc) {
            xml = xml.replace(specs[1], specs[0]);
        }
        return xml;
    }

    public static String getNormalisedString(String str) {
        if (str == null) {
            return null;
        }
        str = replaceXmlSpecChars(str.trim().replace("\n", "").replace("\r", "").replace("\t", "")).toUpperCase();

        return str.isEmpty() ? null : str;
    }

    public static String removeRegEx(String str) {
        return str.replace(any_patern, "").replace(any_patern_tag, "").replace(any_row_break, "").replace(any_space, "");
    }

    public static String getTagName(String tag) {
        String name = "";
        boolean started = false;
        tag = tag.trim();
        tag = tag.substring(tag.indexOf("<") + 1);
        for (Character c : tag.toCharArray()) {
            if (!c.equals(" ") && (Character.isLetterOrDigit(c) || c.toString().matches("[-_]"))) {
                name += c.toString();
                started = true;
            } else {
                if (started) {
                    break;
                }
            }
        }
        return name;
    }

    public static Node getNodeByText(String xml, String text) {
        try {
            Document doc = Helper.loadXMLFromString(xml);
            return getNodeByText(doc.getFirstChild(), text);
        } catch (Exception ex) {
        }
        return null;
    }

    public static String getBodyTagTextById(String html, String id) {
        try {
            List<String> list = Helper.splitRegEx(html, tag_id_start.replace("<id>", id), "\"");
            return list.size() > 0 ? list.get(0).replace("value=\"", "").trim() : "";
        } catch (Exception ex) {
            return null;
        }
    }

    public static Node getNodeByText(Node node, String text) throws Exception {
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node node1 = getNodeByText(node.getChildNodes().item(i), text);
            if (node1 != null) {
                return node1;
            }
            if (node.getChildNodes().item(i).getTextContent() != null && node.getChildNodes().item(i).getTextContent().trim().startsWith(text)) {
                Node sibling = node.getChildNodes().item(i).getNextSibling();
                while (!(sibling instanceof Element) && sibling != null) {
                    sibling = sibling.getNextSibling();
                }
                return sibling;
            }
        }
        return null;
    }

    public static int count(String str, String tag) {
        int i = 0;
        while (str.contains(tag)) {
            int start = str.indexOf(tag) + tag.length();
            str = str.substring(start);
            i++;
        }
        return i;
    }

    public static int count(List list) {
        return list == null ? 0 : list.size();
    }

    public static String splitTopLevel(String str, String tag1, String tag2) {
        List<String> list = split(str, tag1, tag2);
        return list.size() > 0 ? list.get(0) : null;
    }

    public static boolean isNumeric(byte str) {
        int i = (int) str;
        return (i >= 48 && i <= 57) || (i == 46);
    }

    public static boolean isNumber(byte str) {
        int i = (int) str;
        return (i >= 48 && i <= 57);
    }

    public static List<String> splitRegEx(String str, String tag1, String tag2) {
        return splitRegEx(str, tag1, tag2, false);
    }

    public static Document loadXMLFromString(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(false);
        DocumentBuilder builder = factory.newDocumentBuilder();

        return builder.parse(new ByteArrayInputStream(xml.getBytes()));
    }

    public static synchronized Date getDate(String text) {
        Date date = new Date();
        String[] dd = text.split(" ");
        /*  try {
         TimeZone.setDefault(TimeZone.getTimeZone(dd[dd.length - 1]));
         text = text.replace(dd[dd.length - 1], "");
         } catch (Exception ex) {
         }
         */
        try {
            date = new Date(text);
        } catch (Exception ex) {

        }
        return date;
    }

    public static String getTagText(String text) {
        if (text != null) {
            text = text.substring(text.indexOf(">") + 1);
            int i = text.indexOf("</");
            text = text.substring(0, i < 0 ? 0 : i).trim();
            if (text.contains(">")) {
                i = text.lastIndexOf(">") + 1;
                text = text.substring(i > text.length() ? text.length() : i);
            }
            if (text.contains("<")) {
                i = text.lastIndexOf("<");
                text = text.substring(0, i < 0 ? 0 : i);
            }
        }
        return text;
    }

    public static List<String> getNonEmptyList(List<String> list) {
        List<String> arr = new ArrayList<String>();
        for (String s : list) {
            if (!s.trim().isEmpty()) {
                arr.add(s);
            }
        }
        return arr;
    }

    public static Double getMoney(String text) {
        String ret = "0";
        boolean started = false;
        if (text != null) {
            for (Byte b : text.getBytes()) {
                if (isNumeric(b)) {
                    started = true;
                    ret += String.valueOf((char) b.byteValue());
                } else if (started && !b.equals(",".getBytes()[0])) {
                    break;
                }
            }
            return Double.parseDouble(ret);
        }
        return null;
    }

    public static String exceptionToString(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    public static Integer getYear(String text) {
        String ret = "";
        boolean started = false;
        if (text != null) {
            try {
                return new Date(text).getYear();
            } catch (Exception ex) {
            }
            for (Byte b : text.getBytes()) {
                if (isNumber(b) && ret.length() < 4) {
                    started = true;
                    ret += String.valueOf((char) b.byteValue());
                } else {
                    if (started && isYear(ret)) {
                        return Integer.parseInt(ret);
                    } else {
                        ret = "";
                        started = false;
                    }
                }

            }
        }

        return isYear(ret) ? Integer.parseInt(ret) : null;
    }

    public static boolean isYear(String text) {
        int year = Integer.parseInt(new SimpleDateFormat("yyyy").format(new Date()));
        if (text != null) {
            try {
                if (text.length() == 2 && Integer.parseInt(text) > 70) {
                    return true;
                }
                if (text.length() == 4 && Integer.parseInt(text) <= year && Integer.parseInt(text) >= 1970) {
                    return true;
                }
            } catch (NumberFormatException ex) {
            }
        }
        return false;
    }

    public static Long getLong(String text) {
        String ret = "0";
        for (byte b : text.getBytes()) {
            if ("<>".contains(String.valueOf((char) b))) {
                break;
            }
            if (!String.valueOf((char) b).equals(".") && isNumeric(b)) {
                ret += String.valueOf((char) b);
            }
        }
        if (ret.endsWith(".")) {
            ret = ret.substring(0, ret.length() - 1);
        }
        return Long.parseLong(ret);
    }

    static void printParams(HttpPost m) {

    }

    public static HttpResponse login() throws Exception {
        String res = null;

        HttpRequestBase methord = null;

        //HttpPost.setParameter("__EVENTARGUMENT", "");
        //HttpPost.setParameter("__EVENTTARGET", "");
        //setCookies();
        try {
            methord = new HttpPost(loginURL);
            HttpResponse r = executeMethod(methord);
            return r;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void printcookies() {
        for (Cookie c : context.getCookieStore().getCookies()) {
            System.out.println(c.toString());
        }
        System.out.println("--------------------------------------------");
    }

    public static void setAuthCookies() {
        if (itemBidder.AuthCookie == null) {
            for (Cookie c : context.getCookieStore().getCookies()) {
                if (c.getName().equals(".AuthCookie")) {
                    itemBidder.AuthCookie = c;
                }
            }
        }
    }

    public static boolean containsAuthCookie() {
        if (itemBidder.AuthCookie != null) {
            for (Cookie c : context.getCookieStore().getCookies()) {
                if (c.getName().equals(itemBidder.AuthCookie.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void addAuthCookie() {
        if (!containsAuthCookie()) {
            if (itemBidder.AuthCookie != null) {
                context.getCookieStore().addCookie(itemBidder.AuthCookie);
                System.out.println("Added AuthCookie");
            }
        }
    }

    public static HttpResponse executeMethod(HttpRequestBase method2) throws IOException {

        addAuthCookie();
        HttpResponse r = httpClient.execute(method2, context);
        System.out.println("URL: " + method2.getURI().toString());
        printcookies();
        if (method2 instanceof HttpPost) {
            setAuthCookies();
        }
        return follow(r);

    }

    static HttpGet getHttpGet(String url) {
        HttpGet HttpGet = new HttpGet(url);
        return HttpGet;
    }

    static HttpResponse follow(HttpResponse method) throws IOException {
        Header[] locationHeader = method.getHeaders("location");
        if (locationHeader == null) {
            return method;
        } else {
            while (locationHeader != null) {
                HttpResponse r = executeMethod(getHttpGet(baseURL + locationHeader[0].getValue()));

                locationHeader = r.getHeaders("location");
            }
            return method;
        }
    }

    public static String getHttpPage(String uri) throws IOException, InterruptedException {
        int i = 0;
        while (i++ < 3) {
            try {
                URL url = new URL(uri);
                URLConnection con = url.openConnection();
                InputStream in = con.getInputStream();
                String encoding = con.getContentEncoding();
                encoding = encoding == null ? "UTF-8" : encoding;
                return getStringFromInputStream(in);
            } catch (IOException ex) {
                if (!ex.getMessage().contains(HTTP503)) {
                    throw ex;
                }
                LOG.log(Level.SEVERE, String.format("###Error retreving page by URL: %s\n%s", uri, ex.getMessage()));
                Thread.sleep(1000);
            }
        }
        return null;
    }

    public static byte[] loadStream(InputStream stream) throws IOException {
        int available = stream.available();
        int expectedSize = available > 0 ? available : -1;
        return loadStream(stream, expectedSize);
    }

    public static String loadFromFile(String path) throws Exception {
        return new String(Helper.loadStream(new FileInputStream(path)));
    }

    public static Map<String, String> loadMapFromString(String s) {
        Map<String, String> map = new HashMap();
        for (String line : s.replace("\r", "").split("\n")) {
            map.put(line.split("=")[0], line.split("=")[1]);
        }
        return map;
    }

    private static byte[] loadStream(InputStream stream, int expectedSize) throws IOException {
        int basicBufferSize = 0x4000;
        int initialBufferSize = (expectedSize >= 0) ? expectedSize : basicBufferSize;
        byte[] buf = new byte[initialBufferSize];
        int pos = 0;
        try {
            while (true) {
                if (pos == buf.length) {
                    int readAhead = -1;
                    if (pos == expectedSize) {
                        readAhead = stream.read();       // test whether EOF is at expectedSize
                        if (readAhead == -1) {
                            return buf;
                        }
                    }
                    int newBufferSize = Math.max(2 * buf.length, basicBufferSize);
                    buf = Arrays.copyOf(buf, newBufferSize);
                    if (readAhead != -1) {
                        buf[pos++] = (byte) readAhead;
                    }
                }
                int len = stream.read(buf, pos, buf.length - pos);
                if (len < 0) {
                    return Arrays.copyOf(buf, pos);
                }
                pos += len;
            }
        } finally {
            stream.close();
        }
    }

    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }

    public static List<String> splitRegEx(String str, String tag1, String tag2, boolean includeTag) {
        List<String> list = new ArrayList<String>();
        Pattern p = Pattern.compile(tag1 + (tag2.startsWith("</") ? any_patern : any_patern_tag) + tag2);
        Matcher m = p.matcher(str);
        while (m.find()) {
            String s = m.group();
            if (!includeTag) {
                s = s.substring(Helper.removeRegEx(tag1).length());
                s = s.substring(0, s.length() - Helper.removeRegEx(tag2).length());
                if (s.startsWith(">")) {
                    s = s.substring(1);
                }
            }
            list.add(s);
        }
        return list;
    }

    public static String compress(String str) throws Exception {
        if (str == null || str.length() == 0) {
            return str;
        }
        System.out.println("String length : " + str.length());
        ByteArrayOutputStream obj = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(obj);
        gzip.write(str.getBytes("UTF-8"));
        gzip.close();
        String outStr = obj.toString("UTF-8");
        System.out.println("Output String length : " + outStr.length());
        return outStr;
    }

    public static String decompress(byte[] str) throws Exception {

        GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(str));
        BufferedReader bf = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
        String outStr = "";
        String line;
        while ((line = bf.readLine()) != null) {
            outStr += line;
        }
        System.out.println("Output String lenght : " + outStr.length());
        return outStr;
    }

    public static String replaceAllBetweenTags(String text) {
        return text.replaceAll("<[/]*[0-9,a-z,A-Z,\\s,\\t,\\r,=,\",\',%,\\.]*>", "");
    }

    public static String getFirstRegExMath(String str, String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);
        while (m.find()) {
            return m.group();
        }
        return null;
    }

    public static String getHash(String[] list) {
        return getHash(Arrays.asList(list));
    }

    public static String getHash(List<String> list) {
        String str = "";
        for (String str1 : list) {
            str = str + "/" + getNormalisedString(str1);
        }
        return String.valueOf(str.substring(1).hashCode());
    }

    public static List<String> getFirstChildTagText(List<String> nodes, String tag) {
        List<String> list = new ArrayList<String>();
        for (String node : nodes) {
            list.add(new bidder.HtmlParser(node).getNamedTagText(tag));
        }
        return list;
    }

    public static List<String> getRegExMath(String str, String regex) {
        List<String> list = new ArrayList<String>();
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);
        while (m.find()) {
            list.add(m.group());
        }
        return list;
    }

    public static List<String> removeNonURL(List<String> l) {
        List<String> ret = new ArrayList<String>();
        if (l != null) {
            for (String s : l) {
                try {
                    new URL(s);
                    ret.add(s);
                } catch (Exception e) {
                }
            }
        }
        return ret;
    }

    public static String[] removeEmpty(String[] l) {
        List<String> ret = new ArrayList<String>();
        if (l != null) {
            for (String s : l) {
                if (s != null && !s.isEmpty()) {
                    ret.add(s);
                }
            }
        }
        return ret.toArray(new String[]{});
    }

    public static String excetionToString(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }

    public static synchronized byte[] getImage(String uri) throws IOException {
        try {
            URL url = new URL(uri);
            InputStream in = new BufferedInputStream(url.openStream());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int n = 0;
            while (-1 != (n = in.read(buf))) {
                out.write(buf, 0, n);
            }
            out.close();
            in.close();
            return out.toByteArray();
        } catch (MalformedURLException ex) {
            LOG.log(Level.SEVERE, String.format("url: %s is invalid", uri));
        }
        return null;
    }

    public static Date getDateInfuture(String text) {
        Calendar cal = Calendar.getInstance(); // creates calendar
        cal.setTime(new Date()); // sets calendar time/date
        if (text.contains("hrs")) {
            cal.add(Calendar.HOUR, Integer.parseInt(text.substring(0, text.indexOf("hrs") - 1))); // adds one hour
            text = text.substring(text.indexOf("hrs") + 3).trim();
        }
        if (text.contains("hr")) {
            cal.add(Calendar.HOUR, Integer.parseInt(text.substring(0, text.indexOf("hr") - 1))); // adds one hour
            text = text.substring(text.indexOf("hr") + 2).trim();
        }
        if (text.contains("mins")) {
            cal.add(Calendar.MINUTE, Integer.parseInt(text.substring(0, text.indexOf("mins") - 1))); // adds one hour
            text = text.substring(text.indexOf("mins") + 4).trim();
        }
        if (text.contains("min")) {
            cal.add(Calendar.MINUTE, Integer.parseInt(text.substring(0, text.indexOf("min") - 1))); // adds one hour
            text = text.substring(text.indexOf("min") + 3).trim();
        }
        return cal.getTime(); // returns new date object, one hour in the future
    }

    public static int getIndexWithTextContains(List<String> list, String text) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).contains(text)) {
                return i;
            }
        }
        return -1;
    }

    public static String replace(String text, String source, String target) {
        for (String s : source.split("\\|")) {
            text = text.replace(s, target);
        }
        return text;
    }

    public static Map<String, String> StringToMap(String str) {
        Map<String, String> map = new HashMap<String, String>();
        for (String s : str.split(";")) {
            if (s.contains("=")) {
                map.put(s.split("=")[0], s.split("=")[1]);
            }
        }
        return map;
    }
    
    public static Map<String, String> stringToMap(String str) {
        Map<String, String> map = new HashMap<String, String>();
        for (String s : str.split(";")) {
            if (s.contains("=")) {
                map.put(s.split("=")[0], s.split("=")[1]);
            }
        }
        return map;
    }
    
    public synchronized static String getStackTrace() {
        StringWriter sw = new StringWriter();
        new Throwable().printStackTrace(new PrintWriter(sw));
        String ret = sw.toString();
        return ret;
    }

    static public String join(List<String> list, String conjunction) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String item : list) {
            if (first) {
                first = false;
            } else {
                sb.append(conjunction);
            }
            if (item.startsWith(".") && (item.split("=").length == 1 || item.split("=")[1].startsWith(";"))) {
            } else {
                sb.append(item.split(";", 2)[0]);
            }
        }
        return sb.toString();
    }

    private static synchronized HttpUriRequest getRequest(String type, URI url) throws Exception {
        HttpUriRequest req = null;
        if (type.equalsIgnoreCase("get")) {
            req = new HttpGet(url);
        } else if (type.equalsIgnoreCase("post")) {
            req = new HttpPost(url);
        } else if (type.equalsIgnoreCase("put")) {
            req = new HttpPut(url);
        } else {
            req = new HttpDelete(url);
        }
        /*
         req.addHeader("Accept", "text/html");
         req.addHeader("Accept-Language", "en-us,en;q=0.5");
         req.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0");
         */
        //   req.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        /*    req.addHeader("Accept-Language", "en-US,en;q=0.5");
         req.addHeader("Accept-Encoding", "gzip, deflate");
         req.addHeader("Referer", "https://signin.ebay.com.au/ws/eBayISAPI.dll?SignIn");
         */
        req.addHeader("Cookie", join(cookieStr, "\r\n"));
        return req;

    }

    static CookieStore getBasicHttpContext() {
        CookieStore cookieStore1 = new BasicCookieStore();
        for (Cookie k : cookieStore.getCookies()) {
            if ("/".equals(k.getPath())) {
                cookieStore1.addCookie(k);
            }
        }
        return cookieStore1;
    }

    public static synchronized String executeRequest(String uri, String type, String command) throws Exception {

        HttpUriRequest req = getRequest(type, new URI(uri));

        if (command != null) {
            StringEntity entity = new StringEntity((String) command);
            ((HttpEntityEnclosingRequestBase) req).setEntity(entity);
        }

        HttpContext localContext = new BasicHttpContext();

        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        HttpResponse response = itemBidder.httpclient.execute(req, localContext);
        String out = "Error occured";

        for (Header header : response.getHeaders("Set-Cookie")) {
            String headers[] = header.getValue().split(";");
            BasicClientCookie cookie = new BasicClientCookie(headers[0].split("=")[0], headers[0].split("=").length > 1 ? headers[0].split("=")[1] : "");
            for (String h : headers) {
                if (h.trim().startsWith("Domain")) {
                    cookie.setDomain(h.split("=")[1]);
                } else if (h.trim().startsWith("Path")) {
                    cookie.setPath(h.split("=")[1]);
                }
                if (cookieStr.contains(header.getValue())) {
                    cookieStr.remove(header.getValue());
                }
                cookieStr.add(header.getValue());
            }
            cookieStore.addCookie(cookie);
        }

        if (response.getEntity() != null) {
            out = new String(loadStream(response.getEntity().getContent()));
            response.getEntity().consumeContent();
        }

        return out;
    }

    public static HttpResponse setCookies(HttpResponse response, bidder.ItemBidder itemBidder) {
        for (Header header : response.getHeaders("Set-Cookie")) {
            String headers[] = header.getValue().split(";");
            BasicClientCookie cookie = new BasicClientCookie(headers[0].split("=")[0], headers[0].split("=").length > 1 ? headers[0].split("=")[1] : "");
            for (String h : headers) {
                if (h.trim().startsWith("Domain")) {
                    cookie.setDomain(h.split("=")[1]);
                } else if (h.trim().startsWith("Path")) {
                    cookie.setPath(h.split("=")[1]);
                }
                if (cookieStr.contains(header.getValue())) {
                    cookieStr.remove(header.getValue());
                }
                cookieStr.add(header.getValue());
            }
            cookieStore.addCookie(cookie);
            //ib.cookieStore.addCookie(cookie);
        }
        return response;
    }

    public static String saveBytesToFile(String path, byte[] data) throws IOException {
        System.out.println(String.format("Saving bytes to a file: %s", path));
        new File(path).getParentFile().mkdirs();
        FileOutputStream stream = new FileOutputStream(path);
        try {
            stream.write(data);
        } finally {
            stream.close();
        }
        return path;
    }

}
