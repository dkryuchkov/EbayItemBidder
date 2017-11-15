/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bidder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author dmitry
 */
public class HtmlParser {

    String html = null;
    public static final String div_end = "</div>";
    private static final String header_start = "<header";
    private static final String header_end = "</header>";
    private static final String body_start = "<body";
    private static final String body_end = "</body>";
    private static final String meta_start = "<meta" + Helper.any_space + "name=\"<name>\"|<meta property=\"<name>\"";
    private static final String meta_end = "</meta>|/>|>";
    private static final String tag_id_start = "id=" + Helper.any_space + "\"<id>\"";
    private static final String named_tag_class_start = "<<tag>" + Helper.any_patern_tag + " class=" + Helper.any_space + "\"<class>\"";
    private static final String named_tag_property_start = "<<tag>" + Helper.any_patern_tag + " <property>=" + Helper.any_space + "\"<value>";
    private static final String tag_end = "</";
    private static final String tag_any = "<" + Helper.any_patern_tag + ">" + Helper.any_space;
    private static final String named_tag_end = "</<tag>>";
    private static final String tag_text_regex = ">" + Helper.any_row_break + Helper.any_patern + Helper.any_row_break + tag_end;
    private static final Logger LOG = LoggerFactory.getLogger(HtmlParser.class);
    private static final String named_tag_start = "<<tag>" + Helper.any_patern_tag + ">";
    private static final String named_tag_start_end = "<<tag>" + Helper.any_patern_tag + "[/>]*";
    private static final String tag_property_start = "<<tag>" + Helper.any_patern_tag + " <property>=\"<value>\">";
    private static final String named_tag_id_start = "<<tag>" + Helper.any_patern_tag + " " + tag_id_start;

    private static final String property_value = "<property>=\"";

    protected HtmlParser() {
    }

    public HtmlParser(String html) {
        this.html = html;
    }

    public static String getNamed_tag_end() {
        return named_tag_end;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html.replace("\n", "").replace("\r", "").replace("\t", " ");
    }

    public String getTextBetweenTags(String tag1, String tag2) {
        List<String> list = Helper.split(html, tag1, tag2);
        String ret = list.size() > 0 ? list.get(0) : null;
        if (ret != null && ret.contains(tag1)) {
            ret = ret.substring(tag1.length());
            ret = ret.substring(0, ret.length() - 1);
        }
        return ret;
    }

    public String getHeader() {
        String header = Helper.splitTopLevel(html.replace("\\|", " "), header_start, header_end);
        return header == null ? html : header;
    }

    public String getBody() {
        String body = Helper.splitTopLevel(html, body_start, body_end);
        return body == null ? html : body;
    }

    public String getMeta(String names) {
        List<String> list = null;
        for (String name : names.split("\\|")) {
            for (String start : meta_start.split("\\|")) {
                list = Helper.splitRegEx(getHeader(), start.replace("<name>", name), meta_end);
                if (list != null && list.size() > 0) {
                    return list.get(0);
                }
            }
        }
        return list != null && list.size() > 0 ? list.get(0) : null;
    }

    public String getMetaHtml(String name) {
        List<String> list = Helper.splitRegEx(html, meta_start.replace("<name>", name), meta_end);
        return list.size() > 0 ? list.get(0) : null;
    }

    public String getBodyTagTextById(String id) {
        List<String> list = Helper.splitRegEx(getBody(), tag_id_start.replace("<id>", id), tag_end);
        return list.size() > 0 ? list.get(0) : null;
    }

    public String getNamedTagText(String tag) {
        return getNamedTagText(tag, 0, true);
    }

    public String getLastNamedTagText(String tag) {
        return getNamedTagText(tag, -1, true);
    }

    public String getNamedTagText(String tag, int i) {
        return getNamedTagText(tag, i, true);
    }

    public String getNamedTagText(String tag, int i, boolean remove) {
        List<String> list = Helper.splitRegEx(html, named_tag_start.replace("<tag>", tag), tag_end);
        try {
            list = Helper.getNonEmptyList(list);
            if (list.size() > i) {
                String s = i >= 0 ? list.get(i) : list.get(list.size() - 1);
                if (s.contains(">") && remove) {
                    s = s.substring(s.indexOf(">") + 1);
                }
                return s;
            }
        } catch (Exception ex) {

        }
        return null;
    }

    public List<String> getNamedTagsText(String tag, boolean remove) {
        List<String> ret = new ArrayList<String>();
        List<String> list = Helper.splitRegEx(html, named_tag_start.replace("<tag>", tag), tag_end);
        try {
            for (String s : Helper.getNonEmptyList(list)) {
                if (s.contains(">") && remove) {
                    s = s.substring(s.indexOf(">") + 1);
                }
                ret.add(s);
            }
        } catch (Exception ex) {

        }
        return ret;
    }

