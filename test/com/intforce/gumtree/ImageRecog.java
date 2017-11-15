package com.intforce.gumtree;

import static com.intforce.gumtree.repost.projectFolder;
import static bidder.Gumtree.loadImage;
import ij.ImagePlus;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.apache.commons.lang3.ArrayUtils;
import org.yccheok.numrecognition.FeatureParameter;
import org.yccheok.numrecognition.NumberImageFeatureFactory;
import org.yccheok.numrecognition.NumberImageLRTBHVFeatureFactory;
import org.yccheok.numrecognition.NumberImageProcessor;
import org.yccheok.numrecognition.NumberNeuralNetworkRecognizer;

/**
 *
 * @author dmitry
 */
public class ImageRecog {

    static final int length = 4;

    static final int count = 40;

    public static void main(String[] args) {
        //String n = getNumber("http://www.gumtree.com.au/bb-image.html?tok=1e5236ded5e8903554adb153c86607c1add9e8794d939aefaf5f3830fb539fda9222231a9c51c7e0ee6abaf33bddf0bdc911fcfec64ca6888c124006e8c7ee0a");
        // String n = getNumber("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ-EHZMV5nJxQb5ZTIj8W4sragljrr_GlDxUcEmkpM8qdeMy100", 3);

        //String n = getNumber("http://www.gumtree.com.au/bb-image.html?tok=1e5236ded5e8903554adb153c86607c1add9e8794d939aefaf5f3830fb539fda9222231a9c51c7e0ee6abaf33bddf0bdc911fcfec64ca6888c124006e8c7ee0a");
        //String n = getNumber("http://cgi4.ebay.com.au/ws/eBayISAPI.dll?LoadBotImage&siteid=15&co_brandId=2&tokenString=m%2BTSRwwAAAA%3D&t=1457398749858&hc=1&hm=uk%601d72f%2B633%3E");

        //System.out.println(n);
    }

    public static String getNumberURL(String url, String user) {
        int[] s1 = new int[count];
        String out = "";
        int r = 0;

        for (int j = 0; j < count; j++) {
            String file = loadImage(url, user);
            NumberImageLRTBHVFeatureFactory imageFeature = new NumberImageLRTBHVFeatureFactory();

            NumberNeuralNetworkRecognizer neuralNetwork = new NumberNeuralNetworkRecognizer(
                    projectFolder + "lib/LRTBHV-training-data.txt-I=96-H=200-LR=0.9-M=0.1-C=2000-.snet", imageFeature, FeatureParameter.DEFAULT_FEATURE_PARAMETER);

            s1[j] = neuralNetwork.recognize(new NumberImageProcessor(new ImagePlus(url))); //neuralNetwork.recognize(file, r);

        }

        return String.valueOf(findPopular(s1));

    }

    public static String getNumber(String url, String user) {
        return getNumber(url, length, user);
    }

    public static String getNumber(String url, int length, String user) {
        int[][] s1 = new int[length][count];
        String out = "";
        int r = 0;
        for (int j = 0; j < count; j++) {
            String file = loadImage(url, user);
            NumberImageFeatureFactory imageFeature = new NumberImageLRTBHVFeatureFactory();

            NumberNeuralNetworkRecognizer neuralNetwork = new NumberNeuralNetworkRecognizer(
                    "C:\\git\\EbayItemBidder\\lib\\LRTBHV-training-data.txt-I=96-H=200-LR=0.9-M=0.1-C=2000-.snet", imageFeature, FeatureParameter.DEFAULT_FEATURE_PARAMETER);
            String[] s = null;
            int k = 0;
            while ((s == null || s.length < length) && k++ < 20) {
                r = randInt(51, 200);
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

            for (int i = 0; i < length && i < s.length; i++) {
                s1[i][j] = Integer.valueOf(s[i]);
            }
        }
        out = "";
        for (int i = 0; i < length; i++) {
            out += String.valueOf(findPopular(s1[i])).trim();
        }
        return out;
    }

    private static String[] cleanInputs(String[] inputArray) {
        List<String> result = new ArrayList<String>(inputArray.length);
        for (String input : inputArray) {
            if (input != null) {
                String str = input.trim();
                if (!str.isEmpty()) {
                    result.add(str);
                }
            }
        }
        return result.toArray(new String[]{});
    }

    public static int findPopular(int[] a) {
        return findPopular(a, -1);
    }

    public static int findPopular(int[] a, int mostpopular) {

        if (a == null || a.length == 0) {
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
                    popular = a[i - 1];
                    maxCount = count;
                }
                previous = a[i];
                count = 1;
            }
        }

        return count > maxCount ? a[a.length - 1] : (mostpopular > -1 ? popular : findPopular(a, popular));

    }

    public static int randInt(int min, int max) {

        // NOTE: This will (intentionally) not run as written so that folks
        // copy-pasting have to think about how to initialize their
        // Random instance.  Initialization of the Random instance is outside
        // the main scope of the question, but some decent options are to have
        // a field that is initialized once and then re-used as needed or to
        // use ThreadLocalRandom (if using at least Java 1.7).
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

}
