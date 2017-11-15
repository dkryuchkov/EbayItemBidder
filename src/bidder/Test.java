package bidder;

import com.sun.media.sound.SoftReceiver;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import net.haxx.curl.*;
/*
 * Test class to login to eBay
 * change the username and password to yours.
 */

public class Test implements CurlWrite {

    public final static String username = "kryuchkov@hotmail.com";
    public final static String password = "N0t4any1";

    public int handleString(byte s[]) {
        /* output everything */
        try {
            System.out.write(s);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        ReferenceQueue q = new ReferenceQueue();
        Map m = new HashMap();
        String s = new String("test");
        m.put(new PhantomReference(s,q).get(), new SoftReference(args,q));
        s="test1";
         m.put(new PhantomReference(s,q).get(), new SoftReference(new String("123"),q));
         m.put(new PhantomReference(s,q).get(), new SoftReference(new String("123"),q));
        s = null;
        System.gc();
        int i =1;
        while(i++<100)
        for(Object o : m.keySet())
        {
            System.gc();
            System.out.println(o);
            Thread.sleep(i*1000);
        }
        new Test().flatten(new File("C:\\Temp\\curl-7.42.1\\lib\\openssl"), new File("C:\\Temp\\New folder (4)"));
        CurlGlue cg;

        try {
            //C:\\Temp\\openssl-1.0.2a/out;C:\\Temp\\curl-7.42.1/lib;C:\\Temp\\liboi-52e8c55" +
//"b2004\\win32\\depends\\zlib;C:\\Temp\\curl-7.42.1/include;C:\\Temp\\curl-7.42.1\\bui" +
//"lds\\libcurl-vc-x86-release-dll-ipv6-sspi-winssl\\bin;
           //System.setProperty("java.library.path", "C:\\Temp\\curljava\\lib");
            System.loadLibrary("javacurl");
            Test cw = new Test();

// Registe//r callback write function
            cg = new CurlGlue();
            cg.setopt(CurlGlue.CURLOPT_WRITEFUNCTION, cw);

// first, go to the login page to get the cookies.
            cg.setopt(CurlGlue.CURLOPT_URL, "https://signin.ebay.com/aw-cgi/eBayISAPI.dll?SignIn");
            cg.setopt(CurlGlue.CURLOPT_USERAGENT, "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.7) Gecko/2009030422 Ubuntu/8.10 (intrepid) Firefox/3.0.7");
            cg.setopt(CurlGlue.CURLOPT_FOLLOWLOCATION, 1);
            cg.setopt(CurlGlue.CURLOPT_COOKIEJAR, "cookie.txt");
            cg.setopt(CurlGlue.CURLOPT_COOKIEFILE, "cookie.txt");
            cg.perform();
            cg.close();

// login using the username, password and the cookies we got from the login page.
            cg.setopt(CurlGlue.CURLOPT_WRITEFUNCTION, cw);
            cg.setopt(CurlGlue.CURLOPT_URL, "https://signin.ebay.com/aw-cgi/eBayISAPI.dll");
            cg.setopt(CurlGlue.CURLOPT_USERAGENT, "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.7) Gecko/2009030422 Ubuntu/8.10 (intrepid) Firefox/3.0.7");
            cg.setopt(CurlGlue.CURLOPT_POST, 1);
            cg.setopt(CurlGlue.CURLOPT_POSTFIELDS, "MfcISAPICommand=SignInWelcome&siteid=0&co_partnerId=2&UsingSSL=0&ru=&pp=&pa1=&pa2=&pa3=&i1=-1&pageType=-1&userid=" + username + "&pass=" + password);
            cg.setopt(CurlGlue.CURLOPT_FOLLOWLOCATION, 1);
            cg.setopt(CurlGlue.CURLOPT_COOKIEJAR, "cookie.txt");
            cg.setopt(CurlGlue.CURLOPT_COOKIEFILE, "cookie.txt");
            cg.perform();

            cg.close();

// now, if you like you can fetch the page: http://my.ebay.com/ws/eBayISAPI.dll?MyeBay and see that you're logged in
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void flatten(File directory, File destination) throws IOException {
        if (directory == null || !directory.exists()) {
            throw new IOException("The source directory is null or does not exist.");
        }
        if (destination == null) {
            destination = directory;
        }

        for (File aFile : directory.listFiles()) {
            if (aFile.isDirectory()) {
                this.flatten(aFile, destination);
                aFile.delete();
            } else {
                // is the file already at the top level of the hierarchy?
                if (!aFile.getParentFile().getAbsolutePath().equals(destination.getAbsolutePath())) {
                    // is a file by the same name already there?
                    File movedFile = new File(destination, aFile.getName());
                    if (movedFile.exists()) {
                        if (aFile.getName().equals("ssl.h")) {
                            aFile.getName();
                        }
                        if (aFile.length() > movedFile.length()) {
                            movedFile.delete();
                            aFile.renameTo(movedFile);
                        }
                    } else if (!aFile.renameTo(movedFile)) {
                        //throw new IOException("Could not move file from: " + aFile.getAbsolutePath() + " to: " + destination.getAbsolutePath());
                    }
                }
            }
        }
    }

    public long getChecksumValue(File aFile) {
        Checksum checksum = new CRC32();
        try {
            BufferedInputStream is = new BufferedInputStream(
                    new FileInputStream(aFile));
            byte[] bytes = new byte[1024];
            int len = 0;
            while ((len = is.read(bytes)) >= 0) {
                checksum.update(bytes, 0, len);
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return checksum.getValue();
    }
}
