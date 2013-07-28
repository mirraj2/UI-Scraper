package scraper;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScraperUtils {

  @SuppressWarnings("unused")
  private static final Logger logger = LoggerFactory.getLogger(ScraperUtils.class);

  public static Rectangle waitFor(BufferedImage image, int timeout, ScreenScraper scraper) {
    return waitFor(image, timeout, scraper, null, null, false);
  }

  public static Rectangle waitFor(BufferedImage image, int timeout, ScreenScraper scraper,
      Rectangle imageArea, boolean debug) {
    return waitFor(image, timeout, scraper, imageArea, null, debug);
  }

  /**
   * Waits for the given image to appear.
   * 
   * @return The location of the image on the screen.
   */
  public static Rectangle waitFor(BufferedImage image, int timeout, ScreenScraper scraper,
      Rectangle imageArea, Boolean matchBlackPixels, boolean debug) {
    logger.debug("Searching for image....");
    long time = System.currentTimeMillis();
    while (true) {
      HumanInteraction.sleep(400);
      Rectangle loc = scraper.getLocationOf(image, imageArea, matchBlackPixels, false);
      if (loc != null) {
        logger.debug("Found image!");
        return loc;
      } else {
        logger.debug("still didnt find...");
      }
      if (System.currentTimeMillis() - time > timeout) {
        if (debug) {
          HumanInteraction.dumpScreenshot(imageArea);
          HumanInteraction.dumpImage(image);
        }
        return null;
      }
    }
  }

  /**
   * Waits until the given image has disappeared. If the image never comes into sight, this will return after the timeout.
   */
  public static void waitToDisappear(BufferedImage image, int timeout, ScreenScraper scraper,
      Rectangle location) {
    long time = System.currentTimeMillis();
    while (true) {
      HumanInteraction.sleep(400);
      Rectangle loc = scraper.getLocationOf(image, location);
      if (loc == null) {
        return;
      }
      if (System.currentTimeMillis() - time > timeout) {
        throw new RuntimeException("Image never disappeared.");
      }
    }
  }

  /**
   * Waits for a frame to appear which has the inputted title in its title.
   * 
   * @return The bounds of the frame on the screen.
   */
  public static TargetWindow waitForFrameToAppear(int timeout, String windowName) {
    long time = System.currentTimeMillis();
    while (true) {
      HumanInteraction.sleep(100);
      List<TargetWindow> windows = WindowsAPI.findWindows(windowName);
      for (TargetWindow window : windows) {
        if (isOnScreen(window)) {
          return window;
        }
      }
      if (System.currentTimeMillis() - time > timeout) {
        return null;
      }
    }
  }

  public static boolean isOnScreen(TargetWindow window) {
    GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();

    GraphicsDevice[] devices = e.getScreenDevices();

    for (GraphicsDevice gd : devices) {
      if (gd.getDefaultConfiguration().getBounds().intersects(window.getBounds())) {
        return true;
      }
    }

    return false;
  }

  public static String getClipboard() {
    Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

    try {
      if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
        return (String) t.getTransferData(DataFlavor.stringFlavor);
      } else {
        return null;
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
