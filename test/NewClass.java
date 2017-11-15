
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author dmitry
 */
public class NewClass {

    public static void main(String[] args) throws IOException {

        BufferedImage hugeImage = ImageIO.read(new URL("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ-EHZMV5nJxQb5ZTIj8W4sragljrr_GlDxUcEmkpM8qdeMy100"));

        long startTime = System.nanoTime();
        int[][] result = convertTo2DUsingGetRGB(hugeImage);

    }

    private static int[][] convertTo2DUsingGetRGB(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] result = new int[height][width];

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                result[row][col] = image.getRGB(col, row);
            }
        }

        return result;
    }

    private static byte[][] convertTo2DWithoutUsingGetRGB(BufferedImage image) {

        final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        final int width = image.getWidth();
        final int height = image.getHeight();
        final boolean hasAlphaChannel = image.getAlphaRaster() != null;

        byte[][] result = new byte[height][width];

        final int pixelLength = 4;
        for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
            int argb = 0;
            argb += hasAlphaChannel ? (((int) pixels[pixel] & 0xff) << 24) : 0; // alpha
            if (argb == 100) {
                int c = ((int) pixels[pixel + 1] & 0xff);
                argb += c < 255 / 2 ? 0 : 1;

                c = (((int) pixels[pixel + 2] & 0xff) << 8); // green
                argb += c < 255 / 2 ? 0 : 1;
                c = (((int) pixels[pixel + 3] & 0xff) << 16); // red
                argb += c < 255 / 2 ? 0 : 1;
                argb = argb > 1 ? 1 : 0;
            } else {
                argb = 0;
            }

            result[row][col] = (byte) argb;

            col++;
            if (col == width) {
                col = 0;
                row++;
            }
        }

        return result;
    }

    private static byte[][] prepare(byte[][] c) {
        int k = 0;
        for (int i = 0; i < c.length; i++) {
            for (int j = 0; j < c[i].length; j++) {
                if (c[i][j] == 0) {
                    k--;
                } else {
                    k++;
                }
            }
        }
        if (k > 0) {
            for (int i = 0; i < c.length; i++) {
                for (int j = 0; j < c[i].length; j++) {
                    c[i][j] = (byte) (c[i][j] ^ 1);
                }
            }
        }
        return c;
    }

    private static String toString(long nanoSecs) {
        int minutes = (int) (nanoSecs / 60000000000.0);
        int seconds = (int) (nanoSecs / 1000000000.0) - (minutes * 60);
        int millisecs = (int) (((nanoSecs / 1000000000.0) - (seconds + minutes * 60)) * 1000);

        if (minutes == 0 && seconds == 0) {
            return millisecs + "ms";
        } else if (minutes == 0 && millisecs == 0) {
            return seconds + "s";
        } else if (seconds == 0 && millisecs == 0) {
            return minutes + "min";
        } else if (minutes == 0) {
            return seconds + "s " + millisecs + "ms";
        } else if (seconds == 0) {
            return minutes + "min " + millisecs + "ms";
        } else if (millisecs == 0) {
            return minutes + "min " + seconds + "s";
        }

        return minutes + "min " + seconds + "s " + millisecs + "ms";
    }

}