    public String getBodyNamedTagPropertyValueById(String tag, String id, String property) {
        List<String> list = Helper.splitRegEx(getBody(), named_tag_id_start.replace("<tag>", tag).replace("<id>", id), ">", true);
        String ret = list.size() > 0 ? new HtmlParser(list.get(0)).getTextBetweenTags(
                property_value.replace("<property>", property), "\"") : null;

        return ret;
    }

    public static String getBodyNamedTagPropertyValue(String html, String tag, String property) {
        List<String> list = Helper.splitRegEx(html, named_tag_start_end.replace("<tag>", tag), ">", true);
        String ret = list.size() > 0 ? new HtmlParser(list.get(0)).getTextBetweenTags(
                property_value.replace("<property>", property), "\"") : null;

        return ret;
    }

    public String getBodyTagPropertyValueById(String id, String property) {
        List<String> list = Helper.splitRegEx(getBody(), tag_id_start.replace("<id>", id), tag_end, true);
        return list.size() > 0 ? new HtmlParser(list.get(0)).getTextBetweenTags(
                property_value.replace("<property>", property), "\"") : null;
    }

    public List<String> getBodyNamedTagsTextById(String tag, String id) {
        List<String> list = Helper.splitRegEx(getBody(), named_tag_id_start.replace("<tag>", tag).
                replace("<id>", id), tag_end, true);
        List<String> result = new ArrayList<String>();
        for (String str : list) {
            result.add(new HtmlParser(str).getTextBetweenTags(">", "<"));
        }
        return result;
    }

    public String getNamedTagTextById(String tag, String id) {
        return getNamedTagTextById(getBody(), tag, id);
    }

    public String getNamedTagTextByTagProperty(String tag, String properties, String values) {
        List<String> result = new ArrayList<String>();
        for (String property : properties.split("\\|")) {
            for (String value : values.split("\\|")) {
                List<String> list = Helper.splitRegEx(getBody(), named_tag_property_start.replace("<tag>", tag).replace("<property>", property).replace("<value>", value), tag_end, true);

                for (String str : list) {
                    if (str != null && str.length() > 0) {
                        String t = new HtmlParser(str).getTextBetweenTags(">", "<");
                        if (t != null && t.trim().length() > 0) {
                            result.add(t);
                        }
                    }
                }
            }
        }
        return result.size() > 0 ? result.get(result.size() - 1) : null;
    }

    public static String getNamedTagTextById(String html, String tags, String ids) {
        List<String> result = new ArrayList<String>();
        for (String tag : tags.split("\\|")) {
            for (String id : ids.split("\\|")) {
                List<String> list = Helper.splitRegEx(html, named_tag_id_start.replace("<tag>", tag).
                        replace(id.equalsIgnoreCase("null") ? " " + tag_id_start : "<id>", id.equalsIgnoreCase("null") ? "" : id), tag_end, true);
                for (String str : list) {
                    if (str != null && str.length() > 0) {
                        result.add(new HtmlParser(str).getTextBetweenTags(">", "<"));
                    }
                }
            }
        }
        return result.size() > 0 ? result.get(0) : null;
    }

    public String getBodyNamedTagTextById(String tags, String ids) {
        for (String tag : tags.split(" ")) {
            for (String id : ids.split(" ")) {
                String body = getBody();
                if (body == null) {
                    body = html;
                }
                String text = Helper.splitTopLevel(body,
                        named_tag_id_start.replace("<tag>", tag).replace("<id>", id),
                        named_tag_end.replace("<tag>", tag));
                if (text != null && text.trim().length() > 0) {
                    if (text.startsWith("<iframe")) {
                        try {
                            return Helper.getHttpPage(new HtmlParser(text).getPropertyValue("src"));
                        } catch (Exception ex) {
                        }
                    }
                    return text;
                }
            }
        }
        return null;
    }

    public List<String> getBodyNamedTagPropertyValueByClass(String tag, String className, String property) {
        List<String> list = Helper.splitRegEx(getBody(), named_tag_class_start.replace("<tag>", tag).replace("<class>", className), tag_end, true);
        List<String> result = new ArrayList<String>();
        for (String str : list) {
            result.add(new HtmlParser(str).getTextBetweenTags(
                    property_value.replace("<property>", property), "\""));
        }
        return result;
    }

    public List<String> getBodyNamedTagPropertyValueByProperty(String tag, String property, String value) {
        List<String> list = Helper.splitRegEx(getBody(), named_tag_property_start.replace("<tag>", tag).replace("<property>", property).replace("<value>", value), tag_end, true);
        List<String> result = new ArrayList<String>();
        for (String str : list) {
            result.add(new HtmlParser(str).getTextBetweenTags(
                    property_value.replace("<property>", property), "\""));
        }
        return result;
    }

    public String getPropertyValue(String property) {
        return getPropertyValue(property, 0);
    }

    public String getPropertyValue(String property, int i) {
        String ret = null;
        List<String> list = Helper.split(html, property + "=\"", "\"");
        return list != null && list.size() > 0 ? list.get(0) : null;
    }

