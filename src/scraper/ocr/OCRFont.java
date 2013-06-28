package scraper.ocr;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

public class OCRFont {

  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(OCRFont.class);

  private HashMap<GlyphShape, String> shapeToStringMap = new HashMap<GlyphShape, String>();

  private static final HashMap<Font, OCRFont> fontMap = new HashMap<Font, OCRFont>();
  private static final HashMap<Font, OCRFont> antialiasFontMap = new HashMap<Font, OCRFont>();

  public static OCRFont create(Font font, boolean antialias) {
    OCRFont ret = antialias ? antialiasFontMap.get(font) : fontMap.get(font);
    if (ret == null) {
      ret = new OCRFont(font, antialias);
      if (antialias) {
        antialiasFontMap.put(font, ret);
      } else {
        fontMap.put(font, ret);
      }
    }
    return ret;
  }

  private OCRFont(Font font, boolean antialias) {
    BufferedImage bi =
        new BufferedImage((int) (font.getSize() * 1.5), (int) (font.getSize() * 1.5),
            BufferedImage.TYPE_INT_ARGB);
    Graphics g = bi.getGraphics();
    Graphics2D g2d = (Graphics2D) g;
    if (antialias) {
      g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
          RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    } else {
      g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
          RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    }
    g.setFont(font);

    final List<Character> supportedCharacters = Lists.newArrayList();

    for (char c = 'a'; c <= 'z'; c++) {
      supportedCharacters.add(c);
    }
    for (char c = 'A'; c <= 'Z'; c++) {
      supportedCharacters.add(c);
    }
    for (char c = '0'; c <= '9'; c++) {
      supportedCharacters.add(c);
    }
    supportedCharacters.add('(');
    supportedCharacters.add(')');
    supportedCharacters.add('/');
    supportedCharacters.add('$');

    List<String> glyphs = Lists.newArrayList();

    for (Character c : supportedCharacters) {
      glyphs.add("" + c);
    }

    for (Character c : supportedCharacters) {
      for (Character cc : supportedCharacters) {
        glyphs.add("" + c + cc);
      }
    }

    for (String c : glyphs) {
      g.setColor(Color.white);
      g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
      g.setColor(Color.black);
      g.drawString(c, 0, g.getFontMetrics().getAscent());

      GlyphShape shape = OCR.generateCharacterShape(bi, 0, Color.white, antialias);
      if (shape == null) {
        throw new RuntimeException("Could not generate CharacterShape for character= " + c);
      }
      if (shapeToStringMap.get(shape) != null) {
        if (c.length() > 1) {
          continue;
        }
        if ("I".equals(c)) { // because I and l look exactly the same in arial
          continue;
        }
        // else if ("P".equals(c)) { // because P and p look exactly the same in Tahoma
        // continue;
        // }
        else {
          System.out.println("Trying to add:" + shape.getInfoString());
          for (GlyphShape key : shapeToStringMap.keySet()) {
            if (key.hashCode() == shape.hashCode()) {
              System.out.println("But already have: " + key.getInfoString());
            }
          }
          throw new RuntimeException("HashCode() method for CharacterShape has a duplicate.");
        }
      }
      shapeToStringMap.put(shape, c + "");
    }

  }

  // private String getStringByBestMatch(GlyphShape shape) {
  // double bestScore = -1;
  // Map.Entry<GlyphShape, String> bestMatch = null;
  // for (Map.Entry<GlyphShape, String> entry : shapeToStringMap.entrySet()) {
  // GlyphShape s = entry.getKey();
  // double score = shape.getLikenessScore(s);
  // if (score > bestScore) {
  // bestScore = score;
  // bestMatch = entry;
  // }
  // }
  // // System.out.println("attempting to match: "+shape.getInfoString());
  // // System.out.println("best score = "+bestScore);
  // // System.out.println("gotten by: "+bestMatch.getKey().getInfoString());
  // return bestMatch.getValue();
  // }

  public String getString(GlyphShape shape) {
    return shapeToStringMap.get(shape);
  }

}
