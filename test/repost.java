/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import bidder.Helper;
import bidder.ItemBidder;
import static com.intforce.gumtree.ImageRecog.getNumber;
import static bidder.Helper.context;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

/**
 *
 * @author dmitry
 */
public class repost {

    static final String projectFolder = "/var/lib/openshift/56fc9c947628e11e920000e1/app-root/data/";
    static final String failedFolder = projectFolder + "failed/<username>";
    static final String saveFolder = projectFolder + "saved/<username>";

    static final String deleteFile = projectFolder + "delete/<username>/delete.txt";
    public static String username = "";
    public static String password = "";
    public final static String listing = "http://www.gumtree.com.au/p-edit-ad.html?adId=<id>";
    private static String url = "https://www.gumtree.com.au/t-login-form.html";
    private static String data = "targetUrl=&likingAd=false&loginMail=kryuchkov%40hotmail.com&password=16051972&_rememberMe=on";
    private static String url2 = "http://www.gumtree.com.au/m-my-ads.html?c=1";
    private static final String url1 = "https://www.gumtree.com.au/t-login.html";
    private static final String href = "href=\"";
    private static final String attr = "\"attr\":{";
    private static final String bbUserInput = "name=\"bbUserInput\"";
    private static final String onpage = "value c-dark-green\">";
    private static final String delete = "http://www.gumtree.com.au/m-delete-ad.html?adId=<id>";
    private static final String add = "http://www.gumtree.com.au/p-submit-ad.html";
    private static final String bb_image = "http://www.gumtree.com.au/bb-image.html?tok=<id>";
    private static final String bb_token = "data-token=\"";
    private static final String addAlways = "_renew=on,_showLocationOnMap=on,repost=false,streetName= ,"
            + "unacceptableGeocode=false,"
            + "adId= ,adType=OFFER,"
            + "postcode=2040,galleryImageIndex= ,geocodeConfidence=OK,houseNumber= ,"
            + "attributeMap['cars.greyimport']= ,"
            + "files[]= ,"
            + "level2CategoryId= ,"
            + "quickOffer= ";
    private static final String addMatch = "pstad-descrptn=description,pstad-lat=geocodeLat,"
            + "categoryId=categoryId,"
            + "pstad-lng=geocodeLng,"
            + "pstad-locality=geocodeLocality,images=images,pstad-loctnid=locationId,"
            + "pstad-map-address=mapAddress,offer-amount=minimumOfferPrice,postad-name=name,"
            + "postad-phonenumber=phoneNumber,images=images,pstad-price=price.amount,"
            + "price.type1=price.type,price.type2=price.type,title=title";

    private static final String addMatchPart = "postad-img-=images";
    private static final String addMatchSpecial = ".condition_s";
    //"cars.carmake_s,cars.forsaleby_s,"
    private static final Map<String, String> mapAlways = new HashMap<String, String>();
    private static final Map<String, String> mapMatch = new HashMap<String, String>();
    private static final Map<String, String> mapMatchPart = new HashMap<String, String>();
    private static final String bbInput = "bbUserInput=5628,bbToken=1e5236ded5e8903554adb153c86607c1add9e8794d939aefaf5f3830fb539fda9222231a9c51c7e0ee6abaf33bddf0bdc911fcfec64ca6888c124006e8c7ee0a";
    private static final String bbInputText = " verification code.";
    private static String user;

    static {
        for (String key : addAlways.split(",")) {
            mapAlways.put(key.split("=")[0], key.split("=")[1].trim());
        }
        for (String key : addMatch.split(",")) {
            mapMatch.put(key.split("=")[0], key.split("=")[1].trim());
        }
        for (String key : addMatchPart.split(",")) {
            mapMatchPart.put(key.split("=")[0], key.split("=")[1].trim());
        }
    }

