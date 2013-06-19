package scraper.internal;

import java.util.Stack;

import org.apache.log4j.Logger;

public class Clock {

  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(Clock.class);

  private static final ThreadLocal<Stack<Clocker>> stack = new ThreadLocal<Stack<Clocker>>() {
    @Override
    protected java.util.Stack<Clocker> initialValue() {
      return new Stack<Clocker>();
    };
  };

  public static void in(String s) {
    stack.get().add(new Clocker(s));
  }

  public static void out() {
    Clocker pop = stack.get().pop();
    long t = System.nanoTime();
    double millis = (t - pop.time) / 1000000.0;
    logger.debug(pop.s + ": " + millis + " ms");
  }

  private static class Clocker {
    private final long time = System.nanoTime();
    private final String s;

    public Clocker(String s) {
      this.s = s;
    }
  }

}
