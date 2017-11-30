/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bidder;

import static bidder.ImageRecog.getNumber;
import static bidder.ImageRecog.randInt;
import static bidder.Utils.encrypt;
import com.intforce.db.entity.Item;
import com.intforce.exception.ForbiddenException;
import com.intforce.html.parsers.EbayItemParser;
import com.intforce.md5.MD5Hash;
import ij.gui.GUI;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.net.ssl.SSLHandshakeException;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.openejb.util.Strings;
import utils.HTMLDecoder;

/**
 *
 * @author dmitry
 */
public class Gumtree {

    final String const_MyAdClass = "my-adlisting-ad my-adlisting-ad-outer enabled";
    final String const_GraysCondition = "entity.condition=";
    final String const_GSTnote = "GST is included in the final bid price of this item.|GST is included in the buyers premium.|GST will be added to the final bid price of this item.|GST will be added to the buyers premium.|GST|Grays|Graysonline|grays|grasonline";
    final String const_graysImageClass = "thumbs-list-wrapper";
    final static String loginPage = "id=\"login-email\"";
    final String const_graysLotUrl = "https://www.graysonline.com/lot/<lot>";
    final Map<String, String> map = new HashMap<String, String>();
    final String const_parseJSON = "$.parseJSON(";
    final String const_PostAdForm = "PostAdForm\"}";
    final String const_MyAddById = "/m-my-ad.html?adId=<id>";
    final String const_PostEditUrl = "https://www.gumtree.com.au/p-edit-ad.html?adId=1131405831";
    final String const_PostAddUrl = "https://www.gumtree.com.au/p-post-ad2.html?categoryId=<category>&adType=<type>";
    final String catJsonUrl = "https://www.gumtree.com.au/p-select-category.json?nodeType=cat&level1id=<id>";
    final String URL_CATEGORY = "https://www.gumtree.com.au/p-post-ad.html";
    final String DATA_CAT_ID_PATERN = "data-catid=";
    final String CAT_ID_PATERN = "categoryId-wrp-option-";
    final int TIME_OUT = 2 * 1000;
    //String id, String user, String title, short visits
    final String const_Item = "<id>\n<user>\n<title>\n<page>\n<visits>\n<active>\n<managed>\n<hourstomarket>\n<minvisits>";
    final String loginError = "show-login-form"; //"Sorry, this email and password combination is not known";
    final String projectFolder = "C:\\git\\EbayitemBidder\\";
    final String failedFolder = projectFolder + "failed\\<username>";
    final String imageFolder = projectFolder + "images\\<username>";
    final String tempFolder = projectFolder + "temp\\<username>\\output.bmp";
    final String messagesFolder = projectFolder + "messages\\<username>";
    final String templateItem = projectFolder + "templates\\itemTemplate.txt";
    final String templateInactiveUserReport = projectFolder + "templates\\inactiveUserReportTemplate.txt";
    final String templateActiveUserReport = projectFolder + "templates\\activeUserReportTemplate.txt";
    final String saveFolder = projectFolder + "saved\\<username>";
    final String nexPage = "<a class=\"paginator__page-num\"";
    final String pageNumberSelected = "paginator__page-num--selected\">";
    final String pageNumberNext = "<a class=\"paginator__page-num\"";
    final String deleteFile = projectFolder + "delete\\<username>\\delete.txt";
    final String loadFile = projectFolder + "load\\<username>\\load.txt";
    final String deleteFolder = projectFolder + "delete\\<username>";
    final String outFolder = projectFolder + "out\\<username>";
    public String username = "tatiana_kr@hotmail.com,kryuchkov@hotmail.com";
    public String password = "N0t4any1,16051972";
    public final String show_listing = "https://www.gumtree.com.au/m-my-ad.html?adId=<id>";
    public final String edit_listing = "https://www.gumtree.com.au/p-edit-ad.html?adId=<id>";
    private final String url = "https://www.gumtree.com.au/t-login-form.html";
    private final String data = "targetUrl=&likingAd=false&loginMail=kryuchkov%40hotmail.com&password=16051972&_rememberMe=on";
    private final String url2 = "https://www.gumtree.com.au/m-my-ads.html?show=ALL&pageNum=<page>";
    private final String loginUrlPost = "https://www.gumtree.com.au/t-login.html";
    private final String loginUrlGet = "https://www.gumtree.com.au/t-login-form.html";
    private final String jsonUrlBase = "www.gumtree.com.au";
    private final String jsonUrlGet = "/j-cars-specs.json?make=<make>&model=<model>";
    private final String nvicUrlGet = "https://www.gumtree.com.au/j-car-attributes-defaults.json?code=<nvic>";
    private final String href = "href=\"";
    private final String attr = "\"attr\":{";
    private static final String BASE_INTFORCE_URL = "https://gumtree-intforce.b9ad.pro-us-east-1.openshiftapps.com/";
    private final String bbUserInput = "name=\"bbUserInput\"";
    private final String onpage = "value c-dark-green\">";
    private final static String loginAgain = "<span class=\"login-text c-hide\">Sign in</span>";
    private final String baseURL = "https://www.gumtree.com.au";
    private final String delete = "https://www.gumtree.com.au/m-delete-ad.html?adId=<id>";
    private final String add = "https://www.gumtree.com.au/p-submit-ad.html";
    private final String process = BASE_INTFORCE_URL + "/process?token=<token>";
    private final String userURL = BASE_INTFORCE_URL + "/process?token=<token>&user=<user>";
    private final String messageURL = BASE_INTFORCE_URL + "/process?token=<token>&user=<user>&message=<message>";
    private final String statusURL = BASE_INTFORCE_URL + "/process?token=<token>&user=<user>&status=<status>";
    private final String itemURL = BASE_INTFORCE_URL + "/process?token=<token>&user=<user>&item=<item>";
    private final String addtopostURL = BASE_INTFORCE_URL + "/process?token=<token>&user=<user>&addtopost=<addtopost>";
    private final String itemIdURL = BASE_INTFORCE_URL + "/process?token=<token>&item=<item>";
    private final String bb_image = "https://www.gumtree.com.au/bb-image.html?tok=<id>";
    private final String pleaseEnterVC = "please enter a correct verification code|please enter the validation code below";
    private final String bb_token = "data-token=\"";
    private final String removeAlways = "bbUserInput,ctk,csrft,bbToken";
    private final String exclusionCategoriesTeplate = "<dd class=\"rs-ma-details-category\"><category></dd>";
    private final String exclusionCategories = "Cars, Vans & Utes";
    static final String SELECT_PAID = "You must select a paid Ad Type";

    private final String addAlways = "_renew=on,_showLocationOnMap=on,repost=false,streetName= ;"
            //+ "mapAddress=Chatswood, NSW;"
            + "unacceptableGeocode=false;"
            + "adId= ;"
            + "galleryImageIndex= ;"
            + "geocodeConfidence=OK;"
            + "houseNumber= ;"
            + "attributeMap['cars.greyimport']= ;"
            + "files[]= ;"
            + "level2CategoryId= ;";
    //short_term.availability_mtdtcsv=attributeMap[short_term.availability_mtdtcsv],
    private final String addMatch = "shared_accomodation.availabilitystartdate=datesMap[shared_accomodation.availabilitystartdate],pstad-lat=geocodeLat,"
            + "categoryId=categoryId,"
            + "adType=adType,"
            + "locationLevel1=locationLevel1,"
            + "locationLevel2=locationLevel2,"
            + "pstad-lng=geocodeLng,"
            + "pstad-locality=geocodeLocality,images=images,pstad-loctnid=locationId,"
            + "pstad-map-address=mapAddress,offer-amount=minimumOfferPrice,postad-name=name,"
            + "postad-phonenumber=phoneNumber,images=images,pstad-price=price.amount,"
            + "price.type1=price.type,price.type2=price.type,price.type3=price.type,price.type4=price.type,title=title,postad-title=title";

    private final String addMatchPart = "postad-img-=images";
    private final String strMatchListOfNonQuotedAttrs = "short_term.";
    private final String addMatchSpecial = ".condition_s";
    //"cars.carmake_s,cars.forsaleby_s,"
    private final Map<String, String> mapAlwaysRemove = new HashMap<>();
    private final Map<String, String> mapAlways = new HashMap<>();
    private final Map<String, String> mapListOfSpecialDatePeriodAttrs = new HashMap<>();
    private final Map<String, String> mapMatch = new HashMap<>();
    private final Map<String, String> mapMatchPart = new HashMap<>();
    private final String bbInput = "bbUserInput=5628,bbToken=1e5236ded5e8903554adb153c86607c1add9e8794d939aefaf5f3830fb539fda9222231a9c51c7e0ee6abaf33bddf0bdc911fcfec64ca6888c124006e8c7ee0a";
    private final String bbInputText = " verification code.";
    private JsonValue jsonObject;
    private ItemBidder itemBidder = new ItemBidder();
    private final int maxPage = 1;
    private final String strListOfSpecialDatePeriodAttrs = "short_term.availability_mtdtcsv=dateRangeMap[short_term.availability.availabilitystartdate];dateRangeMap[short_term.availability.availabilityenddate]";
    private final String const_postImageUrl = "https://www.gumtree.com.au/p-upload-image.html";
    private final String const_ImageUrl = "https://i.ebayimg.com";
    final static Gumtree gumtree = new Gumtree();
    final static Gumtree gumtree1 = new Gumtree();
    final static boolean repostAll = false;
    private static final int MAX_WAIT = 10;
    private static final int MAX_RETRY = 5;
    private String const_RedirectURL = null;
    private static PrintWriter out = null;
    private static final String const_YouAreNotAuthorised = "Sorry, you have attempted to access a page for which you are not authorized.";
    private static String searchFormLocationStr = "searchFormLocationStr: '";
    private static String[] cl = null;

    private final String addMatchPartName = "attributeMap[=attributeMap,datesMap=datesMap";
    private final Map<String, String> mapMatchPartName = new HashMap<String, String>();
    static final String strHasToBePaidFor = ">You must select a paid Ad Type: Plus, Featured or Premium</li>";

    static {
        try {
            //System.setProperty("org.apache.http.client.protocol.ResponseProcessCookies", "fatal");
            out = new PrintWriter(new BufferedWriter(new FileWriter("log.txt", false)));
        } catch (IOException e) {
            out = null;
        }
    }
    private int lastResponseCode = 200;
    private String adminEmail = "info@virtualcolleague.com.au";
    private static final String textarea = "</textarea>";
    private static final boolean waitWhileActioned = false;
    private static Map<String, String> userMap = new HashMap<>();
    private static final String NOT_RENEW = "__NOR__";
    private static final String ALWAYS_RENEW = "__ALR__";

    @Override
    protected void finalize() throws Throwable {
        super.finalize(); //To change body of generated methods, choose Tools | Templates.
        if (out != null) {
            out.close();
        }
    }

    public Gumtree() {
        for (String key : removeAlways.split(",")) {
            mapAlwaysRemove.put(key, key);
        }
        for (String key : addAlways.split(";")) {
            mapAlways.put(key.split("=")[0], key.split("=")[1].trim());
        }
        for (String key : strListOfSpecialDatePeriodAttrs.split(",")) {
            mapListOfSpecialDatePeriodAttrs.put(key.split("=")[0], key.split("=").length > 1 ? key.split("=")[1].trim() : null);
        }
        for (String key : addMatch.split(",")) {
            mapMatch.put(key.split("=")[0], key.split("=")[1].trim());
        }
        for (String key : addMatchPart.split(",")) {
            mapMatchPart.put(key.split("=")[0], key.split("=")[1].trim());
        }
        for (String key : addMatchPartName.split(",")) {
            mapMatchPartName.put(key.split("=")[0], key.split("=")[1].trim());
        }
    }

