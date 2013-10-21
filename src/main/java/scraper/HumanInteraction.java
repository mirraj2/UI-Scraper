package scraper;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A robot which uses the mouse and keyboard like a human.
 */
public final class HumanInteraction {

  @SuppressWarnings("unused")
  private static final Logger logger = LoggerFactory.getLogger(HumanInteraction.class);

  private static long lastActiveTime = 0;
  private static boolean userActive = false;
  private static final int TIME_UNTIL_INACTIVE = 500;

  private static Robot robot;
  private static final int MIN_KEY_DELAY = 50;
  private static final int MAX_KEY_DELAY = 70;
  private static int moveSleepTime = 30;
  private static final int pressSleepTime = 30;
  private static boolean beNiceToUsers = true;

  static {
    try {
      robot = new Robot();
    } catch (AWTException e) {
      e.printStackTrace();
    }
    startUserActiveThread();
  }

  public static void main(String[] args) {
    for (int i = 0; i < 1000; i++) {
      robot.mousePress(InputEvent.BUTTON1_MASK);
      robot.mouseRelease(InputEvent.BUTTON1_MASK);
      sleep(10);
    }
  }

  public synchronized static void moveMouse(double x, double y) {
    if (beNiceToUsers && userActive) {
      throw new UserActiveException();
    }

    robot.mouseMove((int) x, (int) y);
    lastMouseLoc = new Point((int) x, (int) y);
    sleep(moveSleepTime);
  }

  public static void rightClick(int x, int y) {
    if (beNiceToUsers && userActive) {
      throw new UserActiveException();
    }
    moveMouse(x - 1, y);
    moveMouse(x, y);
    robot.mousePress(InputEvent.BUTTON3_MASK);
    sleep(pressSleepTime);
    robot.mouseRelease(InputEvent.BUTTON3_MASK);
    sleep(pressSleepTime);
  }

  public static void controlClick(double x, double y) {
    robot.keyPress(KeyEvent.VK_CONTROL);
    sleep(30);
    click(x, y);
    robot.keyRelease(KeyEvent.VK_CONTROL);
  }

  public synchronized static void click(double x, double y) {
    if (beNiceToUsers && userActive) {
      throw new UserActiveException();
    }

    moveMouse((int) x - 1, (int) y);
    sleep(pressSleepTime);
    moveMouse((int) x, (int) y);
    sleep(pressSleepTime);
    robot.mousePress(InputEvent.BUTTON1_MASK);
    sleep(pressSleepTime);
    robot.mouseRelease(InputEvent.BUTTON1_MASK);
    sleep(pressSleepTime);
  }

  public synchronized static void fastClick(double x, double y) {
    if (beNiceToUsers && userActive) {
      throw new UserActiveException();
    }

    robot.mouseMove((int) x - 1, (int) y);
    sleep(1);
    robot.mouseMove((int) x, (int) y);
    sleep(1);
    lastMouseLoc = new Point((int) x, (int) y);
    robot.mousePress(InputEvent.BUTTON1_MASK);
    sleep(1);
    robot.mouseRelease(InputEvent.BUTTON1_MASK);
    sleep(1);
  }

  public static void doubleClick(double x, double y) {
    click(x, y);
    click(x, y);
  }

  /**
   * Clicks somewhere on this location.
   */
  public static void click(Rectangle location) {
    if (location == null) {
      throw new IllegalArgumentException("location cannot be null.");
    }

    int xClick = (int) (Math.random() * location.width);
    int yClick = (int) (Math.random() * location.height);
    click(xClick + location.x, yClick + location.y);
  }

  public static void doubleClick(Rectangle location) {
    int xClick = (int) (Math.random() * location.width);
    int yClick = (int) (Math.random() * location.height);
    click(xClick + location.x, yClick + location.y);
    click(xClick + location.x, yClick + location.y);
  }

  public static void type(char c) {
    if (beNiceToUsers && userActive) {
      throw new UserActiveException();
    }

    boolean upperCase = Character.isUpperCase(c);
    if (upperCase) {
      robot.keyPress(KeyEvent.VK_SHIFT);
    }
    int code = getKeyCode(c);
    type(code);
    if (upperCase) {
      robot.keyRelease(KeyEvent.VK_SHIFT);
    }
  }

