package scraper;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import scraper.internal.FileUtils;
import scraper.internal.OS;

/**
 * This class requires the WindowsAPI.exe file.
 * 
 * @author jmirra
 * 
 */
public final class WindowsAPI {

  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(WindowsAPI.class);

  private static final boolean DEBUG = false;

  private static Process process;
  private static PrintStream output;
  private static BufferedReader input;

  static {
    try {
      File exeDir =
          new File(OS.getLocalAppFolder("AMP" + File.separatorChar + "BrokerageDataServer"
              + File.separatorChar + "executables"));
      if (!exeDir.exists()) {
        FileUtils.mkdirs(exeDir);
      }
      File exeFile = FileUtils.getChildFile(exeDir, "WindowsAPI.exe");

      if (!exeFile.exists()) {
        // copy the executable out
        logger.debug("Extracting WindowsAPI.exe...");
        InputStream input = WindowsAPI.class.getResourceAsStream("WindowsAPI.exe");
        FileUtils.transfer(input, exeFile, new byte[2048]);
      }

      process = Runtime.getRuntime().exec(exeFile.getPath());
      output = new DebugPrintStream(process.getOutputStream());
      input = new DebugBufferedReader(new InputStreamReader(process.getInputStream()));
      readLine();

      Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
        public void run() {
          exit();
        }
      }));

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void setNormal(TargetWindow window) {
    write("setnormal " + window.getID());
  }

  public static void setNormal(StringNameCallback callback) {
    List<ListedFrame> list = getWindowIDs(callback);
    for (ListedFrame frame : list) {
      write("setnormal " + frame.id);
    }
  }

  public static void setLocation(int frameID, int x, int y) {
    write("move " + frameID + " " + x + " " + y);
  }

  public static void setSize(int frameID, int width, int height) {
    write("setsize " + frameID + " " + width + " " + height);
  }

  public static TargetWindow findWindow(StringNameCallback callback) {
    List<TargetWindow> windows = findWindows(callback);
    if (windows.size() == 0) {
      return null;
    }
    if (windows.size() > 1) {
      System.out.println("Warning, found multiple TargetWindows for findWindow() --> " + windows);
    }
    return windows.get(0);
  }

  public static List<TargetWindow> findWindows(StringNameCallback callback) {
    List<ListedFrame> list = getWindowIDs(callback);
    List<TargetWindow> ret = new ArrayList<TargetWindow>(list.size());
    for (ListedFrame frame : list) {
      write("getbounds " + frame.id);
      String boundsString = readLine();
      String[] m = boundsString.split(" ");
      Rectangle bounds =
          new Rectangle(Integer.valueOf(m[0]), Integer.valueOf(m[1]), Integer.valueOf(m[2]),
              Integer.valueOf(m[3]));
      ret.add(new TargetWindow(frame.id, frame.title, bounds));
    }
    return ret;
  }

  private static List<ListedFrame> getWindowIDs(StringNameCallback nameCallback) {
    write("list");

    List<ListedFrame> ret = new ArrayList<ListedFrame>();

    while (true) {
      String line = readLine();
      if (line == null) {
        return Collections.emptyList();
      }
      if (line.equals("!ENDLIST")) {
        break;
      }
      int spaceIndex = line.indexOf(' ');
      String title = line.substring(spaceIndex + 1);
      if (nameCallback.isTargetString(title)) {
        int id = Integer.valueOf(line.substring(0, spaceIndex - 1));
        ret.add(new ListedFrame(id, title));
      }
    }
    return ret;
  }

  private static void write(String s) {
    output.println(s);
    output.flush();
  }

  private static String readLine() {
    try {
      return input.readLine();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private WindowsAPI() {}

  public static void exit() {
    System.out.println("Exiting WindowsAPI");
    process.destroy();
  }

  private static class DebugPrintStream extends PrintStream {

    public DebugPrintStream(OutputStream out) {
      super(out);
    }

    @Override
    public void println(String x) {
      if (DEBUG) {
        System.out.println(x + " << WindowsAPI");
      }
      super.println(x);
    }

  }

  private static class DebugBufferedReader extends BufferedReader {

    public DebugBufferedReader(Reader in) {
      super(in);
    }

    @Override
    public String readLine() throws IOException {
      String ret = super.readLine();
      if (DEBUG) {
        System.out.println("WindowsAPI >> " + ret);
      }
      return ret;
    }

  }

  public static interface StringNameCallback {
    public boolean isTargetString(String s);
  }

  public static class StandardWindowNameCallback implements StringNameCallback {

    private final String targetWindowName;

    public StandardWindowNameCallback(String windowName) {
      if (windowName == null) {
        throw new IllegalArgumentException("windowName cannot be null");
      }
      this.targetWindowName = windowName;
    }

    @Override
    public boolean isTargetString(String windowName) {
      return this.targetWindowName.equals(windowName);
    }

  }

  public static class ContainsWindowNameCallback implements StringNameCallback {
    private final String targetWindowName;

    public ContainsWindowNameCallback(String windowName) {
      if (windowName == null) {
        throw new IllegalArgumentException("windowName cannot be null");
      }
      this.targetWindowName = windowName;
    }

    @Override
    public boolean isTargetString(String windowName) {
      return windowName.contains(this.targetWindowName);
    }
  }

  public static class StartsWithWindowNameCallback implements StringNameCallback {

    private final String targetWindowName;

    public StartsWithWindowNameCallback(String windowName) {
      if (windowName == null) {
        throw new IllegalArgumentException("windowName cannot be null");
      }
      this.targetWindowName = windowName;
    }

    @Override
    public boolean isTargetString(String windowName) {
      return windowName.startsWith(targetWindowName);
    }

  }

  private static class ListedFrame {
    final int id;
    final String title;

    public ListedFrame(int id, String title) {
      this.id = id;
      this.title = title;
    }
  }

  public static void main(String[] args) {
    List<TargetWindow> windows = findWindows(new ContainsWindowNameCallback(""));
    for (TargetWindow window : windows) {
      if (window.getBounds().width > 800 && window.getBounds().height > 600) {
        System.out.println(window.getTitle() + " -> " + window.getBounds());
      }
    }
  }

}
