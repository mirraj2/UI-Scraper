package scraper;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import org.apache.log4j.Logger;

public class ScreenScraper {

  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(ScreenScraper.class);

  private static final int THRESHOLD = 90;

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
    return getLocationOf(image, null, debug);
  }

  public Rectangle getLocationOf(BufferedImage image, Rectangle rect) {
    return getLocationOf(image, rect, false);
  }

  static int debug_count = 0;

  public Rectangle getLocationOf(BufferedImage image, Rectangle rect, boolean debug) {
    if (image == null) {
      throw new IllegalArgumentException("image cannot be null");
    }
    if (rect == null) {
      rect = new Rectangle(0, 0, screenSize.width, screenSize.height);
    }

    BufferedImage bi = bot.createScreenCapture(rect);

    Rectangle ret = locateInImage(image, bi, debug);
    if (ret != null) {
      // translate to global coordinates
      ret.x += rect.x;
      ret.y += rect.y;
    }
    return ret;
  }

  public Rectangle locateInImage(BufferedImage image, BufferedImage bi, boolean debug) {
    if (image == null) {
      throw new IllegalArgumentException("image cannot be null");
    }
    if (bi == null) {
      throw new IllegalArgumentException("bi cannot be null");
    }

    int w = image.getWidth();
    int h = image.getHeight();

    // Check the cache
    Point loc = cache.get(image);
    if (loc != null) {
      if (isImageAt(image, bi, loc.x, loc.y, w, h)) {
        return new Rectangle(loc.x, loc.y, w, h);
      }
    }

    // Search Everywhere
    int i, j;
    for (i = 0; i < bi.getWidth() - image.getWidth(); i++) {
      for (j = 0; j < bi.getHeight() - image.getHeight(); j++) {
        if (isImageAt(image, bi, i, j, w, h)) {
          Rectangle ret = new Rectangle(i, j, w, h);
          // cache the result
          cache.put(image, new Point(ret.x, ret.y));
          return ret;
        }
      }
    }

    // We could not find it
    if (debug) {
      HumanInteraction.dumpImage(bi);
      HumanInteraction.dumpImage(image);
    }

    return null;
  }

  private boolean isImageAt(BufferedImage image, BufferedImage bi, int i, int j, int w, int h) {
    int x, y;
    for (x = 0; x < w; x++) {
      for (y = 0; y < h; y++) {
        if (!isAboutSameColor(bi.getRGB(i + x, y + j), image.getRGB(x, y))) {
          return false;
        } else {
          // System.out.println(x+" "+y);
        }
      }
    }
    return true;
  }

}