    public static void main(String[] args) throws Exception {
        cl = args;
        // text(Helper.fixEndOfSentance("FREE Shipping Australia Wide! International Shipping AvailableInstaLight case has released the first Selfie Light Case with an Internal PowerBank to never leave you out of charge!No more dead batteries.BOTH SIDES BRIGHT LED LIGHTING! (For the perfect selfie)DURABLE & SHOCKPROOF!UP TO 5 HOURS OF CHARGE! (internal 2500mAh powerbank)MICRO USB & IPHONE CHARGER INCLUDED!The perfect phone case that willLight Up Your Life !Www.instalightcase.comFeel free to call me on ******** 979 + click to reveal  Postage can be arranged anywhere in Aus"));

        Thread thread1 = new Thread(() -> gumtree1.manageReports());
        thread1.start();

        Thread thread2 = new Thread(() -> gumtree.run());
        thread2.start();
        //loadFromEbayItemId("162286229882");
        //String s = Utils.getTokenValid("vcBEsmKm1XBf6Lg2sjWyS2%2BTWPYPa4RAkVUagPsSL4w%3D");
        //text(sendPaid("ab1ed914c3c9938222c3f407b576ff9e"));
//Mail.sendEmail("tatiana_kr@hotmail.com", "test email gumtree", "This is a test email from gumtree");
    }

    private List<NameValuePair> addToNvps(String vid, String vname, String value, List<NameValuePair> nvps) {
        if (vid.contains("price") || vname.contains("price")) {
            vname = vname;
        }
        if (vname != null && vname.contains("greyimport_s")) {
            return nvps;
        }
        if (vid != null && mapMatch.containsKey(vid)) {
            value = checkKeyValue(mapMatch.get(vid), value);
            nvps.add(new BasicNameValuePair(mapMatch.get(vid), value));
        } else {
            //Changed_New
            for (String key : mapMatchPart.keySet().toArray(new String[0])) {
                if (vid != null && vid.startsWith(key)) {
                    value = checkKeyValue(mapMatchPart.get(key), value);
                    nvps.add(new BasicNameValuePair(mapMatchPart.get(key), value));
                    return nvps;
                }
            }
            //New
            for (String key : mapMatchPartName.keySet().toArray(new String[0])) {
                if (vname.startsWith(key)) {
                    value = checkKeyValue(mapMatchPartName.get(key), value);
                    nvps.add(new BasicNameValuePair(vname, value));
                    return nvps;
                }
            }
            if (vid != null && vid.contains(addMatchSpecial)) {
                nvps.add(new BasicNameValuePair("attributeMap['" + vid.split("-")[0] + "']", value));
            }
        }
        return nvps;
    }

    static boolean isClUser(String userMatch) {
        if (cl != null && cl.length > 0) {
            for (String user : cl) {
                if (user.equalsIgnoreCase(userMatch)) {
                    return true;
                }
            }
            return false;
        }

        return true;
    }

