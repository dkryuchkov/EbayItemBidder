/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bidder;

import static utils.Helper.cookieStore;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpHost;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

/**
 *
 * @author dmitry
 */
public class ItemBidder {

    /**
     * @param args the command line arguments
     */
    final String getOfferUrl = utils.Helper.baseURL + "/confirmbid.aspx?lot-id=12433878&quantity=1&price=159.0000&bid-type=Auto";
    final String offerUrl = utils.Helper.baseURL + "/submitbid.aspx?lot-id=<itemid>&quantity=1&price=<price>.0000&bid-type=Auto"
            + "&frchar=0.0000&bpre=27.83";
    // final String post = "https://signin.ebay.com.au/ws/eBayISAPI.dll?co_partnerId=2&amp;siteid=15&amp;UsingSSL=1";
    public HttpClient httpclient = null;
    public final CookieStore cookieStore = new BasicCookieStore();
    public RequestConfig config = null;

    PoolingHttpClientConnectionManager connManager = null;
    public HttpClientContext context = null;
    public HttpHost proxy = null;
    final static String tracemyip = "http://www.tracemyip.org";

    public ItemBidder() {
        connManager = new PoolingHttpClientConnectionManager();
        context = HttpClientContext.create();
        context.setCookieStore(cookieStore);
        proxy = null;

        proxy = isInAU() ? null : null; //new HttpHost("139.59.244.147", 8080);

        Builder builder = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD)
                .setConnectionRequestTimeout(120000)
                // Waiting for connection to establish
                .setConnectTimeout(120000)
                // Waiting for data
                .setSocketTimeout(0).setStaleConnectionCheckEnabled(true);

        if (proxy != null) {
            builder.setProxy(proxy).setRedirectsEnabled(true).build();
        }

        config = builder.build();

        httpclient = HttpClients.custom().setConnectionManager(connManager).setDefaultRequestConfig(config)
                .setDefaultCookieStore(cookieStore)
                .setMaxConnTotal(2000).setMaxConnPerRoute(1000)
                .build();

        // httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
        //         CookiePolicy.BROWSER_COMPATIBILITY);
    }

    public Cookie AuthCookie = null;

    {
        //Scheme scheme = new Scheme("https", mac.http.conn.ssl.SSLSocketFactory.getSocketFactory(), 443);
        //httpclient.getConnectionManager().getSchemeRegistry().register(scheme);
    }

    public static void main(String[] args) throws Exception {
        args = new String[]{"12433878", "159"};

    }

    public static boolean isInAU() {
        ByteArrayOutputStream output = null;
        try {
            output = new ByteArrayOutputStream();
            new DefaultHttpClient().execute(new HttpGet(tracemyip)).getEntity().writeTo(output);
            return output.toString().toLowerCase().contains("australia");
        } catch (IOException ex) {

        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException ex) {

                }
            }
        }
        return false;
    }

}
