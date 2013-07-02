package scraper;

import java.awt.Rectangle;

import com.google.common.base.Objects;

public class TargetWindow {

  private final long id;
  private final String title;

  public TargetWindow(long id, String title) {
    this.id = id;
    this.title = title;
  }

  public long getID() {
    return id;
  }

  public Rectangle getBounds() {
    return WindowsAPI.getBounds(this);
  }

  public String getTitle() {
    return title;
  }

  @Override
  public String toString() {
    return id + " :: " + title;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    TargetWindow other = (TargetWindow) obj;
    if (id != other.id) return false;
    return true;
  }



}
