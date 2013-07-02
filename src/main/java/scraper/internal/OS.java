package scraper.internal;

import java.io.File;

public final class OS {


  public static enum OS_Type {
    WINDOWS, MAC, LINUX, UNKNOWN
  }

  public static final OS_Type type;

  static {
    String name = System.getProperty("os.name");
    if (name.contains("Windows") || name.contains("windows")) {
      type = OS_Type.WINDOWS;
    } else if (name.contains("Mac")) {
      type = OS_Type.MAC;
    } else if (name.contains("linux") || name.contains("Linux")) {
      type = OS_Type.LINUX;
    } else {
      type = OS_Type.UNKNOWN;
    }
  }

  public static String getLocalAppFolder(String appName) {
    String ret;
    if (type == OS_Type.WINDOWS) {
      ret = System.getenv("LOCALAPPDATA");
      if (ret == null) {
        ret =
            System.getProperty("user.home") + File.separatorChar + "Local Settings"
                + File.separatorChar + "Application Data";
      }
    } else {
      appName = "." + appName;
      ret = System.getProperty("user.home");
    }
    if (!ret.endsWith(File.separator)) {
      ret = ret + File.separatorChar;
    }
    ret = ret + appName + File.separatorChar;

    File file = new File(ret);
    if (!file.exists()) {
      FileUtils.mkdirs(file);
    }


    return ret;
  }

  public static File getDownloadsFolder() {
    StringBuilder ret = new StringBuilder();
    ret.append(System.getProperty("user.home"));
    if (ret.charAt(ret.length() - 1) != File.separatorChar) {
      ret.append(File.separatorChar);
    }
    ret.append("Downloads");

    String path = ret.toString();

    File file = new File(path);
    if (!file.exists()) {
      return null;
    }

    return file;
  }

  public static String getTemporaryFolder() {
    String t = System.getProperty("java.io.tmpdir");
    if (!t.endsWith(File.separator)) {
      t = t + File.separatorChar;
    }
    return t;
  }

  public static String getDesktop() {
    String t = System.getProperty("user.home");
    if (!t.endsWith(File.separator)) {
      t = t + File.separatorChar;
    }
    t += "Desktop" + File.separatorChar;
    return t;
  }
}