  public static void type(int keyCode) {
    if (beNiceToUsers && userActive) {
      throw new UserActiveException();
    }

    boolean shift = false;

    if (keyCode == KeyEvent.VK_COLON) {
      keyCode = KeyEvent.VK_SEMICOLON;
      shift = true;
    }

    if (shift) {
      robot.keyPress(KeyEvent.VK_SHIFT);
    }

    robot.keyPress(keyCode);
    robot.keyRelease(keyCode);

    if (shift) {
      robot.keyRelease(KeyEvent.VK_SHIFT);
    }
  }

  public static void typeViaCopyPaste(String s) {
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(s), null);
    type(KeyEvent.VK_CONTROL, KeyEvent.VK_V);
  }

  public static void type(String s) {
    if (s == null) {
      throw new IllegalArgumentException("s");
    }
    for (int i = 0; i < s.length(); i++) {
      sleep(getKeyDelay());
      type(s.charAt((i)));
    }
  }

  public static void type(int modifier, int keyCode) {
    if (beNiceToUsers && userActive) {
      throw new UserActiveException();
    }

    Toolkit.getDefaultToolkit().beep();

    robot.keyPress(modifier);
    sleep(30);
    robot.keyPress(keyCode);
    sleep(1000);
    robot.keyRelease(keyCode);
    sleep(30);
    robot.keyRelease(modifier);
    sleep(30);
  }

  private static int getKeyDelay() {
    return (int) (Math.random() * (MAX_KEY_DELAY - MIN_KEY_DELAY)) + MIN_KEY_DELAY;
  }

  private static int getKeyCode(char c) {
    if (Character.isLetter(c)) {
      if (Character.isUpperCase(c)) {
        return c;
      } else {
        return c - 'a' + 'A';
      }
    } else if (Character.isDigit(c)) {
      return c - '0' + KeyEvent.VK_0;
    } else if (c == ' ') {
      return KeyEvent.VK_SPACE;
    } else if (c == ':') {
      return KeyEvent.VK_COLON;
    } else if (c == '\\') {
      return KeyEvent.VK_BACK_SLASH;
    } else if (c == '(') {
      return KeyEvent.VK_LEFT_PARENTHESIS;
    } else if (c == ')') {
      return KeyEvent.VK_RIGHT_PARENTHESIS;
    } else if (c == '.') {
      return KeyEvent.VK_PERIOD;
    } else if (c == ',') {
      return KeyEvent.VK_COMMA;
    } else {
      throw new UnsupportedOperationException("Cannot type: " + c);
    }
  }

  public static void sleep(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  // no constructor
  private HumanInteraction() {}

  public static BufferedImage caputreScreen(Rectangle rect) {
    return robot.createScreenCapture(rect);
  }

  private static Point lastMouseLoc;

  private static void startUserActiveThread() {
    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        lastMouseLoc = MouseInfo.getPointerInfo().getLocation();
        while (true) {
          sleep(80);
          synchronized (HumanInteraction.class) {
            PointerInfo pointerInfo = MouseInfo.getPointerInfo();
            if (pointerInfo == null) {
              // something happened to the mouse -- screensaver?
              continue;
            }
            Point currentLoc = pointerInfo.getLocation();
            if (currentLoc.equals(lastMouseLoc) == false) {
              userActive = true;
              lastActiveTime = System.currentTimeMillis();
            } else if (userActive) {
              if (System.currentTimeMillis() - lastActiveTime > TIME_UNTIL_INACTIVE) {
                userActive = false;
              }
            }
            lastMouseLoc = currentLoc;
          }
        }
      }
    });
    t.setDaemon(true);
    t.start();
  }

  public static void dumpScreenshot(Rectangle bounds) {
    BufferedImage bi = caputreScreen(bounds);
    dumpImage(bi);
  }

  public static void dumpImage(BufferedImage image) {
    File folder = new File("dump/");
    folder.mkdirs();
    File ff = new File(folder, (c++) + ".png");
    try {
      ImageIO.write(image, "png", ff);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static int c = 0;

  public static void setBeNiceToUsers(boolean b) {
    beNiceToUsers = b;
  }

  public static boolean isUserActive() {
    return userActive;
  }

  public static void waitForNotActive() {
    while (isUserActive()) {
      sleep(10);
    }
  }

}
