package scraper;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;

import javax.imageio.ImageIO;

import com.google.common.base.Stopwatch;

public class ConvolveTest {

  public static void main(String[] args) throws Exception {
    BufferedImage bi = ImageIO.read(ConvolveTest.class.getResourceAsStream("input.jpg"));
    BufferedImage lisa = ImageIO.read(ConvolveTest.class.getResourceAsStream("lisa.png"));

    for (int i = 0; i < 10; i++) {
      Stopwatch w = new Stopwatch().start();
      Rectangle r = new ScreenScraper().locateInImage(lisa, bi, null, false);
      System.out.println(w + " :: " + r);
    }

    

    for (int t = 0; t < 10; t++) {
      Stopwatch w = new Stopwatch().start();
      float[][] dimg = greyscale(bi);
      System.out.println("gs1: " + w);
      float[][] dsearch = greyscale(lisa);
      System.out.println("gs2: " + w);
      float[][] dconv = convolve(dimg, dsearch);
      System.out.println("convolve: " + w);

      int x = 0, y = 0;
      for (int i = 0; i < dconv.length; i++) {
        for (int j = 0; j < dconv[0].length; j++) {
          if (dconv[i][j] < dconv[x][y]) {
            x = i;
            y = j;
          }
        }
      }
      System.out.println(w + " Found her at " + x + "," + y);
    }

    // HumanInteraction.dumpImage(bi.getSubimage(x, y, 100, 100));
  }

  private static float[][] convolve(float[][] dimg, float[][] dsearch) {
    int w = dimg.length, h = dimg[0].length;
    int iw = dsearch.length, ih = dsearch[0].length;

    float[][] ret = new float[w - iw][h - ih];
    for (int i = 0; i < w - iw; i++) {
      for (int j = 0; j < h - ih; j++) {
        float sum = 0;
        for (int x = 0; x < iw; x++) {
          for (int y = 0; y < ih; y++) {
            float diff = dimg[i + x][j + y] - dsearch[x][y];
            sum += diff * diff;
          }
        }
        ret[i][j] = sum / (iw * ih); // ret[i][j] is 0 if the patch is identical
      }
    }
    return ret;
  }

  private static float[][] greyscale(BufferedImage bi) {
    int w = bi.getWidth();
    int h = bi.getHeight();
    float[][] ret = new float[w][h];
    float[] rs = new float[w * h];
    float[] gs = new float[w * h];
    float[] bs = new float[w * h];
    Raster raster = bi.getData();
    raster.getSamples(0, 0, w, h, 0, gs);
    raster.getSamples(0, 0, w, h, 1, gs);
    raster.getSamples(0, 0, w, h, 2, bs);

    for (int i = 0; i < w; i++) {
      for (int j = 0; j < h; j++) {
        float r = rs[j * w + i] / 255.0f;
        float g = bs[j * w + i] / 255.0f;
        float b = gs[j * w + i] / 255.0f;

        float grey = (r + g + b) / 3; // avg, scale 0 to 1
        ret[i][j] = grey;
      }
    }
    return ret;
  }
}