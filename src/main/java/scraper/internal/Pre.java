package scraper.internal;

import java.util.Collection;


/**
 * AMP's preconditions class!
 * 
 * @author Jason
 * 
 */
public class Pre {

  public static void checkNonNull(Object o, String paramName) {
    if (o == null) {
      throw new NullArgumentException(paramName);
    }
  }

  public static void checkCollectionNonNull(Collection<?> c, String paramName) {
    if (c == null) {
      throw new NullArgumentException(paramName);
    }

    if (c.contains(null)) {
      throw new IllegalArgumentException(paramName + " contains 'null'");
    }
  }

}
