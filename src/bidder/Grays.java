/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bidder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

/**
 *
 * @author dmitry
 */
public class Grays {

    private static ItemBidder itemBidder = new ItemBidder();
    private static String url = "https://www.graysonline.com/login.aspx?el=1&ReturnUrl=%2fpromotions%2fmartelco";
    private static String statusurl = "http://www.graysonline.com/api/LoginStatus/GetLoginStatus";
    private static String ukey = "ctl00$content$loginControl$login$UserName";
    private static String pkey = "ctl00$content$loginControl$login$Password";
    private static String user = "kryuchkov@hotmail.com";
    private static String password = "N0t4any1";
    private static final Gumtree gumtree = new Gumtree();
    
    static String _login
            = "ReqCrossSiteImage=true; mbox=check#true#1460167572|session#1460167511269-197201#1460169372"
            + "; deviceScreenSize=xl; deviceSmallScreenSizeSet=0; AMCV_grays%40AdobeOrg=136688995"
            + "%7CMCMID%7C12129662747133892615980833627041990645%7CMCAID%7CNONE; s_vnum=1491689035216%26vn%3D2; s_nr"
            + "=1460163307661-Repeat; ItemsPerPage=40; AMCV_84192D4653A2DDF90A490D4E%40AdobeOrg=T; _caid=73c0b143-921e-4f77-9cde-6f434c6abd8e"
            + "; _ga=GA1.3.182228620.1460153068; __utma=217762704.182228620.1460153068.1460153069.1460157142.2; __utmz"
            + "=217762704.1460153069.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); scsuid=FerjNFxyUBDd6PHkwYMcHAhEssHoBPAe"
            + "; s_invisit=true; s_ppn=https%3A%2F%2Fwww.graysonline.com%2Flogin.aspx%3Fel%3D1; __utmb=217762704.16"
            + ".10.1460157142; _cavisit=153f861b133|; GlobalGraysTrackingData=%7B%22RecentlyVisitedPage%22%3A%7B%22EntityId"
            + "%22%3A%22%22%2C%22EntityCategoryId%22%3A%22no-category%22%7D%7D; _gat_UA-19767456-2=1; __utmt_UA-19767456-1"
            + "=1";

    public static void main(String[] args) throws Exception {
        System.out.println(login());
        System.out.println(gumtree.getPageByURL(url, itemBidder, null));
    }

    public static String login() throws Exception {

        List<NameValuePair> nvps = getPage(url);
        nvps.remove(ukey);
        nvps.remove(pkey);
        nvps.add(new BasicNameValuePair(ukey, user));
        nvps.add(new BasicNameValuePair(pkey, password));

        HttpPost post = new HttpPost(new URI(url));
        post.addHeader("Cookie", _login + "; " + utils.Helper.join(utils.Helper.cookieStr, "; "));
        post.addHeader(new BasicHeader("Accept-Encoding", "gzip, deflate, br"));

        //post.addHeader("User-Agent", "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.7) Gecko/2009030422 Ubuntu/8.10 (intrepid) Firefox/3.0.7");
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nvps);
        post.setEntity(entity);

        return new String(utils.Helper.loadStream(utils.Helper.setCookies(itemBidder.httpclient.execute(post, itemBidder.context), itemBidder).getEntity().getContent()));

    }

    static List<NameValuePair> getPage(String url) throws Exception {
        String out = gumtree.getPageByURL(url, itemBidder, null);
        out = com.intforce.utils.Helper.split(out, "<form ", "/form>").get(1);

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        for (String text : com.intforce.utils.Helper.split(out, "<input ", ">")) {
            String vid = gumtree.getValue(text, "id");
            String value = gumtree.getValue(text, " value");
            String type = gumtree.getValue(text, "type");
            String checked = gumtree.getValue(text, "checked");
            nvps.add(new BasicNameValuePair(vid, value));
        }
        return nvps;
    }

}