    public List<String> getBodyOfNamedTagTextByClass(String tag, String className) {
        List<String> list = new ArrayList<String>();
        for (String s : Helper.splitRegEx(html, named_tag_class_start.replace("<tag>", tag).replace("<class>", className), named_tag_end.replace("<tag>", tag))) {
            list.add(s);
        }
        return list;
    }

    public List<String> getBodyNamedTagTextByClass(String tag, String className) {
        List<String> list = Helper.splitRegEx(html, named_tag_class_start.replace("<tag>", tag).replace("<class>", className), named_tag_end.replace("<tag>", tag));
        return list;
    }

    public String getInnerText() {
        String ret = html;
        if (ret.contains(">")) {
            ret = ret.substring(ret.indexOf(">") + 1);
        }
        if (ret.contains("<")) {
            ret = ret.substring(0, ret.indexOf("<"));
        }

        return ret;

    }

    public String getTagByText(String texts) {
        String ret = null;
        for (String text : texts.split("\\|")) {
            List<String> list = Helper.splitRegEx(html, tag_any + text + Helper.any_patern, tag_end, true);
            ret = list.size() > 0 ? list.get(0) : null;
            if (ret != null) {
                return ret;
            }
        }
        return null;
    }

    public String getNextSiblingTagText(String text) throws Exception {
        if (text == null || !html.contains(text)) {
            return null;
        }
        String text1 = text;
        String tag = null;
        String tagName = Helper.getTagName(text);
        String str = html.substring(0, html.indexOf(text));
        if (str.lastIndexOf("<") < 0) {
            return null;
        }
        String parent = str.substring(str.lastIndexOf("<"));
        parent += text + tagName + ">";
        tag = text + tagName + ">";
        str = html.substring(html.indexOf(tag) + tag.length());
        tag = str.substring(0, str.indexOf(tag_end));
        parent += tag + named_tag_end.replace("<tag>", Helper.getTagName(tag));
        parent += named_tag_end.replace("<tag>", Helper.getTagName(parent));

        /*List<String> list = Helper.getRegExMath(text, tag_text_regex);
         if (list != null && list.size() > 0) {
         tag = list.get(0).substring(1);
         tag = tag.substring(0, tag.length() - 2);
         Node node = Helper.getNodeByText(parent, tag);
         if (node != null) {
         return node.getTextContent();
         }
         */
        try {
            parent = parent.replaceAll("<[/]*h[1-9]*>", "");
            return Helper.loadXMLFromString(parent).getChildNodes().item(0).getFirstChild().getNextSibling().getTextContent();

        } catch (Exception ex) {
            try {
                //return parent.replaceAll("<[/]*[0-9,a-z,A-Z,\\s,\\t,\\r,=,\",\',%,\\.]*>", "").replaceAll("[\r,\\t]*", "").replace(text1, "").trim();
            } catch (Exception e) {
            }
            // LOG.info(String.format("Cannot find NextSiblingTagText: %s", text));
        }
        return null;
    }

    public String getTagByTag(String tag) {
        List<String> list = Helper.splitRegEx(html,
                named_tag_start.replace("<tag>", tag), named_tag_end.replace("<tag>", tag));
        return list.size() > 0 ? list.get(0) : null;
    }

    public String getTagTextByProperty(String tag, String property, String value) {
        List<String> list = Helper.splitRegEx(html,
                tag_property_start.replace("<tag>", tag)
                        .replace("<property>", property)
                        .replace("<value>", value), named_tag_end.replace("<tag>", tag));
        return list.size() > 0 ? list.get(0) : null;
    }

    public String getTopLevelNamedTagTextByProperty(String tag, String propertyName, String value) {
        return Helper.splitTopLevel(html, named_tag_property_start.replace("<tag>", tag)
                .replace("<property>", propertyName).replace("<value>", value), named_tag_end.replace("<tag>", tag));
    }

    public String getTopLevelNamedTagTextByClass(String tag, String className) {
        return Helper.splitTopLevel(html, named_tag_class_start.replace("<tag>", tag)
                .replace("<class>", className), named_tag_end.replace("<tag>", tag));
    }

    public String getTopLevelNamedTagTextByClassLast(String tag, String className) {
        return Helper.splitTopLevelLast(html, named_tag_class_start.replace("<tag>", tag)
                .replace("<class>", className), named_tag_end.replace("<tag>", tag));
    }

    public static String getTopLevelNamedTagTextByClass(String html, String tag, String className) {
        return Helper.splitTopLevel(html, named_tag_class_start.replace("<tag>", tag)
                .replace("<class>", className), named_tag_end.replace("<tag>", tag));
    }

    public String getTopLevelNamedTagText(String tag) {
        return Helper.splitTopLevel(html, named_tag_start.replace("<tag>", tag), named_tag_end.replace("<tag>", tag));
    }
}
