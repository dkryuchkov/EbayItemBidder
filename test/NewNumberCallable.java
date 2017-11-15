/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bidder;

import static bidder.ImageRecog.length;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.commons.lang3.ArrayUtils;
import org.yccheok.numrecognition.FeatureParameter;
import org.yccheok.numrecognition.NumberImageFeatureFactory;
import org.yccheok.numrecognition.NumberImageLRTBHVFeatureFactory;
import org.yccheok.numrecognition.NumberNeuralNetworkRecognizer;

/**
 *
 * @author dmitry
 */
public class NewNumberCallable implements Callable{

    String file = null;
    int r = 0;
    
    public NewNumberCallable(String file, int r)
    {
        this.file = file;
        this.r = r;
    }
    
    @Override
    public int[] call() throws Exception {
           NumberImageFeatureFactory imageFeature = new NumberImageLRTBHVFeatureFactory();
        NumberNeuralNetworkRecognizer neuralNetwork = new NumberNeuralNetworkRecognizer(
                "C:\\git\\EbayItemBidder\\lib\\LRTBHV-training-data.txt-I=96-H=200-LR=0.9-M=0.1-C=2000-.snet", imageFeature, FeatureParameter.DEFAULT_FEATURE_PARAMETER);
        String[] s = null;
        int k = 0;
        while ((s == null || s.length < length) && k++ < 20) {

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
        int[] s1 = new int[length];
        for (int i = 0; i < length && i < s.length; i++) {
            s1[i] = Integer.valueOf(s[i]);
        }

        new File(file).delete();
        return s1;
    }
    
     private String[] cleanInputs(String[] inputArray) {
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

    
}
