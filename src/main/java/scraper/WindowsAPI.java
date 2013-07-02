package scraper;

import java.awt.Rectangle;
import java.util.List;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.win32.StdCallLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class WindowsAPI {

  @SuppressWarnings("unused")
  private static final Logger logger = LoggerFactory.getLogger(WindowsAPI.class);

  public static void sendToFront(TargetWindow window) {
    Pointer p = Pointer.createConstant(window.getID());
    User32.INSTANCE.ShowWindow(p, 1);
    User32.INSTANCE.SetForegroundWindow(p);
    HumanInteraction.sleep(1000);
  }

  public static TargetWindow findWindow(String windowName) {
    return Iterables.getOnlyElement(findWindows(windowName));
  }

  public static List<TargetWindow> findWindows(String windowName) {
    final String searchString = windowName.toLowerCase();

    final List<TargetWindow> ret = Lists.newArrayList();

    User32.INSTANCE.EnumWindows(new User32.WNDENUMPROC() {
      public boolean callback(Pointer hWnd, Pointer userData) {
        byte[] windowText = new byte[512];
        User32.INSTANCE.GetWindowTextA(hWnd, windowText, 512);
        String title = Native.toString(windowText).trim();

        if (title.isEmpty()) {
          return true;
        }

        if (!title.toLowerCase().contains(searchString)) {
          return true;
        }


        ret.add(new TargetWindow(Pointer.nativeValue(hWnd), title));

        return true;
      }
    }, null);

    return ret;
  }

  public static Rectangle getBounds(TargetWindow targetWindow) {
    RECT r = new RECT();
    User32.INSTANCE.GetWindowRect(Pointer.createConstant(targetWindow.getID()), r);
    Rectangle bounds = new Rectangle(r.left, r.top, r.right - r.left, r.bottom - r.top);
    return bounds;
  }

  // Equivalent JNA mappings
  public interface User32 extends StdCallLibrary {
    User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class);

    interface WNDENUMPROC extends StdCallCallback {
      boolean callback(Pointer hWnd, Pointer arg);
    }

    boolean EnumWindows(WNDENUMPROC lpEnumFunc, Pointer arg);

    int GetWindowTextA(Pointer hWnd, byte[] lpString, int nMaxCount);
    
    boolean GetWindowRect(Pointer hWnd, RECT rect);
    
    boolean SetForegroundWindow(Pointer hWnd);

    boolean ShowWindow(Pointer hWnd, int nCmdShow);
  }

  public static void main(String[] args) {
    // for (TargetWindow w : findWindows("Magic Online")) {
    // sendToFront(w);
    // }
  }

}