    public static void main(String[] args) throws Exception {
        if (args == null || args.length < 2) {
            System.out.println("Wrong number of arguments");
        } else {
            username = args[0];
            password = args[1];
            System.out.println("Started");
            for (int n = 0; n < username.split(",").length; n++) {
                System.out.println(login(n));
                user = username.split(",")[n];
                deleteAdv(user);
                loadFailed(user);

                renewAdv();
            }
            System.out.println("Finished");
        }
    }

    private static void renewAdv() throws Exception {
        String[] args = new String[0];
        for (String id : getItems(getPage(url2))) {
            id = id.split("=")[1];

            String out = getPage(listing.replace("<id>", id));
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            for (String key : mapAlways.keySet().toArray(args)) {
                nvps.add(new BasicNameValuePair(key, mapAlways.get(key)));
            }
            for (String text : com.intforce.utils.Helper.split(out, "<input ", ">")) {
                String vid = getValue(text, "id");
                String value = getValue(text, " value");
                String type = getValue(text, "type");
                String checked = getValue(text, "checked");
                if ((type.equalsIgnoreCase("radio") && checked.equalsIgnoreCase("checked"))
                        || (!type.equalsIgnoreCase("radio"))) {
                    if (mapMatch.containsKey(vid)) {
                        if (mapMatch.get(vid).equalsIgnoreCase("images")) {
                            value = value.replace("$_14.", "$_20.");
                        }
                        nvps.add(new BasicNameValuePair(mapMatch.get(vid), value));
                    } else {
                        for (String key : mapMatchPart.keySet().toArray(args)) {
                            if (vid.startsWith(key)) {
                                nvps.add(new BasicNameValuePair(mapMatchPart.get(key), value));
                            }
                        }
                        if (vid.contains(addMatchSpecial)) {
                            nvps.add(new BasicNameValuePair("attributeMap['" + vid.split("-")[0] + "']", value));
                        }
                    }
                }

            }
            for (String text : com.intforce.utils.Helper.split(out, "<textarea ", "</textarea>")) {
                String vid = getValue(text, "id");
                if (mapMatch.containsKey(vid)) {
                    text = text.substring(text.indexOf(">") + 1);
                    String value = text.substring(0, text.length() - 1);
                    nvps.add(new BasicNameValuePair(mapMatch.get(vid), value));
                }

            }
            out = out.substring(out.indexOf(attr) + attr.length());
            out = out.substring(0, out.indexOf("}"));
            for (String att : out.split(",")) {
                String[] attrs = att.replace("\"", "").split(":");
                if (!attrs[0].equalsIgnoreCase("id") && attrs.length > 1) {
                    BasicNameValuePair v = new BasicNameValuePair("attributeMap['" + attrs[0] + "']", attrs[1]);
                    if (!nvps.contains(v)) {
                        nvps.add(v);
                    } else {
                        System.out.println(String.format("Already contains: %s=%s", attrs[0], attrs[1]));
                    }
                }
            }

            getPage(delete.replace("<id>", id));
            saveAdvert(user, id, nvps);
            out = getPost(nvps);
            checkPost(out, nvps, id);
        }
    }

    private static final void saveAdvert(String user, String id, List<NameValuePair> nvps) {
        String title = getKey(nvps, "title");
        stringToFile(saveFolder.replace("<username>", user), nvps, id + " " + title);
    }

    private static final String getKey(List<NameValuePair> nvps, String key) {
        for (NameValuePair p : nvps.toArray(new NameValuePair[0])) {
            if (p.getName().equalsIgnoreCase(key)) {
                return p.getValue();
            }
        }
        return "";
    }