    public void run() {
        //while (true) {
        try {
            itemBidder = new ItemBidder();
            JsonObject json = sendGetUsers();
            JsonObject jsonSchedule = sendGetSchedule();

            for (JsonValue o : json.getJsonArray("users")) {
                jsonObject = o;
                username = ((JsonObject) o).getString("email");
                if (((JsonObject) o).getBoolean("enabled")) {
                    int plan = ((JsonObject) jsonObject).getInt("plan");
                    if (isClUser(username) && plan > 0) {
                        text("Started for: " + username);
                        password = ((JsonObject) o).getString("password");

                        if (login(true)) {
                            sendMessages();
                            if (!ItemBidder.isInAU()) {
                                loadFailed(true);
                            }
                            loadAdvFromOtherSources();
                            if (ItemBidder.isInAU()) {
                                getAddsToPost();
                                loadFailed(false);
                                List<String> list = getAllItems();
                                list = deleteAdv(list);
                                updateItems(list);
                                if (containsItem(jsonSchedule.getJsonArray("users"), (JsonObject) o)) {
                                    renewAdv();
                                }
                            } else {
                                text("You are outside Australia. No renewal will be attempted");
                            }
                        }

                        text("Finished renewal for: " + username);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        /* 
        finally {
                try {
                    Thread.sleep(1000 * 60 * 10);
                } catch (InterruptedException ex) {
                    return;
                }
            }
        } */
    }

    public void manageReports() {
        try {
            itemBidder = new ItemBidder();
            //if (itemBidder.proxy == null) {
            sendReport(gumtree.sendGetInactiveUsers(), templateInactiveUserReport);
            sendReport(gumtree.sendGetUsers(), templateActiveUserReport);
            //}
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private int getReportRunHour() {
        int i = 9;
        try {
            String rrh = utils.Helper.stringToMap(sendGetStatus(((JsonObject) jsonObject).getString("name"))).get("rrh");

            if (rrh == null) {
                sendSetStatus(((JsonObject) jsonObject).getString("name"), "rrh=" + i);
                text("Set report run hour");
            } else {
                i = Integer.valueOf(rrh);
            }
        } catch (Exception ex) {
            text("Report run hour is not yet set");
        }
        return i;
    }

    private Date getLastReportDate() {
        Date d1 = null;
        try {
            String lastReportTime = utils.Helper.stringToMap(sendGetStatus(((JsonObject) jsonObject).getString("name"))).get("report");
            if (lastReportTime != null) {
                d1 = new Date(Long.valueOf(lastReportTime));
            }
        } catch (Exception ex) {
            text("Last report date is not yet set");
        }
        return d1;
    }

    public Map<String, String> jsonObjectToMap(JsonObject json) {
        Map<String, String> map = new HashMap<>();
        for (JsonValue jsono : json.getJsonArray("items")) {
            for (String key : ((JsonObject) jsono).keySet()) {
                map.put(((JsonObject) jsono).getString("title").trim() + "$$$$" + key, ((JsonObject) jsono).get(key).toString());
            }
        }
        return map;
    }

    public void sendReport(JsonObject json, String templatePath) throws Exception {
        if (json != null) {
            try {
                String template = utils.Helper.loadFromFile(templatePath);
                for (JsonValue o : json.getJsonArray("users")) {
                    jsonObject = o;
                    if (new Date().getHours() == getReportRunHour()) {
                        Date d1 = getLastReportDate();
                        if (d1 == null || d1.getDate() < new Date().getDate()
                                || d1.getMonth() < new Date().getMonth() || d1.getYear() < new Date().getYear()) {
                            text("Started for: " + ((JsonObject) o).getString("email"));
                            username = ((JsonObject) o).getString("email");
                            password = ((JsonObject) o).getString("password");

                            if (login(false)) {
                                text("Sending report " + templatePath + " to user: " + username);
                                List<String> list = getAllItems();
                                Map<String, String> map = jsonObjectToMap(sendGetAllItems());
                                String items = "";
                                int itemsnotonfirstpage = 0;
                                for (String item : list) {
                                    JsonObject jsonItem = itemToJson(item);
                                    if (Integer.valueOf(jsonItem.getString("page")) > 1) {
                                        itemsnotonfirstpage++;
                                    }
                                    items += "\n" + utils.Helper.padRight(jsonItem.getString("title"), 80) + utils.Helper.padRight(jsonItem.getString("page"), 10) + map.get(jsonItem.getString("title").trim() + "$$$$totalvisits");
                                }
                                if (list.size() > 0) {
                                    sendMessage(((JsonObject) jsonObject).getString("name"),
                                            template.replace("<itemstotalcount>", String.valueOf(list.size()))
                                                    .replace("<itemsnotonfirstpage>", String.valueOf(itemsnotonfirstpage))
                                                    .replace("<plan>", String.valueOf(((JsonObject) o).getJsonNumber("plan")))
                                                    .replace("<schedule>", String.valueOf(((JsonObject) o).getJsonNumber("schedule")))
                                                    .replace("<items>", items.substring(1))
                                                    .replace("<date>", new Date().toLocaleString())
                                    );
                                    sendSetStatus(((JsonObject) jsonObject).getString("name"), "report=" + new Date().getTime());
                                }

                                text("Finished reports for: " + username);
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {

            }

        }
    }

    public JsonObject itemToJson(String item) throws Exception {
        String[] items = item.split("\n");
        return Json.createObjectBuilder().add("itemid", items[0])
                .add("userid", items[1])
                .add("title", items[2])
                .add("page", items[3])
                .add("visits", items[4])
                .add("minvisits", items.length > 8 ? items[8] : "0")
                .add("hourstomarket", items.length > 7 ? items[7] : "0")
                .add("managed", items.length > 6 ? items[6] : "false")
                .add("active", items[5]).build();
    }

    private List<String> getAllItems() throws Exception {
        List<String> list = new ArrayList<>();
        int j = getNumberOfPages(url2.replace("<page>", "1"), itemBidder);
        for (int i = 1; i <= j; i++) {
            String page = getPageByURL(url2.replace("<page>", String.valueOf(i)), itemBidder, null);

            list.addAll(getAllItems(page));
        }
        return list;
    }

    private void updateItems(List<String> list) throws Exception {

        for (String item : list) {
            JsonObject json = sendItem(((JsonObject) jsonObject).getString("name"), item);
        }
        JsonObject json = sendGetAllItems();
        for (JsonValue o : json.getJsonArray("items")) {
            if (!containsItem(list, (JsonObject) o)) {
                try {
                    o = parseJson(((JsonObject) o).toString().replace("\"active\":true", "\"active\":false"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                sendItem(((JsonObject) jsonObject).getString("name"), (JsonObject) o).toString();
            }
        }
        if (!list.isEmpty()) {
            deleteItems(list);
        }
    }

    private void deleteItems(List<String> list) throws Exception {
        JsonObject json = sendGetItemsToBeDeleted();
        for (JsonValue o : json.getJsonArray("items")) {
            String id = getItemId(list, (JsonObject) o);
            if (id != null) {
                getPageByURL(delete.replace("<id>", id), itemBidder, null);
                text(String.format("Deleted item: %s", id));
            } else {
                text(String.format("Cannot find item to delete: %s", o.toString()));
                text(sendDeleteItem(((JsonObject) o).getString("id")).toString());
            }
        }
    }

    private String getItemId(List<String> list, JsonObject json) {
        for (String item : list) {
            String[] el = item.split("\n");
            if (MD5Hash.md5Java(el[1] + ":" + el[2]).equals(MD5Hash.md5Java(json.getString("userid") + ":" + json.getString("title")))) {
                return el[0];
            }
        }
        return null;
    }

    private boolean containsItem(String title, JsonArray jsonArray) {
        for (JsonValue json : jsonArray) {
            if (((JsonObject) json).getString("title").trim().equalsIgnoreCase(title.trim())) {
                return true;
            }
        }
        return false;
    }

    private boolean containsItem(List<String> list, JsonObject json) {
        for (String item : list) {
            String[] el = item.split("\n");
            if (MD5Hash.md5Java(el[1] + ":" + el[2]).equals(MD5Hash.md5Java(json.getString("userid") + ":" + json.getString("title")))) {
                return true;
            }
        }
        return false;
    }

    private boolean containsItem(JsonArray json1, JsonObject json2) {
        for (JsonValue json : json1) {
            if (((JsonObject) json).getString("email").equalsIgnoreCase(json2.getString("email"))) {
                return true;
            }
        }
        return false;
    }

    private void sendMessages() throws Exception {
        File folder = new File(messagesFolder.replace("<username>", username));
        folder.mkdirs();
        text("Looking for messages in: " + folder.getAbsolutePath());

        for (File file : folder.listFiles()) {
            try {
                text("Found message to send: " + file.getAbsolutePath());
                sendMessage(((JsonObject) jsonObject).getString("name"), utils.Helper.loadFromFile(file.getAbsolutePath()));
            } catch (Exception ex) {
                text(ex.getMessage());

            } finally {
                file.delete();

            }
        }
    }

    private List<NameValuePair> addMapAddress(List<NameValuePair> nvps, String out) {
        String mapaddress = out.substring(out.indexOf(searchFormLocationStr)
                + searchFormLocationStr.length());
        mapaddress = mapaddress.substring(0, mapaddress.indexOf("'"));
        String postCode = Utils.getNumber(mapaddress);
        mapaddress = mapaddress.replace(postCode, "");
        String[] address = mapaddress.split(",");
        mapaddress = address[0] + ", " + address[1];// + ", " + postCode;
        nvps.add(new BasicNameValuePair("mapAddress", mapaddress));
        return nvps;
    }

    private void getAddsToPost() throws Exception {
        text("Started Adds to post for: " + username);
        JsonObject json = sendGetAddsToPost(((JsonObject) jsonObject).getString("name"));
        List<NameValuePair> nvps = null;
        for (JsonValue val : json.getJsonArray("addstopost")) {

            BufferedReader reader = new BufferedReader(new StringReader(Utils.decrypt(((JsonObject) val).getString("text"))));
            try {
                nvps = loadFromReader(reader);
            } catch (Exception ex) {
                ex.printStackTrace(System.out);
            } finally {
                reader.close();
            }
            stringToFile(nvps, getKey(nvps, "title"));
            JsonObject json1 = sendDeleteAddToPost(((JsonObject) jsonObject).getString("name"),
                    ((JsonObject) val).getJsonNumber("addid").longValue());
            if (json1 == null || !json1.getString("status").equalsIgnoreCase("OK")) {
                throw new Exception("sendDeleteAddToPost exception: " + json1 == null ? "null" : json1.toString());
            }
        }
    }

    private void putAddsToPost(List<NameValuePair> nvps) throws Exception {
        text("Started putAddsToPost for: " + username);
        JsonObject json = sendPutAddToPost(((JsonObject) jsonObject).getString("name"), nvpsToStringAsIs(nvps));

        if (json == null || !json.getString("status").equalsIgnoreCase("OK")) {
            throw new Exception("sendDeleteAddToPost exception: " + json == null ? "null" : json.toString());
        }

    }

    private void renewAdv() throws Exception {
        text("Started Adds renewal for: " + username);
        JsonObject json = sendGetItemsForRenewals(((JsonObject) jsonObject).getString("name"));

        List<String[]> list = new ArrayList<>();

        int j = getNumberOfPages(url2.replace("<page>", "1"), itemBidder);
        for (int i = 1; i <= j; i++) {
            String page = getPageByURL(url2.replace("<page>", String.valueOf(i)), itemBidder, null);

            list.addAll(getItemsToRenew(page));
        }

        list = filterExclusions(list);

        int plan = ((JsonObject) jsonObject).getInt("plan");
        if (plan > 0) {
            for (String[] items : list) {
                if (repostAll || containsItem(items[1], json.getJsonArray("items"))) {
                    String out = getPageByURL(edit_listing.replace("<id>", items[0]), itemBidder, null);
                    String description = getPageByURL(show_listing.replace("<id>", items[0]), itemBidder, null);
                    description
                            = com.intforce.utils.Helper.split(description, "<div id=\"ad-description-details\" ", "</div>").get(0);
                    //new com.intforce.html.parsers.HtmlParser(description).getBodyNamedTagTextById("div", "ad-description-details");
                    description = description.trim();

                    if (out.contains(NOT_RENEW)) {
                        continue;
                    }
                    List<NameValuePair> nvps = new ArrayList<NameValuePair>();
                    nvps.add(new BasicNameValuePair("description", description));
                    //nvps = addMapAddress(nvps,out);

                    for (String key : mapAlways.keySet().toArray(new String[0])) {
                        if (!hasKey(nvps, key)) {
                            nvps.add(new BasicNameValuePair(key, mapAlways.get(key)));
                        }
                    }

                    //Changed_New
                    for (String text : com.intforce.utils.Helper.split(out, "<input ", ">")) {
                        String vid = getValue(text, "id|name");
                        String vname = getValue(text, "name");
                        String value = getValue(text, "value");
                        String type = getValue(text, "type");
                        String checked = getValue(text, "checked");
                        if ((type.equalsIgnoreCase("radio") && checked.equalsIgnoreCase("checked"))
                                || (type.equalsIgnoreCase("checkbox") && checked.equalsIgnoreCase("checked"))
                                || (!type.equalsIgnoreCase("radio") && !type.equalsIgnoreCase("checkbox"))) {
                            nvps = addToNvps(vid, vname, value, nvps);
                        }

                    }
                    //New
                    for (String text : Helper.split(out, "<select ", "</select>", true)) {
                        String vname = getValue(text, "name");
                        String vid = getValue(text, "id|name");
                        text = text.substring(0, text.lastIndexOf("</select"));
                        for (String text1 : Helper.split(text, "<option ", "</option>", true)) {
                            text1 = text1.substring(0, text1.lastIndexOf("</option"));
                            String selected = getValue(text1, "selected");
                            if (selected.equalsIgnoreCase("selected")) {
                                nvps = addToNvps(vid, vname, getValue(text1, "value"), nvps);
                            }
                        }
                    }

                    for (String text : Helper.split(out, "<textarea ", textarea, true)) {
                        String vid = getValue(text, "id|name");
                        if (mapMatch.containsKey(vid)) {
                            text = text.substring(text.indexOf(">") + 1);
                            if (text.endsWith(textarea)) {
                                text = text.substring(0, text.length() - textarea.length());
                            }
                            if (!text.contains("\n")) {
                                text = Helper.fixEndOfSentance(text);
                            }
                            //String value = text.substring(0, text.length());
                            nvps.add(new BasicNameValuePair(mapMatch.get(vid), text));
                        }
                    }
                    out = out.substring(out.indexOf(attr) + attr.length());
                    out = out.substring(0, out.indexOf("}"));
                    for (String att : out.split(",")) {
                        String[] attrs = att.replace("\"", "").split(":");
                        if (!attrs[0].equalsIgnoreCase("id") && attrs.length > 1) {
                            attrs = addListOfSpecialDatePeriodAttrs(attrs, nvps, out);
                            attrs[1] = checkKeyValue(attrs[0], attrs[1]);
                            BasicNameValuePair v = new BasicNameValuePair("attributeMap[" + (containsAny(attrs[0], strMatchListOfNonQuotedAttrs) ? "" : "'") + attrs[0] + (containsAny(attrs[0], strMatchListOfNonQuotedAttrs) ? "" : "'") + "]", attrs[1]);
                            if (nvps.contains(v)) {
                                nvps.remove(v);
                            }
                            nvps.add(v);

                        }
                    }

                    saveAdvert(username, items[0], nvps);
                    stringToFile(nvps, items[0]);

                    out = getPageByURL(delete.replace("<id>", items[0]), itemBidder, null);
                    waitWhileDeleted(items[0], getKey(nvps, "title"));
                    text("Deleted advert: " + getKey(nvps, "title"));

                    try {
                        String adType = getKey(nvps, "adType");
                        if (adType.length() == 0) {
                            adType = "OFFER";
                        }
                        nvps = loadHiddenInput(const_PostAddUrl.replace("<type>", adType).replace("<category>", getKey(nvps, "categoryId")), nvps, true);
                        out = getPost(false, nvps);
                        if (!checkPost(false, out, nvps, items[0])) {

                        } else {
                            deleteFailedFile(items[0]);
                            plan--;
                            if (plan <= 0) {
                                break;
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        //sendSettimestamp(((JsonObject) jsonObject).getString("name"));
        text("Finished Adds renewal for: " + username);
    }

    private void waitWhilePosted(String id, String title) throws Exception {
        if (waitWhileActioned) {
            int i = 0;
            while (!isAdvertExists(edit_listing.replace("<id>", id), itemBidder, title) && i < MAX_WAIT) {
                sleep();
                i++;
            }
            if (i == MAX_WAIT) {
                text("Error. Advert id: " + id + " has not been posted");
                throw new Exception("Error. Advert id: " + id + " has not been posted");
            }
        }
    }

    private void waitWhileDeleted(String id, String title) throws Exception {
        if (waitWhileActioned) {
            int i = 0;
            while (isAdvertExists(edit_listing.replace("<id>", id), itemBidder, title) && i < MAX_WAIT) {
                sleep();
                i++;
            }
            if (i == MAX_WAIT) {
                text("Error. Cannot delete advert id: " + id);
                throw new Exception("Error. Cannot delete advert id: " + id);
            }
        }
    }

    private final void saveAdvert(String user, String id, List<NameValuePair> nvps) {
        String title = getKey(nvps, "title");
        nvpsToFile(saveFolder.replace("<username>", user), nvps, id + " " + title);
    }

    private final List<NameValuePair> removeAllKeys(List<NameValuePair> nvps, String key) {
        List<NameValuePair> list = new ArrayList<>();
        for (NameValuePair p : nvps.toArray(new NameValuePair[0])) {
            if (!p.getName().equalsIgnoreCase(key)) {
                list.add(p);
            }
        }
        return list;
    }

    private final List<String> getKeys(List<NameValuePair> nvps, String key) {
        List<String> list = new ArrayList<>();
        for (NameValuePair p : nvps.toArray(new NameValuePair[0])) {
            if (p.getName().equalsIgnoreCase(key)) {
                list.add(p.getValue());
            }
        }
        return list;
    }

    private final String getKey(List<NameValuePair> nvps, String key) {
        for (NameValuePair p : nvps.toArray(new NameValuePair[0])) {
            if (p.getName().equalsIgnoreCase(key)) {
                return p.getValue();
            }
        }
        return "";
    }

    private final boolean hasKey(List<NameValuePair> nvps, String key) {
        for (NameValuePair p : nvps.toArray(new NameValuePair[0])) {
            if (p.getName().equalsIgnoreCase(key)) {
                return true;
            }
        }
        return false;
    }

    private final boolean hasKeyValue(List<NameValuePair> nvps, String key, String value) {
        for (NameValuePair p : nvps.toArray(new NameValuePair[0])) {
            if (p.getName().equalsIgnoreCase(key) && p.getValue().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    private List<NameValuePair> setKeyValues(List<NameValuePair> nvps, String key, String value) {

        for (int i = 0; i < nvps.size(); i++) {
            NameValuePair p = nvps.get(i);
            if (p.getName().equals(key)) {
                nvps.set(i, new BasicNameValuePair(p.getName(), value));
                break;
            }
        }
        return nvps;
    }

    private List<NameValuePair> replaceKeyValues(List<NameValuePair> nvps, String key, String value1, String value2) {

        for (int i = 0; i < nvps.size(); i++) {
            NameValuePair p = nvps.get(i);
            if (p.getName().equals(key)) {
                nvps.set(i, new BasicNameValuePair(p.getName(), Helper.replace(p.getValue(), value1, value2)));
                break;
            }
        }
        return nvps;
    }

    private List<NameValuePair> replaceAllKeyValues(List<NameValuePair> nvps, String value1, String value2) {

        for (int i = 0; i < nvps.size(); i++) {
            NameValuePair p = nvps.get(i);
            nvps.set(i, new BasicNameValuePair(p.getName().replace(value1, value2), p.getValue().replace(value1, value2)));
        }
        return nvps;
    }

    private void deleteKeyByValue(List<NameValuePair> nvps, String value) {
        int i = 0;
        for (NameValuePair p : nvps.toArray(new NameValuePair[0])) {
            if (p.getValue().equals(value)) {
                nvps.remove(p);
            }
        }
    }

    private void deleteKey(List<NameValuePair> nvps, String value) {
        int i = 0;
        for (NameValuePair p : nvps.toArray(new NameValuePair[0])) {
            if (p.getName().equals(value)) {
                nvps.remove(p);
            }
        }
    }

    private final void loadAdvFromOtherSources() throws Exception {
        File file = new File(loadFile.replace("<username>", username));
        text("Looking for adverts to load in : " + file.getAbsolutePath());
        if (file.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    String[] advs = line.split(";");
                    text("Found advert to load: " + line);
                    switch (advs[0]) {
                        case "grays":
                            loadFromGraysLotId(advs);
                            break;
                        case "ebay":
                            loadFromEbayItemId(advs);
                            break;
                    }

                }

            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                reader.close();
                file.delete();
            }
        }
    }

    private String getItemIdbyTitle(String title, List<String> list) {
        for (String item : list) {
            if (title.trim().equalsIgnoreCase(item.split("\n")[2])) {
                return item;
            }
        }
        return null;
    }

    private List<String> deleteAdv(List<String> list) throws Exception {
        File folder = new File(deleteFolder.replace("<username>", username));
        folder.mkdirs();
        text("Looking for adverts to delete in: " + folder.getAbsolutePath());
        for (File file : folder.listFiles()) {
            if (file.isFile()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                try {
                    text("Found advert to delete: " + file.getAbsolutePath());
                    List<NameValuePair> nvps = loadFromReader(reader);
                    String item = getItemIdbyTitle(getKey(nvps, "title"), list);
                    if (item != null) {
                        String id = item.split("\n")[0];
                        list.remove(item);
                        getPageByURL(delete.replace("<id>", id), itemBidder, null);
                        text("Deleted advert by id: " + id);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace(System.out);
                } finally {
                    reader.close();
                    file.delete();
                }
            }
        }
        return list;
    }

    private Map<String, String> loadCategories() {

        try {
            String page = getPageByURL(URL_CATEGORY, itemBidder, null);

            for (String cat : Helper.split(page, DATA_CAT_ID_PATERN, ">")) {
                cat = cat.replace(">", "");
                cat = cat.split("\"")[cat.split("\"").length - 1];
                new Gumtree().loadJsonCats(cat, null);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return map;
    }

    private void loadJsonCats(String id, JsonObject jsonCat) throws Exception {
        JsonObject json = parseJson(new HttpGet(new URI(catJsonUrl.replace("<id>", id))));
        if (json.getString("headline").equals("Sub Category")) {
            for (JsonValue value : json.getJsonArray("items")) {
                loadJsonCats(((JsonObject) value).getString("id"), (JsonObject) value);
            }
        } else {

            String page = getPageByURL(const_PostAddUrl.replace("<id>", id).replace("<type>", "OFFER"), itemBidder, null);
            List<String> list = Helper.split(page, const_parseJSON, const_PostAdForm);
            if (list.size() > 0) {
                page = Helper.split(page, const_parseJSON, const_PostAdForm).get(0).substring(const_parseJSON.length() - 1);
                page = page.substring(page.indexOf("{"));
                page = page.substring(0, page.lastIndexOf("}") + 1);
                json = parseJson(page);
                page = ((JsonObject) json.getJsonObject("o").getJsonArray("oi").toArray()[0]).getJsonObject("a").getJsonObject("attr").keySet().toArray()[0].toString().split("\\.")[0];
                map.put(id, jsonCat.getString("value") + ";" + page);
                text("Added category: " + map.get(id));
            }
        }
    }

    private String normaliseDescription(String s) {
        s = s.replace("&nbsp;", " ").replace("\r", "");
        while (s.contains("  ")) {
            s = s.replace("  ", " ");
        }
        s = s.trim();
        s = s.replace(" \n", "\n");
        while (s.contains("\n\n\n")) {
            s = s.replace("\n\n\n", "\n");
        }
        return s;
    }

    private String normaliseString(String s) {
        s = s.replace("&nbsp;", " ").replace("\r", "");
        while (s.contains("  ")) {
            s = s.replace("  ", " ");
        }
        s = s.trim();
        s = s.replace(" \n", "\n");
        while (s.contains("\n\n")) {
            s = s.replace("\n\n", "\n");
        }
        return s;
    }

    private static String getMatchOrFirst(JsonArray array, String match, String keywords) {
        for (JsonValue val : array) {
            if (val.toString().toLowerCase().equals(match.toLowerCase())) {
                return val.toString();
            }
        }
        int j = 0;
        String ret = null;
        for (JsonValue val : array) {
            int i = 0;
            for (String s : keywords.split("\\W+")) {
                if (val.toString().toLowerCase().contains(s.toLowerCase())) {
                    i++;
                }
            }
            if (i > j) {
                ret = val.toString();
                j = i;
            }
        }
        return ret == null ? array.isEmpty() ? null : array.getString(0) : ret;
    }

    private List<NameValuePair> loadFromGraysLotId(String[] advs) throws Exception {
        JsonObject json = null;
        String lotID = advs[1];
        String keywords = advs.length > 2 && advs[2].trim().length() > 0 ? advs[2].trim() : null;
        Long price = advs.length > 3 && advs[3].trim().length() > 0 ? Long.valueOf(advs[3].trim()) : null;

        String page = getPageByURL(const_graysLotUrl.replace("<lot>", lotID), itemBidder, null);
        HtmlParser parser = new HtmlParser(page);

        json = parseJson(parser.getTextBetweenTags("dataLayer = [", "];").replace("'", "\""));
        if (!json.keySet().contains("Condition")) {
            String[] arr = parser.getTextBetweenTags("<link rel=\"canonical\" href=\"", "\"").split("/");
            parser = new HtmlParser(getPageByURL(
                    const_graysLotUrl.replace("<lot>", lotID) + "/" + arr[arr.length - 2] + "/" + arr[arr.length - 1] + "?redirect=0", itemBidder, null));
        }
        String value = parser.getTopLevelNamedTagTextByClass("div", "content-section");
        value = Helper.replace(value, const_GSTnote, "");
        String desc = StringEscapeUtils.unescapeHtml4(value);

        String title = parser.getBodyOfNamedTagTextByClass("h1", "entry-title").get(0).trim();
        title = title.substring(title.lastIndexOf(">") + 1);

        String cat = getCategory((keywords == null ? "" : keywords + " ") + title);
        text(String.format("Determined category: %s", cat));

        List<NameValuePair> nvps = null;
        File file = new File(templateItem + "." + cat.split(";")[0]);
        if (!file.exists()) {
            file = new File(templateItem);
        }
        BufferedReader reader = new BufferedReader(new FileReader(file));
        try {
            nvps = loadFromReader(reader);
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        } finally {
            reader.close();
        }
        if (hasKey(nvps, "attributeMap['<category>.fueltype_s']")) {
            String carbodytype = null;
            String variant = "";

            String carmileageinkms = desc.toLowerCase().contains("<li>odometer") ? desc.substring(desc.toLowerCase().indexOf("<li>odometer") + "<li>odometer".length()) : "1";
            //carmileageinkms = carmileageinkms.contains("</li>") ? carmileageinkms.substring(0, carmileageinkms.indexOf("</li>")) : "1";
            //carmileageinkms = carmileageinkms.contains(":") ? carmileageinkms.split(":")[1] : "1";
            carmileageinkms = carmileageinkms.trim().replace(",", "");
            carmileageinkms = String.valueOf(Helper.getMoney(carmileageinkms));
            carmileageinkms = carmileageinkms.split("\\.")[0];
            nvps = replaceAllKeyValues(nvps, "<carmileageinkms>", carmileageinkms);
            //2007 Mercedes C220 Turbo Diesel Automatic Sedan
            String[] arr = title.toLowerCase().replace("mercedes benz", "mercedes-benz").split(" ");

            if (!Utils.isStringNumeric(arr[0])) {
                json = parseJson(new HttpGet(new URI(jsonUrlGet.replace("<make>", arr[0]).replace("<model>", arr[1]))));
                arr = (json.getJsonArray("years").getString(0) + " " + title).toLowerCase().replace("mercedes benz", "mercedes-benz").split(" ");
            }

            if (cat.split(";")[0].equals("18320")) {
                json = parseJson(new HttpGet(new URI("https", jsonUrlBase, jsonUrlGet.split("\\?")[0], jsonUrlGet.split("\\?")[1].replace("<make>", arr[1]).replace("<model>",
                        arr[2]) + "&year=" + arr[0], null)));
                variant = getMatchOrFirst(json.getJsonArray("variants"), arr[3], title);
                if (variant != null) {
                    json = parseJson(new HttpGet(new URI("https", jsonUrlBase, jsonUrlGet.split("\\?")[0], jsonUrlGet.split("\\?")[1].replace("<make>", arr[1]).replace("<model>",
                            arr[2]) + "&year=" + arr[0] + "&variant=" + variant, null)));
                    String nvic = json.getString("nvic");
                    if (nvic.isEmpty()) {
                        nvic = ((JsonObject) ((JsonArray) (json.getJsonArray("vehicleFeatures").get(0))).get(0)).getString("o2");
                    }
                    json = parseJson(new HttpGet(new URI(nvicUrlGet.replace("<nvic>", nvic))));
                    carbodytype = json.getString("carbodytype");
                } else {
                    variant = "";
                    carbodytype = title.toLowerCase().contains("van") ? "vanmini" : title.toLowerCase().contains("ute") ? "ute" : "othrbdytyp";
                    arr[2] = "othrmdl";
                }
                arr[1] = arr[1].replace("mercedes-benz", "mercedes");
            } else {
                arr[1] = arr[1].replace("mercedes-benz", "mercedesbenz");
            }

            if (carbodytype != null) {
                nvps = replaceAllKeyValues(nvps, "<carbodytype>", carbodytype);
            }

            nvps = replaceAllKeyValues(nvps, "<year>", arr[0]);
            nvps = replaceAllKeyValues(nvps, "<make>", arr[1]);
            nvps = replaceAllKeyValues(nvps, "<model>", arr[2].replace("-", ""));
            nvps = replaceAllKeyValues(nvps, "<variant>", variant);
            nvps = replaceAllKeyValues(nvps, "<fueltype>", Helper.split(parser.getBody(), "<div title=\"Description\"", "</div>").get(0).toLowerCase().contains("diesel") ? "diesel" : "unleaded");
        }
        desc = desc.toLowerCase().contains("<li>location: ") ? desc.substring(0, desc.toLowerCase().indexOf("<li>location: ")) : desc;
        desc = desc.replace("<p>", "&lt;br&gt;").replace("</p>", "&lt;br&gt;").replace("<li>", "&lt;br&gt;").replace("<br>", "&lt;br&gt;").replace("</br>", "&lt;br&gt;");
        desc = Helper.replaceAllBetweenTags(desc).replace("\n", "").replace("\r", "").replace("\t", " ").trim();
        nvps = replaceAllKeyValues(nvps, "<description>", desc);
        json = parseJson(parser.getTextBetweenTags("dataLayer = [", "];").replace("'", "\""));
        if (json.keySet().contains("Condition")) {
            value = mapCondition(json.getString("Condition"));
        } else {
            value = "Reconditioned";
        }
        nvps = replaceAllKeyValues(nvps, "<condition>", value);

        title = value.equalsIgnoreCase("new") ? value.toUpperCase() + " " + title : title;

        nvps = replaceAllKeyValues(nvps, "<title>", title.length() > 65 ? title.substring(0, 65) : title);

        price = (price == null
                ? Long.valueOf(String.valueOf(Math.round(Helper.getMoney((new HtmlParser(parser.getBodyNamedTagTextById("select", "price")).
                        getTagTextByProperty("option", "selected", "selected"))) * 2)).split("\\.")[0]) : price);
        nvps = replaceAllKeyValues(nvps, "<price>", String.valueOf(price));
        nvps = replaceAllKeyValues(nvps, "<minimumOfferPrice>", String.valueOf(price - 100));

        int i = 1;
        for (String im : Helper.split(parser.getTopLevelNamedTagTextByClass("div", const_graysImageClass),
                "<img ", ">")) {
            nvps = replaceAllKeyValues(nvps, String.format("<image%s>", i++),
                    new HtmlParser(im).getPropertyValue("src").replace("=gt", "=gl"));
        }
        for (; i <= 12; i++) {
            deleteKeyByValue(nvps, String.format("<image%s>", i));
        }

        nvps = replaceAllKeyValues(nvps, "<categoryid>", cat.split(";")[0]);
        nvps = replaceAllKeyValues(nvps, "<category>", cat.split(";")[2]);

        //closes
        List<String> values = parser.getBodyNamedTagPropertyValueByClass("abbr", "endtime", "title");
        i = 0;
        if (!values.isEmpty()) { //-14T22:00:00+10:0
            i = (int) TimeUnit.HOURS.convert(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").parse(values.get(0)).getTime() - new Date().getTime(),
                    TimeUnit.MILLISECONDS);
        }
        nvps = replaceAllKeyValues(nvps, "<hourstomarket>", i < 2 ? "24" : String.valueOf(i - 1));

        if (!ItemBidder.isInAU()) {
            stringToFile(nvps, lotID);
        } else {
            putAddsToPost(nvps);
            sendItem(((JsonObject) jsonObject).getString("name"), nvpsToJsonItem(nvps));
        }
        return nvps;
    }

    String mapCondition(String cond) {
        if (cond.equalsIgnoreCase("Demo")) {
            return "new";
        } else if (cond.equalsIgnoreCase("new")) {
            return "new";
        } else if (cond.equalsIgnoreCase("Retail Return")) {
            return "new";
        } else if (cond.equalsIgnoreCase("Reconditioned")) {
            return "new";
        }
        return "used";
    }

    private List<NameValuePair> loadFromEbayItemId(String[] advs) throws Exception {
        String itemID = advs[1];
        String keywords = advs.length > 2 ? advs[2] : null;
        String price = advs.length > 3 ? advs[3] : null;

        Item item = new EbayItemParser(com.intforce.utils.Helper.getHttpPage("http://www.ebay.com.au/itm/" + itemID), null).getItem();

        List<NameValuePair> nvps = null;
        BufferedReader reader = new BufferedReader(new FileReader(new File(templateItem)));
        try {
            nvps = loadFromReader(reader);
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        } finally {
            reader.close();
        }

        String desc = item.getDescription();
        desc = normaliseString(desc.replaceAll("\\<.*?\\>", "\n"));
        if (desc.contains("<")) {
            desc = desc.substring(desc.lastIndexOf("<"));
        }
        nvps = replaceAllKeyValues(nvps, "<description>", desc);
        String cat = getCategory(keywords == null ? desc : keywords);
        text(String.format("Determined category: %s", cat));

        nvps = replaceAllKeyValues(nvps, "<categoryid>", cat.split(";")[0]);
        nvps = replaceAllKeyValues(nvps, "<category>", cat.split(";")[2]);

        nvps = replaceAllKeyValues(nvps, "<title>", item.getTitle().length() > 65 ? item.getTitle().substring(0, 65) : item.getTitle());
        nvps = replaceAllKeyValues(nvps, "<price>", price == null ? String.valueOf(item.getFixedprice().intValue()) : price);
        String[] cond = item.getCondition().getName().toLowerCase().split("\\W+");
        nvps = replaceAllKeyValues(nvps, "<condition>", cond[cond.length - 1]);
        int i = 1;
        for (com.intforce.db.entity.Image im : item.getImageList()) {
            nvps = replaceAllKeyValues(nvps, String.format("<image%s>", i++), im.getUrl());
        }
        for (; i <= 12; i++) {
            deleteKeyByValue(nvps, String.format("<image%s>", i));
        }

        nvps = replaceAllKeyValues(nvps, "<hourstomarket>", "240");

        stringToFile(nvps, itemID);
        sendItem(((JsonObject) jsonObject).getString("name"), nvpsToJsonItem(nvps));

        return nvps;
    }

    private List<NameValuePair> removeBBimageT(List<NameValuePair> nvps) {
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

    private boolean checkPost(boolean login, String out, List<NameValuePair> nvps, String id) throws Exception {
        int i = 0;
        while (true) {
            while (out.trim().length() > 0 && !(login && out.trim().contains(loginError))) {
                if (!login && !fileExists(id)) {
                    stringToFile(nvps, id);
                }
                if (out.contains(const_YouAreNotAuthorised)) {
                    text("Failed to post: " + getKey(nvps, "title"));
                    return false;
                }
                if (out.contains(SELECT_PAID)) {
                    //text("Failed to post " + getKey(nvps, "title") + ". To post this advert " + SELECT_PAID.toLowerCase());
                    //return true;
                }
                if ((lastResponseCode == HttpStatus.SC_INTERNAL_SERVER_ERROR && i < 3)
                        || (out.contains(bbInputText) && out.contains(bb_token) && Helper.containsAny(out, pleaseEnterVC.split("\\|")))) {
                    if (lastResponseCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                        i++;
                    }
                    nvps = removeBBimageT(nvps);

                    out = out.substring(out.indexOf(bb_token) + bb_token.length());
                    out = out.substring(0, out.indexOf("\""));
                    int r = randInt(51, 69);
                    String bbUserInputValue = getNumber(bb_image.replace("<id>", out), username, 68); //getBbUserInput(bb_image.replace("<id>", out));
                    text("Random: " + r + ". Got bbUserInputValue: " + bbUserInputValue + " for: " + (login ? "login" : id));
                    nvps.add(new BasicNameValuePair("bbUserInput", bbUserInputValue));
                    nvps.add(new BasicNameValuePair("bbToken", out));
                    out = getPost(login, nvps);
                } else {
                    break;
                }
            }

            if (out.trim().length() > 0 && !login) {
                text("Failed to post: " + getKey(nvps, "title") + " Trying again...");
                if (!login) {
                    stringToFile(out, id);
                    if (out.contains(strHasToBePaidFor)) {
                        stringToFile(nvps, id, outFolder);
                        deleteIfExists(id);
                    } else {
                        stringToFile(nvps, id);
                    }
                }
                return false;
            } else if (!login) {
                if (const_RedirectURL != null && const_RedirectURL.contains("adId=")) {
                    String adid = const_RedirectURL.substring(const_RedirectURL.indexOf("adId=") + 5);
                    adid = adid.substring(0, adid.indexOf("&"));
                    waitWhilePosted(adid, getKey(nvps, "title"));
                    text("Posted advert: " + getKey(nvps, "title"));

                    deleteIfExists(id);
                    return sendRenewed(((JsonObject) jsonObject).getString("name"), getKey(nvps, "title")) != null;
                } else {
                    if (const_RedirectURL != null) {
                        text("Failed to post: " + getKey(nvps, "title") + " Trying again...");
                        out = getPageByURL(const_RedirectURL.startsWith("/") ? baseURL + const_RedirectURL : const_RedirectURL, itemBidder, null);
                    } else {
                        text("Failed to post: " + getKey(nvps, "title"));
                        stringToFile(nvps, id);
                        return false;
                    }
                }
            } else {
                return !getPageByURL(url2.replace("<page>", "1"), itemBidder, null).contains(loginPage) && !out.trim().contains(loginError);
            }
        }
    }

    private String checkKeyValue(String key, String value) {

        if (key.equalsIgnoreCase("images")) {
            value = value.replace("$_14.", "$_20.");
        } else if (key.replace("'", "").replace("]", "").endsWith("_tdt") && value.contains("T")) {
            value = value.split("T")[0] + "T00:00:00Z";
        }

        return value;
    }

    private List<NameValuePair> loadHiddenInput(String url, List<NameValuePair> nvps, boolean bbUserInput) throws Exception {
        String page = getPageByURL(url, itemBidder, null);
        for (String tag : Helper.split(page, "<input type=\"hidden\"", ">")) {
            HtmlParser parser = new HtmlParser(tag);
            String key = parser.getPropertyValue("name");
            if (key != null) {
                if (!hasKey(nvps, key)) {
                    nvps.add(new BasicNameValuePair(key, parser.getPropertyValue("value")));
                }
            }
        }
        if (page.contains(bbInputText) && page.contains(bb_token)) {
            nvps = removeBBimageT(nvps);

            String out = page.substring(page.indexOf(bb_token) + bb_token.length());
            out = out.substring(0, out.indexOf("\""));
            if (bbUserInput) {
                for (int i = 0; i < MAX_RETRY; i++) {
                    try {
                        String bbUserInputValue = getNumber(bb_image.replace("<id>", out), username, 68); //getBbUserInput(bb_image.replace("<id>", out));
                        nvps.add(new BasicNameValuePair("bbUserInput", bbUserInputValue));
                    } catch (Exception e) {
                        Thread.sleep(MAX_WAIT * 1000);
                    }
                }
            }
            nvps.add(new BasicNameValuePair("bbToken", out));
        }
        return nvps;
    }

    private List<NameValuePair> removeAddAways(List<NameValuePair> nvps) throws Exception {
        for (String key : mapAlways.keySet().toArray(new String[0])) {
            if (!hasKey(nvps, key)) {
                nvps.add(new BasicNameValuePair(key, mapAlways.get(key)));
            }
        }
        for (String key : mapAlwaysRemove.keySet().toArray(new String[0])) {
            if (hasKey(nvps, key)) {
                deleteKey(nvps, key);
            }
        }
        return nvps;
    }

    boolean isAdvertDuplicate(List<NameValuePair> nvps) throws Exception {
        for (String item : getAllItems()) {
            if (hasKeyValue(nvps, "title", item.split("\n")[2])) {
                return true;
            }
        }
        return false;
    }

    private void loadFailed(boolean putAddsToPostOnly) throws Exception {
        File folder = new File(failedFolder.replace("<username>", username));
        folder.mkdirs();
        text("Looking for failed files in: " + folder.getAbsolutePath());
        for (File file : folder.listFiles()) {
            if (file.isFile()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String out = "";
                List<NameValuePair> nvps1 = null;
                boolean duplicate = false;
                try {
                    text("Found file to load: " + file.getAbsolutePath());
                    List<NameValuePair> nvps = loadFromReader(reader);
                    nvps = removeAddAways(nvps);
                    nvps = upLoadImages(nvps);

                    for (int j = 0; j < MAX_RETRY; j++) {
                        String adType = getKey(nvps, "adType");
                        if (adType.length() == 0) {
                            adType = "OFFER";
                        }
                        nvps1 = loadHiddenInput(const_PostAddUrl.replace("<type>", adType).replace("<category>", getKey(nvps, "categoryId")), nvps, true);
                        try {
                            duplicate = isAdvertDuplicate(nvps1);
                            if (!duplicate) {
                                if (putAddsToPostOnly) {
                                    putAddsToPost(nvps);
                                    text("Done putAddsToPost() - " + getKey(nvps1, "title"));
                                    break;
                                } else {
                                    text("Posting: " + file.getAbsolutePath());
                                    out = getPost(false, nvps1);
                                    text("Posted but needs be checked: " + file.getAbsolutePath());
                                }
                            } else {
                                text("Found duplicate advert to load. " + getKey(nvps1, "title") + " Skipping..");
                            }
                            break;
                        } catch (ForbiddenException ex) {
                            text(ex.getMessage() + ". loadFailed(). Retrying...");
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace(System.out);
                } finally {
                    reader.close();
                    file.delete();
                    try {
                        if (!putAddsToPostOnly) {
                            if (!duplicate && checkPost(false, out, nvps1, UUID.randomUUID().toString().replace("-", ""))) {

                                sendItem(((JsonObject) jsonObject).getString("name"),
                                        const_Item.replace("<id>", UUID.randomUUID().toString())
                                                .replace("<user>", ((JsonObject) jsonObject).getString("email"))
                                                .replace("<title>", getKey(nvps1, "title"))
                                                .replace("<page>", "1")
                                                .replace("<visits>", "0")
                                                .replace("<minvisits>", "0")
                                                .replace("<hourstomarket>", "24")
                                                .replace("<managed>", "false")
                                                .replace("<active>", "true"));

                                saveAdvert(username, "NEW", nvps1);
                            }
                        }
                    } catch (Exception e) {

                    }
                }
            }
        }
    }

    private List<NameValuePair> loadFromReader(BufferedReader reader) throws IOException {
        List<NameValuePair> nvps = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains("=")) {
                String key = line.split("=")[0];
                String value = line.length() > line.indexOf("=") + 1 ? line.substring(line.indexOf("=") + 1) : "";
                value = checkKeyValue(key, value);
                nvps.add(new BasicNameValuePair(key,
                        value.replace("\r", "").replace("&lt;br&gt;", "\n")));
            }
        }
        return nvps;
    }

    private static synchronized void text(String txt) {
        String text = new Date().toString() + ": " + txt;
        System.out.println(text);
        if (out != null && !text.contains("Retrying...")) {
            out.println(text);
            out.flush();
        }
    }

    private String getBbUserInput(String url) throws IOException {
        Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start chrome " + url});

        String code = "";
        while (code.length() != 4) {
            text("Enter verification code: ");
            byte[] b = new byte[4];
            System.in.read(b, 0, 4);
            code = new String(b, 0, 4);
            code = code.replaceAll("[^0-9.]", "");
        }
        return code;
    }

    private void deleteIfExists(String fileName) {
        File file = new File(failedFolder.replace("<username>", username) + "/" + getFileName(fileName));
        if (file.exists()) {
            file.delete();
            text("Deleted file " + file.getAbsolutePath());
        }
    }

    private boolean fileExists(String fileName) {
        File file = new File(failedFolder.replace("<username>", username) + "/" + getFileName(fileName));
        return file.exists();
    }

    //New
    private void stringToFile(List<NameValuePair> nvps, String fileName) {
        stringToFile(nvps, fileName, null);
    }

    //Changed_New
    private void stringToFile(List<NameValuePair> nvps, String fileName, String folder) {
        nvpsToFile((folder == null ? failedFolder : folder).replace("<username>", username), nvps, fileName);
    }

    private void stringToFile(String out, String fileName) {
        try {
            String folder = outFolder.replace("<username>", username);

            new File(folder).mkdirs();
            File file = new File(folder + "/" + getFileName(fileName) + ".html");

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(out);
            bw.close();
            text("Saved post to: " + file.getAbsolutePath());
        } catch (IOException e) {
            text("Error: " + e);
            e.printStackTrace();
        }
    }

    private void deleteFailedFile(String fileName) {
        File file = new File(failedFolder.replace("<username>", username) + "/" + getFileName(fileName));
        if (file.exists()) {
            file.delete();
        }
    }

    private String getFileName(String fileName) {
        return fileName.replace("/", "-").replace("\\", "-") + ".txt";
    }

    private void mapToFile(String folder, Map<String, String> nvps, String fileName) {
        try {
            new File(folder).mkdirs();
            File file = new File(folder + "/" + getFileName(fileName));
            String out = "";
            for (String v : nvps.keySet()) {
                out += v + "=" + nvps.get(v).replace("\r", "").replace("\n", "&lt;br&gt;") + "\n";
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
            text("Saved post to: " + file.getAbsolutePath());
            //text("Done writing to " + fileName); //For testing 
        } catch (IOException e) {
            text("Error: " + e);
            e.printStackTrace();
        }
    }

    private String nvpsToStringAsIs(List<NameValuePair> nvps) {
        String out = "";
        for (NameValuePair v : nvps) {
            out += v.getName() + "=" + v.getValue() + "\n";
        }
        return out;
    }

    private String nvpsToString(List<NameValuePair> nvps) {
        String out = "";
        for (NameValuePair v : nvps) {
            out += v.getName() + "=" + v.getValue().replace("\r", "").replace("&lt;br&gt;", "\n") + "\n";
        }
        return out;
    }

    private void nvpsToFile(String folder, List<NameValuePair> nvps, String fileName) {
        try {
            new File(folder).mkdirs();
            File file = new File(folder + "/" + getFileName(fileName));
            String out = "";
            for (NameValuePair v : nvps) {
                out += v.getName() + "=" + v.getValue().replace("\r", "").replace("\n", "&lt;br&gt;")
                        .replace("<br>", "&lt;br&gt;")
                        .replace("<br/>", "&lt;br&gt;") + "\n";
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
            text("Saved post to: " + file.getAbsolutePath());
            //text("Done writing to " + fileName); //For testing 
        } catch (IOException e) {
            text("Error: " + e);
            e.printStackTrace();
        }
    }

    private List<NameValuePair> upLoadImages(List<NameValuePair> nvps) throws Exception {
        List<String> list = getKeys(nvps, "images");
        nvps = removeAllKeys(nvps, "images");
        for (String image : list) {
            if (!Strings.checkNullBlankString(image) && !image.startsWith(const_ImageUrl)) {
                boolean deleteSavedImg = false;
                if (image.startsWith("http")) {
                    image = image.replace("&amp;", "&");
                    image = utils.Helper.saveBytesToFile(imageFolder.replace("<username>", username) + "\\"
                            + UUID.randomUUID().toString().replace("-", "") + ".jpg",
                            com.intforce.utils.Helper.getImage(image));
                    deleteSavedImg = true;
                }
                JsonObject json = null;
                for (int i = 0; i < MAX_RETRY; i++) {
                    HttpPost post = null;
                    try {
                        post = new HttpPost(new URI(const_postImageUrl));
                        File file = new File(image);
                        if (!file.exists()) {
                            throw new Exception("File " + file.getPath() + " does not exist");
                        }
                        FileBody pic = new FileBody(file);
                        StringBody name = new StringBody(file.getName());

                        MultipartEntity requestEntity = new MultipartEntity();
                        requestEntity.addPart("text", name);
                        requestEntity.addPart("file", pic);

                        post.setEntity(requestEntity);
                        text("Uploading image: " + file.getPath());

                        json = parseJson(sendPost(post));
                    } catch (GumtreeOtherException ex) {
                        throw ex;
                    } catch (Exception ex) {
                        if (ex.getMessage().toLowerCase().contains("does not exist")) {
                            break;
                        }
                        text(ex.getMessage() + ". upLoadImages(). Retrying...");
                    } finally {
                        post.releaseConnection();
                    }

                    if (json != null) {
                        break;
                    }
                }

                if (json != null) {
                    if (!json.getString("status").equalsIgnoreCase("OK")) {
                        text(json.toString());
                        throw new Exception("New image uploading exception");
                    }
                    nvps.add(new BasicNameValuePair("images", json.getString("XLargeUrl")));
                    if (deleteSavedImg) {
                        text(String.format("Deleting file: %s", new File(image).getPath()));
                        new File(image).delete();
                    }
                }
//{"XLargeAlias":"extraLarge","XLargeSizeId":20,"XLargeUrl":"https://i.ebayimg.com/00/s/NjAwWDgwMA==/z/5ZUAAOSwa~BYPpRW/$_20.JPG","XXLargeAlias":"extraExtraLarge","XXLargeUrl":"https://i.ebayimg.com/00/s/NjAwWDgwMA==/z/5ZUAAOSwa~BYPpRW/$_57.JPG","emailUrl":"https://i.ebayimg.com/00/s/NjAwWDgwMA==/z/5ZUAAOSwa~BYPpRW/$_8.JPG","largeAlias":"large","largeSizeId":75,"largeUrl":"https://i.ebayimg.com/00/s/NjAwWDgwMA==/z/5ZUAAOSwa~BYPpRW/$_75.JPG","logoSizeId":26,"logoUrl":"https://i.ebayimg.com/00/s/NjAwWDgwMA==/z/5ZUAAOSwa~BYPpRW/$_26.JPG","mediumAlias":"medium","mediumSizeId":35,"mediumUrl":"https://i.ebayimg.com/00/s/NjAwWDgwMA==/z/5ZUAAOSwa~BYPpRW/$_35.JPG","normalAlias":"normal","teaserAlias":"teaser","teaserSizeId":74,"teaserUrl":"https://i.ebayimg.com/00/s/NjAwWDgwMA==/z/5ZUAAOSwa~BYPpRW/$_74.JPG","thumbnailAlias":"thumbnail","thumbnailSizeId":14,"thumbnailUrl":"https://i.ebayimg.com/00/s/NjAwWDgwMA==/z/5ZUAAOSwa~BYPpRW/$_14.JPG","status":"OK"}                
            } else {
                if (!Strings.checkNullBlankString(image)) {
                    nvps.add(new BasicNameValuePair("images", image));
                }
            }
        }
        return nvps;
    }

    private List<NameValuePair> fixAdvert(List<NameValuePair> nvps) {
        String description = getKey(nvps, "description");
        if (description.contains("THIS ADVERT IS MANAGED")) {
            description = description.substring(0, description.indexOf("THIS ADVERT IS MANAGED"));
        }
        while (description.endsWith("\n")) {
            description = description.substring(0, description.length() - 1);
        }

        description = description.replace("<br>", "\n").replace("<br/>", "\n");
        description = normaliseDescription(description);

        // description = description + "\n\n" + "THIS ADVERT IS MANAGED BY " + BASE_INTFORCE_URL;
        nvps = setKeyValues(nvps, "description", description);

        return nvps;
    }

    public String getPost(boolean login, List<NameValuePair> nvps) throws Exception {
        if (!login) {
            nvps = fixAdvert(nvps);
        }
        while (true) {
            HttpPost post = null;
            try {
                post = new HttpPost(new URI(login ? loginUrlPost : add));
                //post.addHeader("Cookie", Helper.join(Helper.cookieStr, "\n"));
                HttpEntity entity = new UrlEncodedFormEntity(nvps);
                String s1 = new String(utils.Helper.loadStream(entity.getContent()));
                post.setEntity(entity);
                String out = sendPost(post);
                return out;
            } catch (GumtreeOtherException ex) {
                throw ex;
            } catch (Exception ex) {
                text(ex.getMessage() + ". " + post.toString() + ". getPost(). Retrying...");
            } finally {
                post.releaseConnection();
            }
        }
    }

    private String sendPost(HttpPost post) throws Exception {
        const_RedirectURL = null;
        if (itemBidder.config != null) {
            post.setConfig(itemBidder.config);
        }
        Exception e = new Exception("Unknown exception");

        HttpResponse response = executeHttpResponse(post);

        lastResponseCode = response.getStatusLine().getStatusCode();

        InputStream is = response.getEntity().getContent();
        String ret = new String(utils.Helper.loadStream(is));
        response.getEntity().consumeContent();
        if (lastResponseCode != HttpStatus.SC_OK && lastResponseCode != HttpStatus.SC_MOVED_TEMPORARILY) {
            if (lastResponseCode != HttpStatus.SC_FORBIDDEN) {
                e = new Exception(response.getStatusLine().getReasonPhrase());
            } else {
                e = new ForbiddenException();
            }
        }

        if (lastResponseCode == HttpStatus.SC_MOVED_TEMPORARILY) {
            const_RedirectURL = response.getFirstHeader("Location").getValue();
            //getPageByURL(const_RedirectURL.startsWith("/") ? baseURL + const_RedirectURL : const_RedirectURL, itemBidder, null);
        }
        return ret;
    }

    public String getValue(String text, String ids) {
        for (String id : ids.split("\\|")) {
            String text1 = text.replace("\t", " ").replace("\r", " ").replace("\n", " ");
            id = " " + id.trim() + "=\"";
            if (text1.startsWith(id.trim())) {
                id = id.trim();
            }
            if (text1.contains(id)) {
                String s = text1.substring(text1.indexOf(id) + id.length());
                return s.substring(0, s.indexOf("\""));
            }
        }
        return "";
    }

    public boolean getIsItemsRecent(String page) throws Exception {
        for (String text : Helper.split(page, "<dd class=\"rs-ma-details-sort\">", "</dd>")) {
            text = Helper.getTagText(text);
            if (text.endsWith(" ago")) {
                String[] posted = Helper.replace(text, "minutes|minute|ago", "").trim().split(" ");
                return posted.length == 1 && Helper.getLong(posted[0]) < 10;
            } else {
                return isValidDateAndRecent(text);
            }
        }
        return true;
    }

    public boolean isValidDateAndRecent(String s) {
        try {
            return TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - new SimpleDateFormat("dd/mm/yyyy").parse(s).getTime()) < 10;
        } catch (Exception ex) {
            return false;
        }
    }

    public int getItemsOnPage(String page) throws Exception {

        for (String text : com.intforce.utils.Helper.split(page, "<li class=\"rs-ma-onpg\">", "</li>")) {
            String s = text.substring(text.indexOf(onpage) + onpage.length());
            s = s.substring(0, s.indexOf("<"));
            return com.intforce.utils.Helper.getMoney(s).intValue();
        }
        return 0;
    }

    public Long getItemsVisits(String page) throws Exception {
        for (String text : com.intforce.utils.Helper.split(page, "<span class=\"key\">Visits:</span>", "</span>")) {
            return com.intforce.utils.Helper.getLong(text.substring(text.lastIndexOf(">") + 1));
        }
        throw new Exception("Item visits caanot be found");
    }

    public boolean isExclusionCategory(String page) throws Exception {
        for (String exclusionCategory : exclusionCategories.split("\\|")) {
            if (page.contains(exclusionCategoriesTeplate.replace("<category>", exclusionCategory))) {
                return true;
            }
        }

        return false;
    }

    public String getItemsTitles(String page) throws Exception {

        for (String text : com.intforce.utils.Helper.split(page, "<a class=\"rs-ad-title", "</a>")) {
            text = text.substring(text.indexOf(">") + 1);
            return HTMLDecoder.decode(text.substring(0, text.length()));
        }
        throw new Exception("Item title caanot be found");
    }

    public List<String> getAllItems(String page) throws Exception {

        List<String> list = new ArrayList<String>();
        page = new HtmlParser(page).getTopLevelNamedTagTextByClass("ul", "search-result-set");
        String text = "";

        if (page.contains(const_MyAdClass)) {
            do {
                page = page.substring(page.indexOf("<li"));
                text = new HtmlParser(page).getTopLevelNamedTagTextByClass("li", const_MyAdClass);
                page = page.substring(text.length());

                String id = text.substring(text.indexOf(href) + href.length());
                id = id.substring(0, id.indexOf("\""));
                if (id.contains("=")) {
                    id = id.split("=")[1];
                    if (id.equalsIgnoreCase("1132877573")) {
                        id = id;
                    }
                    String item = const_Item
                            .replace("<id>", String.valueOf(id))
                            .replace("<user>", username)
                            .replace("<title>", getItemsTitles(text))
                            .replace("<page>", String.valueOf(getItemsOnPage(text)))
                            .replace("<visits>", String.valueOf(getItemsVisits(text)))
                            .replace("<active>", "true");
                    item = item.substring(0, item.indexOf("<") - 1);
                    list.add(item);
                }

            } while (page.contains(const_MyAdClass));
        }
        return list;
    }

    public int getMaxPage() {
        int i = maxPage;
        try {
            String value = utils.Helper.stringToMap(sendGetStatus(((JsonObject) jsonObject).getString("name"))).get("mp");
            if (value == null) {
                sendSetStatus(((JsonObject) jsonObject).getString("name"), "mp=" + i);
                text("Setting max page");
            } else {
                i = Integer.valueOf(value);
            }
        } catch (Exception ex) {
            text("Max page has not been set up");
        }
        return i;
    }

    public List<String[]> getItemsToRenew(String page) throws Exception {
        List<String[]> list = new ArrayList<String[]>();
        page = new HtmlParser(page).getTopLevelNamedTagTextByClass("ul", "search-result-set");
        String text = "";

        if (page.contains(const_MyAdClass)) {
            do {
                page = page.substring(page.indexOf("<li"));
                text = new HtmlParser(page).getTopLevelNamedTagTextByClass("li", const_MyAdClass);
                page = page.substring(text.length());

                String id = text.substring(text.indexOf(href) + href.length());
                id = id.substring(0, id.indexOf("\""));
                if (id.contains("=")) {
                    id = id.split("=")[1];

                    if (repostAll || (id != null && getItemsOnPage(text) > getMaxPage() && !getIsItemsRecent(text))) {
                        String title = getItemsTitles(text);
                        if (repostAll || !itemListContains(list, id)) {
                            list.add(new String[]{id, title, (isExclusionCategory(text) ? "true" : "false")});
                        }
                    }
                }
            } while (page.contains(const_MyAdClass));
        }
        return list;
    }

    private boolean itemListContains(List<String[]> list, String id) {
        for (String[] item : list) {
            if (item[0].equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAdvertExists(String url, ItemBidder itemBidde, String title) {
        while (true) {
            HttpGet get = null;

            try {
                get = new HttpGet(new URI(url));
                //get.addHeader("Cookie", Helper.join(Helper.cookieStr, "\n"));
                HttpResponse resp = executeHttpResponse(get);

                if (resp.getStatusLine().getStatusCode() == 500) {
                    return false;
                } else {
                    List<String> list = getAllItems();
                    for (String item : list) {
                        if (item.split("\n")[2].equals(title)) {
                            return true;
                        }
                    }
                    return false;
                }
            } catch (GumtreeOtherException ex) {
                return false;
            } catch (Exception ex) {
                text(ex.getMessage() + ". isAdvertExists(). Retrying...");
            } finally {
                get.releaseConnection();
            }
        }

    }

    public String getPageByURL(String url, ItemBidder itemBidder, String[] headers) {
        while (true) {
            HttpGet get = null;
            try {
                //get.addHeader("Cookie", Helper.join(Helper.cookieStr, "\n"));
                int i = 0;
                HttpResponse resp = null;
                while (true) {
                    get = new HttpGet(new URI(url));
                    if (headers != null) {
                        for (String header : headers) {
                            get.addHeader(header.split("=")[0], header.split("=")[1]);
                        }
                    }

                    resp = executeHttpResponse(get);

                    if (resp.getStatusLine().getStatusCode() == 500) {
                        text("Error loading: " + url);
                        i++;
                        if (i < 3) {

                            text("Retrying loading...");
                            get.releaseConnection();
                            Thread.sleep(3000);
                        } else {
                            url = delete.split("\\?")[0] + "?" + url.split("\\?")[1];
                            new HttpGet(new URI(url));
                            text("Deleted: " + url);
                            return "__NOR__";
                        }
                    } else if (resp.getStatusLine().getStatusCode() == 301 || resp.getStatusLine().getStatusCode() == 302) {
                        return getPageByURL(baseURL + resp.getFirstHeader("Location").getValue(), itemBidder, headers);
                    } else {
                        break;
                    }
                }
                String out = new String(utils.Helper.loadStream(utils.Helper.setCookies(resp, itemBidder).getEntity().getContent()));
                resp.getEntity().consumeContent();

                return out;
            } catch (Exception ex) {

            } finally {
                get.releaseConnection();
            }
        }
    }

    void sleep() {
        try {
            Thread.sleep(TIME_OUT);
        } catch (Exception ex) {
        }
    }

    public int getNumberOfPages(String url, ItemBidder itemBidder) throws Exception {

        String page = getPageByURL(url, itemBidder, null);
        String out = page;

        String s = null;
        if (page.contains(pageNumberSelected)) {
            try {
                List<String> list = Helper.split(Helper.split(page, "<div class=\"pagerlinks\">", "</div>").get(0), "<a", "</a");
                s = list.get(list.size() - 1);
                s = s.substring(s.indexOf(">") + 1);
                if (s.contains("<")) {
                    s = s.substring(0, s.indexOf("<"));
                }
                return Helper.getMoney(s).intValue();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        int i = 0;
        try {
            i = Helper.getMoney(new HtmlParser(page).getTopLevelNamedTagTextByClass("span", "ad-cnt")).intValue();
        } catch (Exception ex) {

        }
        return i >= 1 ? 1 : 0;
    }

    public boolean login(boolean bbUserInput) throws Exception {
        boolean ret = false;
        for (int i = 0; i < 3; i++) {
            //itemBidder.context = HttpClientContext.create();
            //itemBidder.context.setCookieStore(itemBidder.cookieStore);
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("targetUrl", ""));
            nvps.add(new BasicNameValuePair("likingAd", "false"));
            nvps.add(new BasicNameValuePair("loginMail", username));
            nvps.add(new BasicNameValuePair("password", password));
            nvps.add(new BasicNameValuePair("_rememberMe", "on"));
            nvps.add(new BasicNameValuePair("rememberMe", "true"));

            nvps = loadHiddenInput(loginUrlGet, nvps, bbUserInput);

            String out = getPost(true, nvps);
            ret = checkPost(true, out, nvps, add);
            if (ret) {
                break;
            }
            Thread.sleep(1000);
        }
        if (ret) {
            if (!new File(projectFolder + "cats" + "/" + getFileName("map")).exists()) {
                mapToFile(projectFolder + "cats", loadCategories(), "map");
            }
        } else {
            if (lastResponseCode == 403) {
                text("Unauthorised 403 exception on login. Will try again on next run");
                sendMessage(adminEmail, "Cannot loging as user: " + ((JsonObject) jsonObject).getString("name"));
            }
            if (((JsonObject) jsonObject).getBoolean("enabled")) {
                text("###Login error for " + username);
                sendMessage(((JsonObject) jsonObject).getString("name"), String.format("Hi,\nthe system detected that the login credentials provided are invalid:"
                        + "\nuser: %s\npassword: %s\nPlease check and correct them:"
                        + " - Navigate to https://gumtree-intforce.b9ad.pro-us-east-1.openshiftapps.com/\n"
                        + " - Provide correct username\n"
                        + " - Provide correct password\n"
                        + " - Select 1 free renewal option\n"
                        + " - Click Subscribe\n"
                        + "\n"
                        + "Please email us at " + adminEmail + " should you have any questions.\n"
                        + "Regards,\n"
                        + "VirtualColleague team.", username, password));
                JsonObject json = sendDisableUser(((JsonObject) jsonObject).getString("name"));
                text("User delete status: " + json.toString());
                if (json.getString("status").equalsIgnoreCase("OK")) {
                    sendMessage(((JsonObject) jsonObject).getString("name"), "Hi,\nthe system detected that the login credential provided is invalid.\n"
                            + "You should have recieved another notification from us on how to fix this problem.\n"
                            + "In the meantime, the sytem has disabled your incorrect login credentials\n"
                            + "\n"
                            + "Please email us at info@virtualcolleague.com.au should you have any questions.\n"
                            + "Regards,\n"
                            + "VirtualColleague team.");
                }
            }
        }

        return ret;

    }

    public URI getURI(String url, String type) throws URISyntaxException {
        url = url.replace("<token>", Utils.getToken(type));
        return new URI(url);
    }

    private JsonObject parseJson(String str) throws Exception {
        JsonObject obj = null;
        try {
            if (!Strings.checkNullBlankString(str)) {
                obj = Json.createReader(new StringReader(str)).readObject();
            }
        } catch (Exception ex) {
            text("Exception parsing Json: " + str);
            throw ex;
        }
        return obj;
    }

    private HttpResponse executeHttpResponse(HttpRequestBase req) throws ConnectException, NoHttpResponseException, HttpHostConnectException, ConnectionPoolTimeoutException, GumtreeOtherException, SocketException, SSLHandshakeException {
        try {
            return itemBidder.httpclient.execute(req, itemBidder.context);
        } catch (HttpHostConnectException ex) {
            throw ex;
        } catch (ConnectionPoolTimeoutException ex) {
            throw ex;
        } catch (NoHttpResponseException ex) {
            throw ex;
        } catch (ConnectException ex) {
            throw ex;
        } catch (SSLHandshakeException ex) {
            throw ex;
        } catch (SocketException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new GumtreeOtherException(ex);
        }
    }

    private JsonObject parseJson(HttpRequestBase get) throws Exception {
        while (true) {
            try {
                InputStream is = executeHttpResponse(get).getEntity().getContent();

                String response = new String(utils.Helper.loadStream(is));
                is.close();

                JsonObject json = parseJson(response);
                return json;
            } catch (GumtreeOtherException ex) {
                throw ex;
            } catch (Exception ex) {
                text(ex.getMessage() + ". " + get.toString() + ". parseJson(). Retrying...");
                Thread.sleep(2000);
            } finally {
                get.releaseConnection();
            }
        }
    }

    public String getCategory(String keyword) throws Exception {
        Map<String, String> map = utils.Helper.loadMapFromString(utils.Helper.loadFromFile(projectFolder + "cats/" + getFileName("map")));
        String cat = null;
        int j = 0;
        for (String key : map.keySet()) {
            int i = 0;
            for (String s : keyword.split("\\W+")) {
                if (map.get(key).toLowerCase().contains(s.toLowerCase())) {
                    i++;
                }
            }
            if (i > j) {
                cat = key + ";" + map.get(key);
                j = i;
            }
        }
        if (cat == null) {
            throw new Exception(String.format("Cannot determine category for: %s", keyword));
        }
        return cat;
    }

    public JsonObject nvpsToJsonItem(List<NameValuePair> nvps) throws Exception {
        return Json.createObjectBuilder().add("itemid", "0")
                .add("userid", ((JsonObject) jsonObject).getString("name"))
                .add("title", getKey(nvps, "title"))
                .add("page", "0")
                .add("visits", "0")
                .add("minvisits", getKey(nvps, "minvisits").equals("") ? "50" : getKey(nvps, "minvisits"))
                .add("hourstomarket", getKey(nvps, "hourstomarket").equals("") ? "24" : getKey(nvps, "hourstomarket"))
                .add("managed", getKey(nvps, "managed").equals("") ? "true" : getKey(nvps, "managed"))
                .add("active", getKey(nvps, "active").equals("") ? "true" : getKey(nvps, "active")).build();
    }

    public JsonObject sendItem(String user, JsonObject json) throws Exception {
        return sendItem(user, const_Item.replace("<id>", json.get("itemid").toString())
                .replace("<user>", ((JsonObject) jsonObject).getString("email"))
                .replace("<title>", json.get("title").toString())
                .replace("<page>", json.get("page").toString())
                .replace("<visits>", json.get("visits").toString())
                .replace("<minvisits>", json.get("minvisits").toString())
                .replace("<hourstomarket>", json.get("hourstomarket").toString())
                .replace("<managed>", json.get("managed").toString())
                .replace("<active>", json.get("active").toString()));
    }

    public JsonObject sendDeleteItem(String id) throws Exception {
        HttpGet get = new HttpGet(getURI(itemIdURL.replace("<item>", encrypt(id)), "deleteitem"));
        return parseJson(get);
    }

    public JsonObject sendItem(String user, String item) throws Exception {
        HttpGet get = new HttpGet(getURI(itemURL.replace("<user>", user).replace("<item>", encrypt(item)), "item"));
        return parseJson(get);
        //JsonObject = new JsonParserImpl(itemBidder.httpclient.execute(get, itemBidder.context).getEntity().getContent()).
    }

    public JsonObject sendRenewed(String name, String title) throws Exception {
        String item = const_Item.replace("<id>", "0")
                .replace("<user>", username)
                .replace("<title>", title)
                .replace("<page>", "0")
                .replace("<visits>", "0")
                .replace("<active>", "true");
        item = item.substring(0, item.indexOf("<") - 1);
        HttpGet get = new HttpGet(getURI(itemURL.replace("<user>", name).replace("<item>", encrypt(item)), "renewed"));

        return parseJson(get);
        //JsonObject = new JsonParserImpl(itemBidder.httpclient.execute(get, itemBidder.context).getEntity().getContent()).
    }

    public String sendSettimestamp(String user) throws Exception {
        while (true) {
            HttpGet get = null;
            try {
                get = new HttpGet(getURI(userURL.replace("<user>", user), "settimestamp"));
                InputStream is = executeHttpResponse(get).getEntity().getContent();

                String ret = new String(utils.Helper.loadStream(is));
                return ret;
            } catch (GumtreeOtherException ex) {
                throw ex;
            } catch (Exception ex) {
                text(ex.getMessage() + ". Retrying...");
            } finally {
                get.releaseConnection();
            }
        }
        //JsonObject = new JsonParserImpl(itemBidder.httpclient.execute(get, itemBidder.context).getEntity().getContent()).
    }

    public String sendGetStatus(String user) throws Exception {
        String key = user + "_getstatus";
        if (userMap.get(key) == null) {
            HttpGet get = new HttpGet(getURI(userURL.replace("<user>", user), "getstatus"));
            userMap.put(key, Utils.decrypt1(parseJson(get).getString("value")));
        }
        return userMap.get(key);
        //JsonObject = new JsonParserImpl(itemBidder.httpclient.execute(get, itemBidder.context).getEntity().getContent()).
    }

    public JsonObject sendSetStatus(String user, String status) throws Exception {
        HttpGet get = new HttpGet(getURI(statusURL.replace("<user>", user).replace("<status>", encrypt(status)), "setstatus"));
        return parseJson(get);
        //JsonObject = new JsonParserImpl(itemBidder.httpclient.execute(get, itemBidder.context).getEntity().getContent()).
    }

    public JsonObject sendMessage(String user, String message) throws Exception {
        HttpGet get = new HttpGet(getURI(messageURL.replace("<user>", user).replace("<message>", encrypt(message)), "sendMessage"));
        return parseJson(get);
        //JsonObject = new JsonParserImpl(itemBidder.httpclient.execute(get, itemBidder.context).getEntity().getContent()).
    }

    public JsonObject sendDisableUser(String user) throws Exception {
        HttpGet get = new HttpGet(getURI(userURL.replace("<user>", user), "disable"));
        return parseJson(get);
        //JsonObject = new JsonParserImpl(itemBidder.httpclient.execute(get, itemBidder.context).getEntity().getContent()).
    }

    public JsonObject sendDeleteUser(String user) throws Exception {
        HttpGet get = new HttpGet(getURI(userURL.replace("<user>", user), "delete"));
        return parseJson(get);
        //JsonObject = new JsonParserImpl(itemBidder.httpclient.execute(get, itemBidder.context).getEntity().getContent()).
    }

    public String sendPaid(String user) throws Exception {

        while (true) {
            HttpGet get = null;
            try {
                get = new HttpGet(getURI(userURL.replace("<user>", user), "paid"));
                InputStream is = executeHttpResponse(get).getEntity().getContent();

                String ret = new String(utils.Helper.loadStream(is));
                return ret;
            } catch (GumtreeOtherException ex) {
                throw ex;
            } catch (Exception ex) {
                text(ex.getMessage() + ". Retrying...");
            } finally {
                get.releaseConnection();
            }
        }

        //JsonObject = new JsonParserImpl(itemBidder.httpclient.execute(get, itemBidder.context).getEntity().getContent()).
    }

    public JsonObject sendGetSchedule() {
        while (true) {
            try {
                HttpGet get = new HttpGet(getURI(process, "schedule"));
                return parseJson(get);
            } catch (Exception ex) {
                text(ex.getMessage());
            }
        }
    }

    public JsonObject sendPutAddToPost(String user, String text) throws Exception {
        HttpGet get = new HttpGet(getURI(addtopostURL.replace("<user>", user).replace("<addtopost>", Utils.encrypt(text)), "putaddtopost"));
        return parseJson(get);
    }

    public JsonObject sendDeleteAddToPost(String user, long id) throws Exception {
        HttpGet get = new HttpGet(getURI(addtopostURL.replace("<user>", user).replace("<addtopost>", String.valueOf(id)), "deleteaddtopost"));
        return parseJson(get);
    }

    public JsonObject sendGetAddsToPost(String user) throws Exception {
        HttpGet get = new HttpGet(getURI(userURL.replace("<user>", user), "getaddstopost"));
        return parseJson(get);
    }

    public JsonObject sendGetItemsForRenewals(String user) throws Exception {
        HttpGet get = new HttpGet(getURI(userURL.replace("<user>", user), "getitemsforrenewals"));
        return parseJson(get);
    }

    public JsonObject sendGetUsers() {
        try {
            HttpGet get = new HttpGet(getURI(process, "allusers"));
            return parseJson(get);
        } catch (Exception ex) {
            return null;
        }
    }

    public JsonObject sendGetInactiveUsers() {
        try {
            HttpGet get = new HttpGet(getURI(process, "inactiveusers"));
            return parseJson(get);
        } catch (Exception ex) {
            return null;
        }
    }

    public JsonObject sendGetAllItems() throws Exception {
        HttpGet get = new HttpGet(getURI(userURL.replace("<user>", ((JsonObject) jsonObject).getString("name")), "getallitems"));
        return parseJson(get);
    }

    public JsonObject sendGetItemsToBeDeleted() throws Exception {
        HttpGet get = new HttpGet(getURI(userURL.replace("<user>", ((JsonObject) jsonObject).getString("name")), "getallitemstobedeleted"));
        return parseJson(get);
    }

    public String loadImage(String url, String user) {
        if (new File(url).exists()) {
            return url;
        }
        Image image = null;
        try {
            URL iurl = new URL(url);
            image = ImageIO.read(iurl);
            BufferedImage bi = (BufferedImage) image;

            File f = new File(tempFolder.replace("<username>", user));
            f.mkdirs();
            if (f.exists()) {
                f.delete();
            }
            ImageIO.write(bi, "bmp", f);
            return f.getAbsolutePath();
        } catch (Exception ex) {
            ex = ex;
        }
        return null;
    }

    public String TransformDate(String d) {
        String retDate = "";
        for (String s : d.split("-")) {
            retDate = s + (retDate.length() > 0 ? "/" : "") + retDate;
        }
        return retDate;
    }

    public String[] addListOfSpecialDatePeriodAttrs(String[] attrs, List<NameValuePair> nvps, String out) {
        if (mapListOfSpecialDatePeriodAttrs.containsKey(attrs[0])) {
            attrs[1] = out.replace("\"", "").substring(out.replace("\"", "").indexOf(attrs[0]) + attrs[0].length() + 1);
            attrs[1] = attrs[1].substring(0, attrs[1].lastIndexOf("Z") + 1);
            if (mapListOfSpecialDatePeriodAttrs.get(attrs[0]) != null) {
                String[] s = mapListOfSpecialDatePeriodAttrs.get(attrs[0]).split(";");
                nvps.add(new BasicNameValuePair(s[0], TransformDate(attrs[1].split(",")[0].split("T")[0])));
                nvps.add(new BasicNameValuePair(s[1], TransformDate(attrs[1].split(",")[1].split("T")[0])));
            }
        }
        return attrs;
    }

    public boolean containsAny(String s1, String s2) {
        for (String s : s2.split(",")) {
            if (s1.contains(s)) {
                return true;
            }
        }
        return false;
    }

    private List<String[]> filterExclusions(List<String[]> list) {
        List<String[]> excl = new ArrayList<String[]>();
        List<String[]> nonexcl = new ArrayList<String[]>();
        for (String[] arr : list) {
            if (arr[2].equals("true")) {
                excl.add(arr);
            } else {
                nonexcl.add(arr);
            }
        }
        if (excl.size() < 2) {
            nonexcl.addAll(excl);
        } else {
            String txt = "Found some adverts with exclusion category. These will be skipped and not renewed\n";
            for (String[] ex : excl) {
                txt += ex[0] + "\t" + ex[1] + "\n";
            }
            text(txt);
            writeToFile(messagesFolder.replace("<username>", username) + "\\exclusionCat.txt", txt);

        }
        return nonexcl;
    }

    private synchronized void writeToFile(String file, String text) {
        try {
            new FileWriter(new File(file)).write(text);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
