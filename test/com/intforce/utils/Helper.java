package com.intforce.utils;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


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
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.TimeZone;
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
    public static final String any_space = "([ \\t\\n\\r]*)";
    public static final String any_row_break = "([\\r]*)";
    private static final String[][] xmlsc = new String[][]{{"&", "&amp;"}, {"<", "&lt;"}, {">", "&gt;"}, {"\"", "&quot;"}, {"'", "&apos;"}};
    private static final String HTTP503 = "HTTP response code: 503";

    public static String removeAnyLineBreakSymbols(String str) {
        return str.replaceAll(any_row_break, "");
    }

    public static List<String> split(List<String> strs, String tag1, String tag2) {
        List<String> list = new ArrayList<String>();
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
        str = removeAnyLineBreakSymbols(str);

        List<String> list = new ArrayList<String>();
        while (str.contains(tag1) && !str.equals(tag1)) {
            int start = str.indexOf(tag1);
            str = str.substring(start);
            start = 0;
            String str1 = str;
            start = str.indexOf(tag2, start + tag1.length());
            while (str1.contains(tag2)) {
                str1 = str.substring(0, start + tag2.length());
                if (!getTagName(tag2).isEmpty() && (count(str1, "<" + getTagName(tag1))) > count(str1, "</" + getTagName(tag2) + ">")) {
                    start = str.indexOf(tag2, start) + tag2.length();
                    str1 = str.substring(0, start);
                    continue;
                }
                list.add(str1.substring(0, str1.length() - (tag2.length() - 1)));
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
            return "";
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
            String[] tag2s = tags2.split("\\|");
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
                LOG.error(String.format("###Error retreving page by URL: %s\n%s", uri, ex.getMessage()));
                Thread.sleep(1000);
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
        return text.replaceAll("<[/]*[0-9,a-z,A-Z,\\s,\\t,\\r,=,\",\',%,\\.]*>", "");
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
