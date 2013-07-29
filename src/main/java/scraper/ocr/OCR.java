package scraper.ocr;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scraper.HumanInteraction;
import scraper.ScreenScraper;


public class OCR {

  @SuppressWarnings("unused")
  private static final Logger logger = LoggerFactory.getLogger(OCR.class);

  public static String parse(BufferedImage image, Font font, boolean antialias) {
    if (image == null) {
      throw new IllegalArgumentException("image can't be null");
    }
    if (font == null) {
      throw new IllegalArgumentException("font can't be null");
    }

    List<OCRFont> fonts = Lists.newArrayList();
    for (int i = 0; i < 1; i++) {
      fonts.add(OCRFont.create(font.deriveFont(font.getSize2D() - i), antialias));
    }

    Color foregroundColor = getForegroundColor(image);

    StringBuffer ret = new StringBuffer();

    try {
      int lastX = 0;
      for (int i = 0; i < image.getWidth(); i++) {
        GlyphShape shape =
            generateCharacterShape("unknown", Img.wrap(image), i, foregroundColor, antialias,
                false);
        if (shape != null) {

          if (ret.length() > 0 && shape.getMinX() - lastX > 3) {
            ret.append(' ');
          }
          lastX = shape.getMaxX();

          String letter = null;
          for (OCRFont f : fonts) {
            letter = f.getString(shape);
            if (letter != null) {
              break;
            }
          }

          if (letter != null) {
            if (letter.equals(",") && shape.getMinY() < 5) {
              letter = "'";
            }
            ret.append(letter);
          } else {
            ret.append("?");
          }

          i = shape.getMaxX() + 1;
        }
      }
    } catch (Exception e) {
      HumanInteraction.dumpImage(image);
      throw Throwables.propagate(e);
    }

    String s = ret.toString();

    if (s.contains("?")) {
      HumanInteraction.dumpImage(image);
    }

    return s;
  }

  public static GlyphShape generateCharacterShape(String s, Img image, int startingX,
      Color foreground, boolean antialias, boolean walkRight) {
    ForegroundComparator fgComparator = new ForegroundComparator(foreground);
    for (int i = startingX; i < image.getWidth(); i++) {
      for (int j = 0; j < image.getHeight(); j++) {
        int rgb = image.getRGB(i, j);
        if (fgComparator.isForeground(rgb)) {
          GlyphShape shape = findShape(s, image, i, j, fgComparator);
          return shape;
        }
      }
      if (!walkRight) {
        break;
      }
    }
    return null;
  }

  private static GlyphShape findShape(String s, Img bi, int startX, int startY,
      ForegroundComparator fgComparator) {
    Queue<Point> queue = new LinkedList<Point>();
    Point firstPoint = new Point(startX, startY);
    queue.add(firstPoint);
    HashSet<Point> pointsSeen = new HashSet<Point>();
    pointsSeen.add(firstPoint);
    GlyphShape ret = new GlyphShape(s);
    while (!queue.isEmpty()) {
      Point p = queue.poll();
      ret.addPixel(p);
      int minX = Math.max(p.x - 1, 0);
      int maxX = Math.min(p.x + 1, bi.getWidth() - 1);
      int minY = Math.max(p.y - 1, 0);
      int maxY = Math.min(p.y + 1, bi.getHeight() - 1);
      for (int i = minX; i <= maxX; i++) {
        for (int j = minY; j <= maxY; j++) {
          Point next = new Point(i, j);
          if (!pointsSeen.contains(next)) {
            pointsSeen.add(next);
            if (fgComparator.isForeground(bi.getRGB(i, j))) {
              queue.add(next);
            }
          }
        }
      }
    }

    ret.solidify();

    return ret;
  }

  private static Color getForegroundColor(BufferedImage bi) {
    Map<Integer, AtomicInteger> counts = Maps.newHashMap();

    int backgroundRGB = bi.getRGB(0, 0);

    for (int i = 0; i < bi.getWidth(); i++) {
      for (int j = 0; j < bi.getHeight(); j++) {
        int rgb = bi.getRGB(i, j);

        if (ScreenScraper.isAboutSameColor(rgb, backgroundRGB)) {
          continue;
        }

        AtomicInteger a = counts.get(rgb);
        if (a == null) {
          counts.put(rgb, a = new AtomicInteger());
        }
        a.incrementAndGet();
      }
    }

    List<Entry<Integer, AtomicInteger>> entries = Lists.newArrayList(counts.entrySet());
    Collections.sort(entries, new Comparator<Entry<Integer, AtomicInteger>>() {
      @Override
      public int compare(Entry<Integer, AtomicInteger> o1, Entry<Integer, AtomicInteger> o2) {
        return o2.getValue().get() - o1.getValue().get();
      }
    });
    
    return new Color(entries.get(0).getKey());
  }

  private static class ForegroundComparator {
    private final int foregroundRGB;

    public ForegroundComparator(Color foreground) {
      this.foregroundRGB = foreground.getRGB();
    }

    public boolean isForeground(int rgb) {
      return ScreenScraper.isAboutSameColor(rgb, foregroundRGB);
    }
  }

  public static void main(String[] args) throws Exception {
    BufferedImage bi = ImageIO.read(new File("C:/dump/0.png"));
    System.out.println(OCR.parse(bi, new Font("Tahoma", Font.BOLD, 7), false));
    System.out.println("done");
  }

}
