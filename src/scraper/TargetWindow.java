package scraper;

import java.awt.Rectangle;

public class TargetWindow {

  private final int id;
  private final Rectangle bounds;
  private final String title;

  public TargetWindow(int id, String title, Rectangle bounds) {
    this.id = id;
    this.title = title;
    this.bounds = bounds;
  }

  public int getID() {
    return id;
  }

  public Rectangle getBounds() {
    return bounds;
  }

  public String getTitle() {
    return title;
  }

  @Override
  public String toString() {
    return title + " " + bounds;
  }

}
