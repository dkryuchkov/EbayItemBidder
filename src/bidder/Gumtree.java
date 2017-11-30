/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bidder;

import com.intforce.gumtree.Renewer;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.json.JsonObject;
import javax.json.JsonValue;

/**
 *
 * @author dmitry
 */
public class Gumtree {

    Renewer renewer = new Renewer(null);
    final static Gumtree gumtree = new Gumtree();
    final static Gumtree gumtree1 = new Gumtree();
    public static PrintWriter out = null;

    static final int MAX_THREADS = 5;
    static ExecutorService executor = null;
    static Collection<Future> futures = null;

    static {
        try {
            //System.setProperty("org.apache.http.client.protocol.ResponseProcessCookies", "fatal");
            out = new PrintWriter(new BufferedWriter(new FileWriter("log.txt", false)));
        } catch (IOException e) {
            out = null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize(); //To change body of generated methods, choose Tools | Templates.
        if (out != null) {
            out.close();
        }
    }

    public static void main(String[] args) throws Exception {
        Renewer.cl = args;
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

    public void run() {
        //while (true) {
        try {
            int i = 0;
            JsonObject json = renewer.sendGetUsers();
            Renewer.jsonSchedule = renewer.sendGetSchedule();

            for (JsonValue o : json.getJsonArray("users")) {

                if (i++ == 0) {
                    futures = new HashSet<Future>();
                    executor = Executors.newFixedThreadPool(MAX_THREADS);
                }
                futures.add(executor.submit(new Renewer(o)));

                if (i == MAX_THREADS) {
                    i = 0;
                    waitThreads();
                }
            }

            waitThreads();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void waitThreads() {
        for (Future future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                throw new IllegalStateException("task interrupted", e);
            }
        }
        executor.shutdown();
    }

    public void manageReports() {
        try {
            renewer.sendReport(renewer.sendGetInactiveUsers(), Renewer.templateInactiveUserReport);
            renewer.sendReport(renewer.sendGetUsers(), Renewer.templateActiveUserReport);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
