package scraper;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

public class ScreenScraper {

  @SuppressWarnings("unused")
  private static final Logger logger = LoggerFactory.getLogger(ScreenScraper.class);

  private static final int THRESHOLD = 99;
  private static final int BLACK_RGB = Color.black.getRGB();

  private final Dimension screenSize;
  private Robot bot;
  private HashMap<BufferedImage, Point> cache = new HashMap<BufferedImage, Point>();

  public ScreenScraper() {
    screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    try {
      bot = new Robot();
    } catch (AWTException e) {
      e.printStackTrace();
    }
  }

  public Robot getBot() {
    return bot;
  }

  public boolean isAllSingleColor(Rectangle r, Color c) {
    BufferedImage bi = bot.createScreenCapture(r);
    int rgb = c.getRGB();
    for (int i = 0; i < bi.getWidth(); i++) {
      for (int j = 0; j < bi.getHeight(); j++) {
        int rgb2 = bi.getRGB(i, j);
        if (rgb != rgb2) {
          return false;
        }
      }
    }
    return true;
  }

  public static boolean isAboutSameColor(int rgbA, int rgbB) {
    return isAboutSameColor(rgbA, rgbB, THRESHOLD);
  }

  public static boolean isAboutSameColor(int rgbA, int rgbB, int threshold) {
    int combinedDif =
        ((rgbA & 0xFF) - (rgbB & 0xFF)) + (((rgbA >> 8) & 0xFF) - ((rgbB >> 8) & 0xFF))
            + (((rgbA >> 16) & 0xFF) - ((rgbB >> 16) & 0xFF));
    return Math.abs(combinedDif) <= threshold;
  }

  /**
   * Finds the given image inside of the application window.
   * 
   * @return The location of the image on the screen or null if it was not found.
   */
  public Rectangle getLocationOf(BufferedImage image) {
    return getLocationOf(image, null);
  }

  public Rectangle getLocationOf(BufferedImage image, boolean debug) {
    return getLocationOf(image, null, null, debug);
  }

  public Rectangle getLocationOf(BufferedImage image, Rectangle rect) {
    return getLocationOf(image, rect, null, false);
  }

  static int debug_count = 0;

  public Rectangle getLocationOf(BufferedImage image, Rectangle rect, Boolean matchBlackPixels,
      boolean debug) {
    if (image == null) {
      throw new IllegalArgumentException("image cannot be null");
    }
    if (rect == null) {
      rect = new Rectangle(0, 0, screenSize.width, screenSize.height);
    }

    BufferedImage bi = bot.createScreenCapture(rect);

    Rectangle ret = locateInImage(image, bi, matchBlackPixels, debug);
    if (ret != null) {
      // translate to global coordinates
      ret.x += rect.x;
      ret.y += rect.y;
    }
    return ret;
  }

  public static void main(String[] args) throws Exception {
    BufferedImage big = ImageIO.read(new File("C:/dump/0.png"));
    BufferedImage small = ImageIO.read(new File("C:/dump/1.png"));
    
    Rectangle r = new ScreenScraper().locateInImage(small, big, false, false);

    System.out.println(r);
  }

  private double getPercentageBlackPixels(BufferedImage bi) {
    int c = 0;
    for (int i = 0; i < bi.getWidth(); i++) {
      for (int j = 0; j < bi.getHeight(); j++) {
        if (bi.getRGB(i, j) == BLACK_RGB) {
          c++;
        }
      }
    }
    return 1d * c / (bi.getWidth() * bi.getHeight());
  }

  public Rectangle locateInImage(BufferedImage smallImage, BufferedImage largeImage,
      Boolean matchBlackPixels, boolean debug) {
    checkNotNull(smallImage);
    checkNotNull(largeImage);

    int w = smallImage.getWidth();
    int h = smallImage.getHeight();

    if (matchBlackPixels == null) {
      matchBlackPixels = getPercentageBlackPixels(smallImage) > .1;
    }

    // Check the cache
    Point loc = cache.get(smallImage);
    if (loc != null) {
      if (isImageAt(smallImage, largeImage, loc.x, loc.y, w, h, matchBlackPixels)) {
        return new Rectangle(loc.x, loc.y, w, h);
      }
    }

    // Search Everywhere
    int i, j;
    for (i = 0; i <= largeImage.getWidth() - smallImage.getWidth(); i++) {
      for (j = 0; j <= largeImage.getHeight() - smallImage.getHeight(); j++) {
        if (isImageAt(smallImage, largeImage, i, j, w, h, matchBlackPixels)) {
          Rectangle ret = new Rectangle(i, j, w, h);
          // cache the result
          cache.put(smallImage, new Point(ret.x, ret.y));
          return ret;
        }
      }
    }

    // We could not find it
    if (debug) {
      HumanInteraction.dumpImage(largeImage);
      HumanInteraction.dumpImage(smallImage);
    }

    return null;
  }

  private boolean isImageAt(BufferedImage smallImage, BufferedImage largeImage, int i, int j, int w, int h,
      boolean onlyMatchBlack) {
    if (i + w > largeImage.getWidth() || j + h > largeImage.getHeight()) {
      return false;
    }

    int x, y;
    for (x = 0; x < w; x++) {
      for (y = 0; y < h; y++) {
        int rgbA = largeImage.getRGB(i + x, y + j);
        int rgbB = smallImage.getRGB(x, y);
        if (onlyMatchBlack) {
          if (isAboutSameColor(rgbA, BLACK_RGB) ^ isAboutSameColor(rgbB, BLACK_RGB)) {
            return false;
          }
        } else {
          if (!isAboutSameColor(rgbA, rgbB)) {
            return false;
          }
        }
      }
    }
    return true;
  }

}
