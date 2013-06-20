package scraper.ocr;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import javax.imageio.ImageIO;

import scraper.ScreenScraper;


public class OCR {

  public static String parse(BufferedImage image, Font font, boolean antialias) {
    if (image == null) {
      throw new IllegalArgumentException("image can't be null");
    }
    if (font == null) {
      throw new IllegalArgumentException("font can't be null");
    }

    OCRFont oFont = OCRFont.create(font, antialias);

    Color backgroundColor = getBackgroundColor(image);

    StringBuffer ret = new StringBuffer();

    try {
      for (int i = 0; i < image.getWidth(); i++) {
        GlyphShape shape = generateCharacterShape(image, i, backgroundColor.getRGB(), antialias);
        if (shape != null) {
          String letter = oFont.getString(shape);
          if (letter != null) {
            ret.append(letter);
          }
          i = shape.getMaxX() + 1;
        }
      }
    } catch (Exception e) {
      try {
        ImageIO.write(image, "png", new File("C:\\dump\\" + System.currentTimeMillis() + ".png"));
      } catch (IOException e1) {
        e1.printStackTrace();
      }
      throw new RuntimeException(e);
    }

    return ret.toString();
  }

  public static GlyphShape generateCharacterShape(BufferedImage image, int startingX,
      int backgroundRGB, boolean antialias) {
    BackgroundComparator bgComparator = new BackgroundComparator(backgroundRGB, antialias);
    int barHeight = (int) (image.getHeight() * .5);
    for (int i = startingX; i < image.getWidth(); i++) {
      int rgb = image.getRGB(i, barHeight);
      if (!bgComparator.isBackground(rgb)) {
        GlyphShape shape = findShape(image, i, barHeight, bgComparator);
        return shape;
      }
    }
    return null;
  }

  private static GlyphShape findShape(BufferedImage bi, int startX, int startY,
      BackgroundComparator bgComparator) {
    Queue<Point> queue = new LinkedList<Point>();
    Point firstPoint = new Point(startX, startY);
    queue.add(firstPoint);
    HashSet<Point> pointsSeen = new HashSet<Point>();
    pointsSeen.add(firstPoint);
    GlyphShape ret = new GlyphShape();
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
            if (!bgComparator.isBackground(bi.getRGB(i, j))) {
              queue.add(next);
            }
          }
        }
      }
    }

    ret.solidify();

    return ret;
  }

  private static Color getBackgroundColor(BufferedImage bi) {
    // we will draw a line along the top to get the background color since
    // most letters don't make it all the way up.
    HashMap<Integer, IncrementingInteger> colorFrequencies =
        new HashMap<Integer, IncrementingInteger>();
    int j = 1;
    for (int i = 0; i < bi.getWidth(); i++) {
      int rgb = bi.getRGB(i, j);
      IncrementingInteger ii = colorFrequencies.get(rgb);
      if (ii == null) {
        colorFrequencies.put(rgb, new IncrementingInteger());
      } else {
        ii.inc();
      }
    }
    int bestRGB = -1;
    int bestCount = -1;
    for (Map.Entry<Integer, IncrementingInteger> entry : colorFrequencies.entrySet()) {
      if (entry.getValue().value >= bestCount) {
        bestCount = entry.getValue().value;
        bestRGB = entry.getKey();
      }
    }
    return new Color(bestRGB);
  }

  private static class IncrementingInteger {
    int value = 1;

    void inc() {
      value++;
    }
  }

  private static class BackgroundComparator {

    private final int backgroundRGB;
    private final int threshold;

    public BackgroundComparator(int backgroundRGB, boolean antialias) {
      this.backgroundRGB = backgroundRGB;
      threshold = antialias ? 20 : 0;
    }

    public boolean isBackground(int rgb) {
      return ScreenScraper.isAboutSameColor(rgb, backgroundRGB, threshold);
    }

  }

}
