package scraper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class UserExperience {

  public static void callUntilCompleted(Object obj, String methodName, Object... params) {
    Method[] methods = obj.getClass().getMethods();
    for (Method m : methods) {
      if (m.getName().equals(methodName)) {
        callUntilCompleted(obj, m, params);
        return;
      }
    }
  }

  private static void callUntilCompleted(Object obj, Method m, Object... params) {
    while (true) {
      try {
        m.invoke(obj, params);
      } catch (InvocationTargetException e) {
        if (e.getCause() instanceof UserActiveException) {
          System.out.println("user was active...trying again.");
          HumanInteraction.sleep(1000);
          continue;
        } else {
          e.printStackTrace();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
      return;
    }
  }

}
