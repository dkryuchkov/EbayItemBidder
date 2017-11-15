/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bidder;

import com.intforce.db.entity.JpaEntity;
import com.intforce.html.parsers.HtmlParser;
import com.intforce.html.parsers.EbayItemParser;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import javax.json.Json;
import javax.json.JsonObject;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author dmitry
 */
public class Helper {

    private static final String itemID = "{\"itemId\" : \"<id>\"}";
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Helper.class);
    public static final String any_patern = "(.*?)";
    public static final String any_patern_tag = "([.[^><]])*";
    public static final String any_patern_tag1 = "([.[^<]])*";
    public static final String any_space = "([ \\t\\n\\r]*)";
    public static final String any_row_break = "([\\t\\n\\r]*)";
    private static final String[][] xmlsc = new String[][]{{"&", "&amp;"}, {"<", "&lt;"}, {">", "&gt;"}, {"\"", "&quot;"}, {"'", "&apos;"}};
    private static final String HTTP503 = "HTTP response code: 503";
    private static final String allHtmlNewLineTags = "<[/]*[br,p,li]{1}" + Helper.any_patern_tag + "[/]*>";
    public static final String splitChar = "|";

    public static String removeAnyLineBreakSymbols(String str) {
        return str.replaceAll(any_row_break, "");
    }

    public static String lastStringSplitValue(String str, String val) {
        List<String> list = Arrays.asList(str.split(val));
        return list == null ? null : (list.size() == 0 ? null : list.get(list.size() - 1));
    }

    public static List<String> split(List<String> strs, String tag1, String tag2) {
        List<String> list = new ArrayList<String>();
        for (String str : strs) {
            list.addAll(split(str, tag1, tag2));
        }
        return list;
    }

    public static String[] convertToStringArray(Object array) throws Exception {
        return StringUtils.stripAll(array.toString().substring(1, array.toString().length() - 1).split(","));
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

    public static List<String> split(String text, String tag1, String tag2) {
        return split(text, tag1, tag2, false);
    }

    private synchronized static String fixBrokenNewLine(String s, String c) {
        if (s.length() > 2 && Character.isLetter(s.charAt(0)) && Character.isUpperCase(s.charAt(0))) {
            s = "\n" + s;
        }
        s = c + s;

        return s;
    }

    public static String fixEmailBrokenLine(String s) {
        Matcher m = Pattern.compile("[a-zA-Z0-9_.+-]+[.,@]{1}[a-zA-Z0-9-]+\\.[a-z0-9-.]{2,6}+").matcher(s);
        int i = 0;
        while (m.find()) {
            i = s.indexOf(m.group(), i);
            String s1 = s.substring(i + m.group().length());
            s = s.substring(0, i) + m.group();
            if (s1.length() > 2 && Character.isLetter(s1.charAt(0)) && Character.isUpperCase(s1.charAt(0))) {
                s += "\n";
            }
            s += s1;
            i++;
        }
        return s;
    }

    public static String fixEndOfSentance(String text) {
        for (String s : ".!?".split("|")) {
            text = fixEndOfSentanceAnyOf(text, s);
        }
        text = fixEmailBrokenLine(text);
        return text.length() > 0 && text.startsWith("\n") ? text.substring(1) : text;
    }

    public static String fixEndOfSentanceAnyOf(String text, String c) {
        String out = "";
        String str1 = text;
        while (str1.contains(c)) {
            String s = str1.substring(0, str1.indexOf(c));
            out += fixBrokenNewLine(s, c);
            str1 = str1.substring(str1.indexOf(c) + 1);
        }

        out += fixBrokenNewLine(str1, c);
        return out.length() > 0 ? out.substring(1) : out;
    }

    public static List<String> split(String text, String tag1, String tag2, boolean includeTags) {
        tag1 = removeRegEx(tag1);
        tag2 = removeRegEx(tag2);

        String str = text; //removeAnyLineBreakSymbols(text);

        List<String> list = new ArrayList<String>();
        while (str.contains(tag1) && !str.equals(tag1)) {
            int start = str.indexOf(tag1);
            str = str.substring(start);
            start = 0;
            String str1 = str;
            start = str.indexOf(tag2, start + tag1.length());
            while (str1.contains(tag2)) {
                str1 = str.substring(0, start + tag2.length());
                if (!getTagName(tag2).isEmpty() && (count(str1, "<" + getTagName(tag1) + " |<" + getTagName(tag1) + ">")) > count(str1, tag2)) {
                    int start1 = start;
                    start = str.indexOf(tag2, start + tag2.length());
                    if (start > start1) {
                        str1 = str.substring(0, start + tag2.length());
                        continue;
                    } else {
                        str1 = str1;
                    }
                }
                if (str1.contains(tag1)) {
                    str1 = str1.substring(str1.indexOf(tag1) + tag1.length());
                    if (!getTagName(tag2).isEmpty() && str1.contains(">") && str1.substring(str1.indexOf(">") + 1).contains(tag2)) {
                        if (!includeTags) {
                            str1 = str1.substring(str1.indexOf(">") + 1);
                        } else if (!str1.startsWith(tag1)) {
                            str1 = tag1 + str1;
                        }
                    }
                }
                if (str1.length() > tag2.length()) {
                    list.add(str1.substring(0, str1.length() - (includeTags ? 0 : tag2.length())));
                } else {
                    list.add(str1);
                }
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

    public static JsonObject parseJson(String str) {
        JsonObject obj = null;
        try {
            if (str != null && !str.isEmpty()) {
                obj = Json.createReader(new StringReader(str)).readObject();
            } else {
                LOG.error(String.format("###Error: parseJson() string is null or empty=%s", str));
            }
        } catch (Exception ex) {
            LOG.error(String.format("###Error: %s", exceptionToString(ex)));
        }
        return obj;
    }

    public static String replaceAllHtmlNewLineTags(String str, String with) {
        String ret = str.replaceAll(allHtmlNewLineTags, with).trim();
        while (ret.contains(with + with)) {
            ret = ret.replace(with + with, with);
        }
        return ret.startsWith(splitChar) ? ret.substring(1) : ret;
    }

    public static String getNormalisedString(String str) {
        if (str == null) {
            return "";
        }
        str = replaceXmlSpecChars(str.trim().replace("\n", "").replace("\r", "").replace("\t", "")).toUpperCase();
        str = StringEscapeUtils.unescapeHtml(str);
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
            } else if (started) {
                break;
            }
        }
        return name;
    }

    public static String getStringFromNode(org.w3c.dom.Node doc) {
        DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getOwnerDocument().getImplementation();
        LSSerializer lsSerializer = domImplementation.createLSSerializer();
        lsSerializer.getDomConfig().setParameter("xml-declaration", false);
        return lsSerializer.writeToString(doc);
    }

    public static Node getNodeByText(String xml, String text) {
        try {
            Document doc = Helper.loadXMLFromString(xml);
            return getNodeByText(doc.getFirstChild(), text);
        } catch (Exception ex) {
        }
        return null;
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

    public static int count(String text, String tag1) {
        int i = 0;
        for (String tag : tag1.split("\\|")) {
            String str = text;
            while (str.contains(tag)) {
                int start = str.indexOf(tag) + tag.length();
                str = str.substring(start);
                i++;
            }
        }
        return i;
    }

    public static int count(List list) {
        return list == null ? 0 : list.size();
    }

    public static String splitTopLevelLast(String str, String tags1, String tags2) {
        String text = null;
        int i = 0;
        for (String tag1 : tags1.split("\\|")) {
            String[] tag2s = tags2.split("\\|");
            String tag2 = tag2s.length > i ? tag2s[i++] : tag2s[tag2s.length - 1];
            List<String> list = split(str, tag1, tag2);
            text = list.size() > 0 ? list.get(list.size() - 1) : null;
            if (text != null) {
                return text;

            }
        }
        return text;
    }

    public static String splitTopLevel(String str, String tags1, String tags2) {
        String text = null;
        int i = 0;
        for (String tag1 : tags1.split("\\|")) {
            String[] tag2s = tags2.equals(splitChar) ? new String[]{splitChar} : tags2.split("\\|");
            String tag2 = tag2s.length > i ? tag2s[i++] : tag2s[tag2s.length - 1];
            List<String> list = split(str, tag1, tag2);
            text = list.size() > 0 ? list.get(0) : null;
            if (text != null) {
                break;

            }
        }
        return text != null && text.endsWith("<") ? text.substring(0, text.length() - 1) : text;
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

    public static boolean containsAny(String name, String[] array) {
        for (String val : array) {
            if (name.toUpperCase().contains(val.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isDate(String str) {
        try {
            new SimpleDateFormat("dd/MM/yyyy").parse(str);
        } catch (ParseException ex) {
            return false;
        }
        return true;
    }

    public static String getJsonValue(JsonObject json, String analytics_MakeAnalytics_Brand) {
        if (json != null) {
            for (String key : analytics_MakeAnalytics_Brand.split(",")) {
                if (json.get(key) != null && !json.get(key).toString().equals("\"\"")) {
                    return json.get(key).toString().substring(1).substring(0, json.get(key).toString().length() - 2);
                }
            }
        }
        return null;
    }

    private static class MyErrorHandler implements ErrorHandler {

        public void warning(SAXParseException spe) throws SAXException {
            // System.out.println("Warning: " + spe.getMessage() + " getColumnNumber is " + spe.getColumnNumber() + " getLineNumber " + spe.getLineNumber() + " getPublicId " + spe.getPublicId() + " getSystemId " + spe.getSystemId());
        }

        public void error(SAXParseException spe) throws SAXException {
            // System.out.println("Error: " + spe.getMessage() + " getColumnNumber is " + spe.getColumnNumber() + " getLineNumber " + spe.getLineNumber() + " getPublicId " + spe.getPublicId() + " getSystemId " + spe.getSystemId());
            // throw new SAXException("Error: " + spe.getMessage());
        }

        public void fatalError(SAXParseException spe) throws SAXException {
            // System.out.println("Fatal Error:  " + spe.getMessage() + " getColumnNumber is " + spe.getColumnNumber() + " getLineNumber " + spe.getLineNumber() + " getPublicId " + spe.getPublicId() + " getSystemId " + spe.getSystemId());
            // throw new SAXException("Fatal Error: " + spe.getMessage());
        }
    }

    public static Document loadXMLFromString(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(false);
        MyErrorHandler errorHandler = new MyErrorHandler();
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setErrorHandler(errorHandler);
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
        if (text != null && text.contains(">") && text.contains("</")) {
            text = text.substring(text.indexOf(">") + 1);
            int i = text.indexOf("</");
            text = text.substring(0, i < 0 ? text.length() : i).trim();
            if (text.contains(">")) {
                i = text.lastIndexOf(">") + 1;
                text = text.substring(i > text.length() ? text.length() : i);
            }
            if (text.contains("<")) {
                i = text.lastIndexOf("<");
                text = text.substring(0, i < 0 ? 0 : i);
            }
        } else if (text != null && text.contains(">")) {
            text = text.substring(text.indexOf(">") + 1);
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

    public static Double getMoney(int text) {
        return getMoney(String.valueOf(text));
    }

    public static Double getMoney(String text) {
        String ret = "0";
        boolean started = false;
        if (text != null) {
            for (Byte b : text.getBytes()) {
                if (isNumeric(b)) {
                    started = true;
                    String value = String.valueOf((char) b.byteValue());
                    ret += ret.contains(".") && value.equals(".") ? "" : value;
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

    public static boolean isStringIsNumber(String text) {
        for (Byte b : text.getBytes()) {
            if (!isNumeric(b)) {
                return false;
            }
        }
        return true;
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
                } else if (started && isYear(ret)) {
                    return Integer.parseInt(ret);
                } else {
                    ret = "";
                    started = false;
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

    public synchronized static boolean isPageExists(String uri, String id) throws IOException, InterruptedException {
        return getHttpPage(uri).contains(itemID.replace("<id>", id));
    }

    public static String getHttpPage(String uri) throws IOException, InterruptedException {
        LOG.info(String.format("###Pulling uri=%s", uri));
        int i = 0;
        HttpURLConnection con = null;
        while (i++ < 3) {
            try {
                URL url = new URL(uri);
                con = (HttpURLConnection) url.openConnection();
                if (con.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM
                        || con.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
                    String location = con.getHeaderField("location");
                    if (!location.startsWith("http")) {
                        if (!location.startsWith("/")) {
                            location = "/" + location;
                        }
                        location = url.getProtocol() + "://" + url.getHost() + location;
                    }
                    return getHttpPage(location);
                } else {
                    InputStream in = con.getInputStream();
                    String encoding = con.getContentEncoding();
                    encoding = encoding == null ? "UTF-8" : encoding;
                    return getStringFromInputStream(in);
                }
            } catch (IOException ex) {
                if (!ex.getMessage().contains(HTTP503)) {
                    throw ex;
                }
                LOG.error(String.format("###Error retreving page by URL: %s\n%s", uri, ex.getMessage()));
                Thread.sleep(1000);
            } finally {
                if (con != null) {
                    con.disconnect();
                }
            }
        }
        return null;
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

    public static List<String> splitRegEx(String str, String tag11, String tag22, boolean includeTag) {
        List<String> list = new ArrayList<String>();
        for (String tag1 : tag11.split("\\|")) {
            for (String tag2 : tag22.split("\\|")) {
                Pattern p = Pattern.compile(tag1 + (tag2.startsWith(EbayItemParser.item_end) ? any_patern_tag1 : any_patern_tag) + tag2);
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
                if (list.size() > 0) {
                    return list;
                }
            }
        }
        return list;
    }

    /*
     public static String getTag(String html,String tag)
     {
     String str = html;
     int i=1;
     while(str.indexOf("</" + tag, i) > -1)
     {
     i =str.indexOf("</" + tag, i);
     if(count(str, tag)str.substring(0,i).contains("<" + tag))
     }
     return str;
     }

     public static List<String> splitRegExGetTagHtml(String str, String tag11, String tag22, boolean includeTag) {
     List<String> list = new ArrayList<String>();
     for (String tag1 : tag11.split("\\|")) {
     for (String tag2 : tag22.split("\\|")) {
     Pattern p = Pattern.compile(tag1 + (tag2.startsWith(EbayItemParser.item_end) ? any_patern : any_patern_tag) + tag2);
     Matcher m = p.matcher(str);
     while (m.find()) {
     String s = m.group();
     str = str.substring(str.indexOf(s));
     str = getTag(str,tag2);
     if (!includeTag) {
     s = s.substring(Helper.removeRegEx(tag1).length());
     s = s.substring(0, s.length() - Helper.removeRegEx(tag2).length());
     if (s.startsWith(">")) {
     s = s.substring(1);
     }
     }
                    
     list.add(s);
     }
     if (list.size() > 0) {
     return list;
     }
     }
     }
     return list;
     }
     */
    public static String replaceAllBetweenTags(String text) {
        return text.replaceAll("<[/]*[0-9,a-z,A-Z,\\s,\\t,\\r,=,\",\',%,\\.,-]*>", "");
    }

    public static String replaceAllBetweenTags(String text, String with) {
        return text.replaceAll("<[/]*[0-9,a-z,A-Z,\\s,\\t,\\r,=,\",\',%,\\.]*>", with);
    }

    public static List<String> replaceAllBetweenTagsList(List<String> list) {
        int i = 0;
        for (String text : list) {
            list.set(i++, getNormalisedString(replaceAllBetweenTags(text)));
        }
        return list;
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
            String element = new HtmlParser(node).getNamedTagText(tag);
            if (element != null) {
                list.add(element);
            }
        }
        return list;
    }

    public static List<String> getRegExMath(String str, String regexs) {
        List<String> list = new ArrayList<String>();
        for (String regex : regexs.split("\\|")) {
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(str);
            while (m.find()) {
                list.add(m.group());
            }
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

    public static List<String> removeEmpty(List<String> l) {
        return (Arrays.asList(removeEmpty(l.toArray(new String[]{}))));
    }

    public static String[] removeEmpty(String[] l) {
        List<String> ret = new ArrayList<String>();
        if (l != null) {
            for (String s : l) {
                s = s.trim();
                if (s != null && !s.isEmpty() && !s.equalsIgnoreCase("null")) {
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

    public static Iterable<? extends JpaEntity> reverse(Iterable<? extends JpaEntity> src) {
        List<JpaEntity> list = new ArrayList<JpaEntity>();
        list.addAll((Collection<? extends JpaEntity>) src);
        Collections.reverse(list);
        return list;
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
            LOG.error(String.format("url: %s is invalid", uri));
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

    public synchronized static String getStackTrace() {
        StringWriter sw = new StringWriter();
        new Throwable().printStackTrace(new PrintWriter(sw));
        String ret = sw.toString();
        return ret;
    }
}
