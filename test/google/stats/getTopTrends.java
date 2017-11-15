/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package google.stats;

import bidder.Helper;
import bidder.HttpHelper;
import bidder.ItemBidder;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonValue;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import utils.URLParamEncoder;

/**
 *
 * @author dmitry
 */
public class getTopTrends {

    private static String url = "http://www.google.com/trends/fetchComponent?hl=en-US&q=<query>&geo=AU&cid=TOP_QUERIES_0_0&cat=0-5&date=1%2F2016%2012m";
    private static ItemBidder itemBidder = new ItemBidder();

    public static void main(String[] args) throws Exception {
        if (args == null || args.length == 0) {
            //args = new String[]{"support", "help", "office", "virus", "antivirus"};
            args = new String[]{"antivirus"};
        }
        String meta = "";
        for (String s : args) {
            String q = URLParamEncoder.encode(s);
            q = url.replace("<query>", q);
            meta += ", " + generateMeta(getJson(getStats(q)));
        }
        System.out.println(MergeMeta(meta.substring(2)));
    }

    public static String MergeMeta(String meta) {
        return "<meta name=\"description\" content=\"" + meta + "\"/>";
    }

    public static String getStats(String url) throws Exception {
        HttpGet get = new HttpGet(new URI(url));
        get.addHeader("Cookie", "NID=77=tbYki7R1byUzilhFuo0NquuNA-czVu5AgfYOlTbkm7AfDyr-8-YKcaOqoMv6UfMdqImIRSXaG3IRG0vBoYUv7Ja0Q1OfnG8beAYGNxvPXz8WOCn1Hko6CMzmUhciXKKO"
                + "; S=izeitgeist-ad-metrics=4mZVoD8NqlQ");
        get.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0");

        Exception e = new Exception("Unknown exception");
        for (int i = 0; i < 3; i++) {
            try {
                InputStream is = itemBidder.httpclient.execute(get).getEntity().getContent();
                return new String(Helper.loadStream(is));
            } catch (Exception ex) {
                System.out.println("Exception: " + ex.getMessage());
                e = ex;
            }
        }
        throw e;
    }

    private static JsonObject getJson(String out) {
        JsonArrayBuilder jab = Json.createArrayBuilder();
        for (String str : HttpHelper.split(out, "<table", "</table>")) {
            for (String str2 : HttpHelper.split(out, "<tr", "</tr>")) {
                for (String str1 : HttpHelper.split(str2, "<a", "</a>")) {
                    String s = HttpHelper.split(str1, ">", "<").get(0).replace("<", "").replace(">", "").trim();
                    if (s != null && s.length() > 0) {
                        jab.add(s);
                    }
                }
            }
        }
        JsonObject obj = Json.createObjectBuilder().add("items", jab.build()).build();
        return obj;
    }

    private static String joinList(List<String> list) {
        String s = "";
        for (String s1 : list) {
            s += s1 + ", ";
        }
        return s.substring(0, s.length() - 2);
    }

    private static String generateMeta(JsonObject obj) throws Exception {
        String meta = "";
        List<String> list = new ArrayList<String>();
        for (JsonValue s : obj.getJsonArray("items")) {
            if (!list.contains(s.toString())) {
                list.add(s.toString());
            }
            String q = URLParamEncoder.encode(s.toString());

            for (JsonValue s1 : getJson(getStats(url.replace("<query>", q))).getJsonArray("items")) {
                if (!list.contains(s1.toString())) {
                    list.add(s1.toString());
                }
            }
        }
        meta += joinList(list);

        return meta;
    }
}
