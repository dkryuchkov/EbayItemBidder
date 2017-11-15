package bidder;

import static utils.Helper.cookieStore;
import static com.intforce.utils.Helper.isNumeric;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author dmitry
 */
public class Utils {

    static final String statsUrl = "http://investor-api.realestate.com.au/states/NSW/suburbs/<suburb>/postcodes/<postcode>/property_types/house/bedrooms/3.json";
    static HttpHost proxy = null;
    static HttpClient httpclient = null;
    static RequestConfig config = null;
    static String key = "$rt98765Bde84657"; // 128 bit key
    static String initVector = "Rando7Ini1Vector"; // 16 bytes IV
    static final String postcodeToSuburb = "http://v0.postcodeapi.com.au/suburbs/<postcode>.json";

    static {
        config = proxy == null ? RequestConfig.custom()
                .build() : RequestConfig.custom()
                        .setProxy(proxy).setConnectTimeout(3 * 60 * 1000)
                        .build();
        httpclient = HttpClients.custom().setDefaultRequestConfig(config).setDefaultCookieStore(cookieStore).build();

    }

    public static void main(String[] p) throws UnsupportedEncodingException, Exception {

        String token = getToken("token");
        System.out.println(getTokenValid(token));
    }

    public static boolean isStringNumeric(String str) {
        NumberFormat formatter = NumberFormat.getInstance();
        ParsePosition pos = new ParsePosition(0);
        formatter.parse(str, pos);
        return str.length() == pos.getIndex();
    }

    public static String getNumber(String text) {
        String ret = "";
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
            return ret;
        }
        return null;
    }

    public static List<String> getSuburbs(String postcode) throws Exception {
        List<String> list = new ArrayList<String>();
        HttpGet get = new HttpGet(postcodeToSuburb.replace("postcode>", postcode));
        for (JsonValue json : parseJsonArray(get)) {
            list.add(((JsonObject) json).getString("name"));
        }
        return list;
    }

    private static JsonArray parseJsonArray(HttpUriRequest get) throws IOException {
        InputStream is = httpclient.execute(get).getEntity().getContent();
        String response = new String(utils.Helper.loadStream(is));
        try {
            JsonArray obj = Json.createReader(new StringReader(response)).readArray();
            is.close();
            return obj;
        } catch (javax.json.stream.JsonParsingException ex) {
            text("Exception parsing Json: " + response);
        }
        return null;
    }

    private static JsonObject parseJson(HttpUriRequest get) throws IOException {
        InputStream is = httpclient.execute(get).getEntity().getContent();
        String response = new String(utils.Helper.loadStream(is));
        try {
            JsonObject obj = Json.createReader(new StringReader(response)).readObject();
            is.close();
            return obj;
        } catch (javax.json.stream.JsonParsingException ex) {
            text("Exception parsing Json: " + response);
        }
        return null;
    }

    private static final void text(String txt) {
        System.out.println(new Date().toString() + ": " + txt);
    }

    public static String getTokenValid(String token) {
        if (token != null) {
            try {
                String t1 = decrypt(token);
                boolean valid = (Calendar.getInstance().getTimeInMillis() - Long.valueOf(t1.split("_")[1])) < 30 * 1000;
                if (valid) {
                    return t1.split("_")[0];
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    public static String getToken(String type) {
        return encrypt(type + "_" + String.valueOf(Calendar.getInstance().getTimeInMillis()));
    }

    public static String get_UTC_Datetime_from_timestamp(long timeStamp) {
        try {

            Calendar cal = Calendar.getInstance();
            TimeZone tz = cal.getTimeZone();

            int tzt = tz.getOffset(System.currentTimeMillis());

            timeStamp -= tzt;

            // DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
            DateFormat sdf = new SimpleDateFormat();
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        } catch (Exception ex) {
            return "";
        }
    }

    public static void info(String text) {
        System.out.println(text);
    }

    public static String encrypt(String text) {

        return Encryptor.encrypt(key, initVector, text);
    }

    public static String decrypt1(String text) throws Exception {
        return Encryptor.decrypt1(key, initVector, text);
    }

    public static String decrypt(String text) throws Exception {
        return Encryptor.decrypt(key, initVector, text);
    }

    public static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {

        }
        return value;
    }
}
