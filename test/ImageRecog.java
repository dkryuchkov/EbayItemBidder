package bidder;

import ij.ImagePlus;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
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

    static int count = 13;


    public static void main(String[] args) throws Exception {
        String url = "http://www.gumtree.com.au/bb-image.html?tok=7a379fb97a43b47b6248a4033dd97858cc521e29e8274d5e4f8d03059e4d76bcbf9f45921d64ed8d7fc1c2d7e486df0a938ee4be432fb82f81f76847a4885521";
        int j = 100;
        for (count = 5; count <= 20; count++) {
            int i = 1;
            while (i < 9 && i < j) {
                String number = getNumber(url, "", 68);
                System.out.println(number);
                if (number.equals("2891") && j > i) {
                    j = i;
                    System.out.println("i=" + i + " count=" + count);
                    break;
                }
                i++;
            }

        }
    }

    public static String getNumberURL(String url, String user) {
        int[] s1 = new int[count];
        String out = "";
        int r = 0;

        for (int j = 0; j < count; j++) {
            String file = new ImageRecog().loadImage(url, user);
            NumberImageLRTBHVFeatureFactory imageFeature = new NumberImageLRTBHVFeatureFactory();

            NumberNeuralNetworkRecognizer neuralNetwork = new NumberNeuralNetworkRecognizer(
                    "C:\\git\\EbayItemBidder\\lib\\LRTBHV-training-data.txt-I=96-H=200-LR=0.9-M=0.1-C=2000-.snet", imageFeature, FeatureParameter.DEFAULT_FEATURE_PARAMETER);

            s1[j] = neuralNetwork.recognize(new NumberImageProcessor(new ImagePlus(url))); //neuralNetwork.recognize(file, r);

        }

        return String.valueOf(findPopular(s1));

    }

    public static void deleteAllBNP() {
        for (File f : (new File("./")).listFiles()) {
            if (f.getName().endsWith(".bmp")) {
                f.delete();
            }
        }
    }

    public static String getNumber(String url, String user, int r) throws InterruptedException, ExecutionException {
        deleteAllBNP();
        return getNumber(url, length, user, r);
    }

    public static String getNumber(String url, String user) throws InterruptedException, ExecutionException {
        return getNumber(url, length, user, randInt(51, 200));
    }

    public static String getNumber(String url, int length, String user, int r) throws InterruptedException, ExecutionException {

        ExecutorService executor = Executors.newSingleThreadExecutor();
        List<Future<int[]>> futureList = new ArrayList<Future<int[]>>();
        int[][] s1 = new int[length][count];
        String out = "";
        for (int j = 0; j < count; j++) {
            String file = new ImageRecog().loadImage(url, user);

            NewNumberCallable newNumberCallable = new NewNumberCallable(file, r);
            futureList.add(executor.submit(newNumberCallable));

        }
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.MINUTES);

        out = "";
        for (int i = 0; i < length; i++) {
            int j = 0;
            for (Future future : futureList) {
                if (j > count || ((int[]) (future.get())).length - 1 < i) {
                    break;
                }
                try {
                    s1[i][j++] = ((int[]) future.get())[i];
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            out += String.valueOf(findPopular(s1[i])).trim();
        }
        return out;
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
