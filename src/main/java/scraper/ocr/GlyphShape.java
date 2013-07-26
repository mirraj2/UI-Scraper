package scraper.ocr;

import java.awt.Point;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.Sets;

public class GlyphShape {

  private int minX = Integer.MAX_VALUE / 2;
  private int maxX = -1;
  private int minY = Integer.MAX_VALUE / 2;
  private int maxY = -1;

  private Integer hashCode = null;
  private final String s;

  private Set<Point> pixels = new TreeSet<Point>(new Comparator<Point>() {
    @Override
    public int compare(Point o1, Point o2) {
      if (o1.x != o2.x) {
        return o2.x - o1.x;
      }
      return o2.y - o1.y;
    }
  });

  public GlyphShape(String s) {
    this.s = s;
  }

  /**
   * Called after all points have been added to this shape.
   */
  public void solidify() {
    for (Point p : pixels) {
      p.x -= minX;
      p.y -= minY;
    }
  }

  public void addPixel(Point pixel) {
    pixels.add(pixel);
    hashCode = null;

    int x = pixel.x;
    int y = pixel.y;

    if (x > maxX) {
      maxX = x;
    }
    if (x < minX) {
      minX = x;
    }
    if (y > maxY) {
      maxY = y;
    }
    if (y < minY) {
      minY = y;
    }
  }

  public int getMaxX() {
    return maxX;
  }

  public int getMinX() {
    return minX;
  }

  public int getMinY() {
    return minY;
  }

  @Override
  public String toString() {
    return "GlyphShape: " + s;

  }

  public String getInfoString() {
    StringBuffer ret = new StringBuffer();
    ret.append("\n");
    ret.append("hash = " + hashCode() + "\n");
    ret.append("w = " + (maxX - minX + 1) + "\n");
    ret.append("h = " + (maxY - minY + 1) + "\n");
    for (int j = 0; j <= (maxY - minY); j++) {
      for (int i = 0; i <= (maxX - minX); i++) {
        if (pixels.contains(new Point(i, j))) {
          ret.append("#");
        } else {
          ret.append(".");
        }
      }
      ret.append("\n");
    }
    return ret.toString();
  }

  private Integer genHashCode() {
    final int prime = 31;
    int result = 1;
    int w = maxX - minX;
    int h = maxY - minY;
    for (Point p : pixels) {
      result = prime * result + p.x;
      result = prime * result + p.y;
    }
    result = prime * result + w;
    result = prime * result + h;
    return result;
  }

  @Override
  public int hashCode() {
    if (hashCode == null) {
      hashCode = genHashCode();
    }
    return hashCode;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof GlyphShape)) {
      return false;
    }
    return obj.hashCode() == this.hashCode();
  }

  public String getS() {
    return s;
  }

  public double getLikenessScore(GlyphShape s) {
    double score = 0;

    int w = s.maxX - s.minX;
    int myW = maxX - minX;
    if (w != myW) {
      return score;
    }

    int h = s.maxY - s.minY;
    int myH = maxY - minY;
    if (h != myH) {
      return score;
    }

    Set<Point> diff = Sets.symmetricDifference(this.pixels, s.pixels);

    return 1 - 1.0 * diff.size() / (this.pixels.size() + s.pixels.size());
  }

}