    private static final void deleteAdv(String username) throws Exception {
        File file = new File(deleteFile.replace("<username>", username.split("@")[0]));
        if (file.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    text("Found advert to delete: " + line);
                    getPage(delete.replace("<id>", line));
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                reader.close();
                file.delete();
            }
        }
    }

    private static List<NameValuePair> removeBBimageT(List<NameValuePair> nvps) {
        while (true) {
            boolean b = true;
            for (NameValuePair p : nvps) {
                if (p.getName().equalsIgnoreCase("bbUserInput") || p.getName().equalsIgnoreCase("bbToken")) {
                    nvps.remove(p);
                    b = false;
                    break;
                }
            }
            if (b) {
                break;
            }
        }
        return nvps;
    }

    private static void checkPost(String out, List<NameValuePair> nvps, String id) throws Exception {
        System.out.println("In checkPost");
        while (out.trim().length() > 0) {
            if (!fileExists(id)) {
                stringToFile(nvps, id);
            }
            if (out.contains(bbInputText)) {
                nvps = removeBBimageT(nvps);

                out = out.substring(out.indexOf(bb_token) + bb_token.length());
                out = out.substring(0, out.indexOf("\""));
                String bbUserInputValue = getNumber(bb_image.replace("<id>", out), user); //getBbUserInput(bb_image.replace("<id>", out));
                System.out.println("Got bbUserInputValue: " + bbUserInputValue + " for: " + id);
                nvps.add(new BasicNameValuePair("bbUserInput", bbUserInputValue));
                nvps.add(new BasicNameValuePair("bbToken", out));
                out = getPost(nvps);
            } else {
                break;
            }
        }
        if (out.trim().length() > 0) {
            stringToFile(nvps, id);
        } else {
            deleteIfExists(id);
            System.out.println("Posted: " + id);
        }
    }

    public static String loadImage(String url) {
        if (new File(url).exists()) {
            return url;
        }
        Image image = null;
        try {
            URL iurl = new URL(url);
            image = ImageIO.read(iurl);
            BufferedImage bi = (BufferedImage) image;
            File f = new File("./output.bmp");
            if (f.exists()) {
                f.delete();
            }
            ImageIO.write(bi, "bmp", f);
            return f.getAbsolutePath();
        } catch (IOException e) {
        }
        return null;
    }

    private static String checkKeyValue(String key, String value) {
        if (key.equalsIgnoreCase("images")) {
            value = value.replace("$_14.", "$_20.");
        }
        return value;
    }

    private static void loadFailed(String user) throws Exception {
        File folder = new File(failedFolder.replace("<username>", user));
        folder.mkdirs();
        System.out.println("Looking for failed files in: " + folder.getAbsolutePath());
        for (File file : folder.listFiles()) {
            text("Found file to load: " + file.getAbsolutePath());
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = null;
            String out = "";
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            try {
                while ((line = reader.readLine()) != null) {
                    if (line.contains("=")) {
                        String key = line.split("=")[0];
                        String value = line.length() > line.indexOf("=") + 1 ? line.substring(line.indexOf("=") + 1) : "";
                        value = checkKeyValue(key, value);
                        nvps.add(new BasicNameValuePair(key,
                                value));
                    }
                }

                out = getPost(nvps);

            } catch (Exception ex) {
                ex.printStackTrace(System.out);
            } finally {
                reader.close();
                file.delete();
                checkPost(out, nvps, UUID.randomUUID().toString().replace("-", ""));
            }
        }
    }

    private static final void text(String txt) {
        System.out.println(String.format(txt));
    }

    private static String getBbUserInput(String url) throws IOException {
        Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start chrome " + url});

        String code = "";
        while (code.length() != 4) {
            System.out.println("Enter verification code: ");
            byte[] b = new byte[4];
            System.in.read(b, 0, 4);
            code = new String(b, 0, 4);
            code = code.replaceAll("[^0-9.]", "");
        }
        return code;
    }

    private static void deleteIfExists(String fileName) {
        File file = new File(failedFolder + "/" + getFileName(fileName));
        if (file.exists()) {
            file.delete();
            text("Deleted file " + file.getAbsolutePath());
        }
    }

    private static boolean fileExists(String fileName) {
        File file = new File(failedFolder + "/" + getFileName(fileName));
        return file.exists();
    }

    private static void stringToFile(List<NameValuePair> nvps, String fileName) {
        stringToFile(failedFolder, nvps, fileName);
    }

    private static String getFileName(String fileName)
    {
        return fileName.replace("/", "-").replace("/", "-") + ".txt";
    }
    private static void stringToFile(String folder, List<NameValuePair> nvps, String fileName) {
        try {
            new File(folder).mkdirs();
            File file = new File(folder + "/" + getFileName(fileName));
            String out = "";
            for (NameValuePair v : nvps) {
                out += v.getName() + "=" + v.getValue() + "\n";
            }
            // if file doesnt exists, then create it 
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException ex) {
                    file = file;
                }
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(out);
            bw.close();
            System.out.println("Saved post to: " + file.getAbsolutePath());
            //System.out.println("Done writing to " + fileName); //For testing 
        } catch (IOException e) {
            System.out.println("Error: " + e);
            e.printStackTrace();
        }
    }

    public static String getPost(List<NameValuePair> nvps) throws Exception {
        System.out.println("In getPost");
        HttpPost post = new HttpPost(new URI(add));
        post.addHeader("Cookie", Helper.join(Helper.cookieStr, "\n"));
        post.setEntity(new UrlEncodedFormEntity(nvps));
        if(ItemBidder.config != null)
            post.setConfig(ItemBidder.config);
        Exception e = new Exception("Unknown exception");
        for (int i = 0; i < 3; i++) {
            try {
                InputStream is = ItemBidder.httpclient.execute(post,context).getEntity().getContent();
                System.out.println("Got InputSream");
                return new String(Helper.loadStream(is));
            } catch (Exception ex) {
                System.out.println("Exception: " + ex.getMessage());
                e = ex;
            }
        }
        throw e;
    }

    public static String getValue(String text, String id) {
        id = id + "=\"";
        String s = text.substring(text.indexOf(id) + id.length());
        return s.substring(0, s.indexOf("\""));
    }

    public static List<Long> getItemsOnPage(String page) throws Exception {
        List<Long> list = new ArrayList<Long>();
        for (String text : com.intforce.utils.Helper.split(page, "<li class=\"rs-ma-onpg\">", "</li>")) {
            list.add(com.intforce.utils.Helper.getLong(text.substring(text.indexOf(onpage) + onpage.length())));
        }
        return list;
    }

    public static List<String> getItems(String page) throws Exception {
        int i = 0;
        List<Long> listOnPage = getItemsOnPage(page);
        List<String> list = new ArrayList<String>();
        for (String text : com.intforce.utils.Helper.split(page, "<a class=\"rs-ad-title\"", "</a>")) {
            String id = text.substring(text.indexOf(href) + href.length());
            id = id.substring(0, id.indexOf("\""));
            System.out.println("Checking id: " + id);
            if (id != null && listOnPage.get(i) > 3) {
                list.add(id);
            }
            i++;

        }
        return list;
    }

    public static String getPage(String url) throws Exception {
        while (true) {
            try {
                HttpGet get = new HttpGet(new URI(url));
                get.addHeader("Cookie", Helper.join(Helper.cookieStr, "\n"));
                return new String(Helper.loadStream(Helper.setCookies(ItemBidder.httpclient.execute(get,context)).getEntity().getContent()));
            } catch (Exception ex) {
            }
        }
    }

    public static String login(int n) throws Exception {

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("targetUrl", ""));
        nvps.add(new BasicNameValuePair("likingAd", "false"));
        nvps.add(new BasicNameValuePair("loginMail", username.split(",")[n]));
        nvps.add(new BasicNameValuePair("password", password.split(",")[n]));
        nvps.add(new BasicNameValuePair("_rememberMe", "on"));

        HttpPost post = new HttpPost(new URI(url1));
        //post.addHeader("User-Agent", "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.7) Gecko/2009030422 Ubuntu/8.10 (intrepid) Firefox/3.0.7");
        post.setEntity(new UrlEncodedFormEntity(nvps));

        return new String(Helper.loadStream(Helper.setCookies(ItemBidder.httpclient.execute(post,context)).getEntity().getContent()));

    }
}
