package bidder;

import com.intforce.gumtree.Renewer;
import ij.ImagePlus;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.yccheok.numrecognition.FeatureParameter;
import org.yccheok.numrecognition.NumberImageFeatureFactory;
import org.yccheok.numrecognition.NumberImageLRTBHVFeatureFactory;
import org.yccheok.numrecognition.NumberImageProcessor;
import org.yccheok.numrecognition.NumberNeuralNetworkRecognizer;

public class ImageRecog {

    static final String fpath = "C:\\git\\EbayItemBidder\\lib\\LRTBHV-training-data.txt-I=96-H=200-LR=0.9-M=0.1-C=2000-.snet";
    static final String ufpath = "C:\\git\\EbayItemBidder\\temp\\<user>\\LRTBHV-training-data.txt-I=96-H=200-LR=0.9-M=0.1-C=2000-.snet";
    static final NumberImageFeatureFactory imageFeature = new NumberImageLRTBHVFeatureFactory();
    static final NumberNeuralNetworkRecognizer neuralNetwork = new NumberNeuralNetworkRecognizer(fpath, imageFeature, FeatureParameter.DEFAULT_FEATURE_PARAMETER);

    static int length = 4;
    static int count = 13;
    private static int maxIterations = 30;

    public static void main(String[] args) {
        length = 6;
        String url = "http://cgi4.ebay.com.au/ws/eBayISAPI.dll?LoadBotImage&siteid=15&co_brandId=2&tokenString=7qltyA4AAAA%3D&t=1479429741220&hc=1&hm=eog4d71f%2Bea76";
        while (true) {
            //  System.out.println(getNumber(url, "", 30));
        }
    }

    public ImageRecog(String user) {
        if (!new File(ufpath.replace("<user>", user)).exists()) {
            try {
                FileUtils.copyFile(new File(fpath), new File(ufpath.replace("<user>", user)));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        //neuralNetwork = new NumberNeuralNetworkRecognizer(ufpath.replace("<user>", user), imageFeature, FeatureParameter.DEFAULT_FEATURE_PARAMETER);

    }

    public synchronized String loadImage(String url, String user) {
        if (new File(url).exists()) {
            return url;
        }
        Image image = null;
        try {
            URL iurl = new URL(url);
            image = ImageIO.read(iurl);
            BufferedImage bi = (BufferedImage) image;

            File f = new File(Renewer.tempFolder.replace("<username>", user));
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

    public String getNumberURL(String url, String user) {
        int[] s1 = new int[count];
        synchronized (neuralNetwork) {
            for (int j = 0; j < count; j++) {
                String file = loadImage(url, user);
                NumberImageLRTBHVFeatureFactory imageFeature = new NumberImageLRTBHVFeatureFactory();

                NumberNeuralNetworkRecognizer neuralNetwork = new NumberNeuralNetworkRecognizer("C:\\git\\EbayItemBidder\\lib\\LRTBHV-training-data.txt-I=96-H=200-LR=0.9-M=0.1-C=2000-.snet", imageFeature, FeatureParameter.DEFAULT_FEATURE_PARAMETER);

                s1[j] = neuralNetwork.recognize(new NumberImageProcessor(new ImagePlus(url)));
            }
        }
        return String.valueOf(findPopular(s1));
    }

    public synchronized String getNumber(String url, String user, int r) {
        return getNumber(url, length, user, r);
    }

    public String getNumber(String url, String user) {
        return getNumber(url, length, user, randInt(51, 200));
    }

    public String getNumber(String url, int length, String user, int r) {
        synchronized (neuralNetwork) {
            int i = 0;
            while (i++ < maxIterations) {
                String file = loadImage(url, user);
                if (file == null) {
                    continue;
                }

                String[] s2 = neuralNetwork.recognize(file, r);
                for (String s3 : s2) {
                    if (s3.length() == length && !s3.contains("11")) {
                        return s3;
                    }
                }

            }
        }
        return "1234";
    }

    /*
    public synchronized String getNumber1(String url, int length, String user, int r) {
        int[][] s1 = new int[length][count];
        String out = "";
        for (int j = 0; j < count; j++) {
            String file = loadImage(url, user);
            NumberImageFeatureFactory imageFeature = new NumberImageLRTBHVFeatureFactory();

            NumberNeuralNetworkRecognizer neuralNetwork = new NumberNeuralNetworkRecognizer("C:\\git\\EbayItemBidder\\lib\\LRTBHV-training-data.txt-I=96-H=200-LR=0.9-M=0.1-C=2000-.snet", imageFeature, FeatureParameter.DEFAULT_FEATURE_PARAMETER);

            String[] s = null;
            int k = 0;
            while (((s == null) || (s.length < length)) && (k++ < 20)) {
                String[] s2 = neuralNetwork.recognize(file, r);
                s = null;
                for (String s3 : s2) {
                    if (s != null) {
                        s = cleanInputs((String[]) ArrayUtils.addAll(s, s3.split("|")));
                    } else {
                        s = cleanInputs(s3.replaceAll("[^0-9.]", "").split("|"));
                    }
                    if (s.length > length - 1) {
                        break;
                    }
                }
            }
            for (int i = 0; (i < length) && (i < s.length); i++) {
                s1[i][j] = Integer.valueOf(s[i]).intValue();
            }
        }
        out = "";
        for (int i = 0; i < length; i++) {
            out = out + String.valueOf(findPopular(s1[i])).trim();
        }
        return out;
    }
     */
    private String[] cleanInputs(String[] inputArray) {
        List<String> result = new ArrayList(inputArray.length);
        for (String input : inputArray) {
            if (input != null) {
                String str = input.trim();
                if (!str.isEmpty()) {
                    result.add(str);
                }
            }
        }
        return (String[]) result.toArray(new String[0]);
    }

    public int findPopular(int[] a) {
        return findPopular(a, 1);
    }

    public int findPopular(int[] a, int mostpopular) {
        if ((a == null) || (a.length == 0)) {
            return 0;
        }
        Arrays.sort(a);

        int previous = a[0];
        int popular = a[0];
        int count = 1;
        int maxCount = 1;
        for (int i = 1; i < a.length; i++) {
            if (a[i] == previous) {
                if (a[i] != mostpopular) {
                    count++;
                }
            } else {
                if (count > maxCount) {
                    popular = a[(i - 1)];
                    maxCount = count;
                }
                previous = a[i];
                count = 1;
            }
        }
        return mostpopular > -1 ? popular : count > maxCount ? a[(a.length - 1)] : findPopular(a, popular);
    }

    public int randInt(int min, int max) {
        Random rand = new Random();

        int randomNum = rand.nextInt(max - min + 1) + min;

        return randomNum;
    }
}
