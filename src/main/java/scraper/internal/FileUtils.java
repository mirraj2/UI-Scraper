package scraper.internal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.google.common.collect.Lists;

import static com.google.common.base.Preconditions.checkNotNull;

public class FileUtils {

  public static void write(byte[] data, File file) {
    write(data, file, false);
  }

  public static void write(byte[] data, File file, boolean mkdirsIfNecessary) {
    checkNotNull(data, "data");
    checkNotNull(file, "file");

    try {
      if (mkdirsIfNecessary) {
        File canonFile = file.getCanonicalFile();
        canonFile = canonFile.getParentFile();
        if (!canonFile.exists()) {
          FileUtils.mkdirs(canonFile);
        }
      }

      FileOutputStream fos = new FileOutputStream(file);
      fos.write(data);
      fos.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static byte[] read(File file) {
    try {
      FileInputStream fileInputStream = new FileInputStream(file);
      byte[] ret = read(fileInputStream);
      fileInputStream.close();
      return ret;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static byte[] read(InputStream is) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();

    transfer(is, bos);

    return bos.toByteArray();
  }

  public static void transfer(InputStream is, File targetFile, byte[] buf) {
    try {
      FileOutputStream fos = new FileOutputStream(targetFile);
      transfer(is, fos, buf);
      fos.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void transfer(File subFile, OutputStream outputStream, byte[] buf) {
    try {
      transfer(new FileInputStream(subFile), outputStream, buf);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private static void transfer(InputStream is, OutputStream os) {
    transfer(is, os, new byte[1024]);
  }

  private static void transfer(InputStream is, OutputStream os, byte[] buf) {
    try {
      int read;
      while (true) {
        read = is.read(buf);
        if (read < 0) {
          break;
        }
        os.write(buf, 0, read);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static void delete(File file) {
    checkNotNull(file);
    if (!file.exists()) {
      throw new IllegalArgumentException("File does not exist: " + file);
    }
    if (file.isDirectory()) {
      throw new IllegalArgumentException("Use deleteDirectory() to delete a directory. " + file);
    }
    if (!file.delete()) {
      throw new RuntimeException("Could not delete: " + file);
    }
  }

  public static void deleteDirectory(File file) {
    checkNotNull(file);
    if (!file.exists()) {
      throw new IllegalArgumentException("Directory does not exist: " + file);
    }
    if (!file.isDirectory()) {
      throw new IllegalArgumentException("Not a directory.");
    }
    recursiveDelete(file);
  }

  private static void recursiveDelete(File file) {
    for (File child : file.listFiles()) {
      if (child.isDirectory()) {
        recursiveDelete(child);
      } else {
        if (!child.delete()) {
          throw new RuntimeException("Could not delete: " + child);
        }
      }
    }
    if (!file.delete()) {
      throw new RuntimeException("Could not delete: " + file);
    }
  }

  public static void mkdirs(File file) {
    if (!file.mkdirs()) {
      throw new RuntimeException("Could not mkdirs: " + file);
    }
  }

  /**
   * Copies a file from one location to another.
   */
  public static void copy(File from, File to) {
    InputStream is = null;
    OutputStream os = null;

    try {
      is = new FileInputStream(from);
      os = new FileOutputStream(to);

      byte[] buf = new byte[1024];

      int i;
      while (true) {
        i = is.read(buf);
        if (i == -1) {
          break;
        }
        os.write(buf, 0, i);
      }

    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      try {
        if (is != null) {
          is.close();
        }
        if (os != null) {
          os.close();
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static File makeChildFolder(File parent, String folderName) {
    checkNotNull(parent, "parent");
    checkNotNull(folderName, "folderName");

    String path = parent.isDirectory() ? parent.getPath() : parent.getParent();
    path = endWithSlash(path);

    File ret = new File(path + folderName);
    mkdirs(ret);
    return ret;
  }

  public static File getChildFile(File parent, String fileName) {
    checkNotNull(parent, "parent");
    checkNotNull(fileName, "fileName");

    String path = parent.getPath();
    path = endWithSlash(path);

    return new File(path + fileName);
  }

  private static String endWithSlash(String path) {
    if (!path.endsWith(File.separator)) {
      return path + File.separatorChar;
    }
    return path;
  }

  public static File appendToName(String s, File file) {
    checkNotNull(s, "s");
    checkNotNull(file, "file");

    String path = file.getParent();
    path = endWithSlash(path);

    String name = file.getName() + s;

    return new File(path + name);
  }

  public static List<File> listFilesWithExtenstion(File parent, String ext) {
    checkNotNull(parent, "parent");
    checkNotNull(ext, "ext");

    if (!parent.exists()) {
      throw new IllegalArgumentException("File does not exist: " + parent);
    }

    if (!parent.isDirectory()) {
      throw new IllegalArgumentException("File is not a directory: " + parent);
    }

    List<File> ret = Lists.newArrayList();

    for (File child : parent.listFiles()) {
      if (child.getName().endsWith(ext)) {
        ret.add(child);
      }
    }

    return ret;
  }
}
