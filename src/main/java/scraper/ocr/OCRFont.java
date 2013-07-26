package scraper.ocr;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OCRFont {

  @SuppressWarnings("unused")
  private static final Logger logger = LoggerFactory.getLogger(OCRFont.class);

  private HashMap<GlyphShape, String> shapeToStringMap = Maps.newLinkedHashMap();

  private static final HashMap<Font, OCRFont> fontMap = Maps.newLinkedHashMap();
  private static final HashMap<Font, OCRFont> antialiasFontMap = Maps.newLinkedHashMap();

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
    Graphics gg = bi.getGraphics();
    Graphics2D g = (Graphics2D) gg;
    if (antialias) {
      g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
          RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    } else {
      g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
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
    final String s = "()/$,.'-";
    for (int i = 0; i < s.length(); i++) {
      supportedCharacters.add(s.charAt(i));
    }

    List<String> glyphs = Lists.newArrayList();

    for (Character c : supportedCharacters) {
      glyphs.add("tt" + c);
      glyphs.add("ft" + c);
      glyphs.add("ff" + c);
    }

    for (Character c : supportedCharacters) {
      for (Character cc : supportedCharacters) {
        glyphs.add("" + c + cc);
      }
    }

    for (Character c : supportedCharacters) {
      glyphs.add("" + c);
    }

    for (String c : glyphs) {

      g.setColor(Color.white);
      g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
      g.setColor(Color.black);
      g.drawString(c, 0, g.getFontMetrics().getAscent());

      GlyphShape shape = OCR.generateCharacterShape(c, bi, 0, Color.black, antialias, true);
      if (shape == null) {
        throw new RuntimeException("Could not generate CharacterShape for character= " + c);
      }

      shapeToStringMap.put(shape, c + "");
    }

  }

  private String getStringByBestMatch(GlyphShape shape) {
    double bestScore = -1;
    Map.Entry<GlyphShape, String> bestMatch = null;
    for (Map.Entry<GlyphShape, String> entry : shapeToStringMap.entrySet()) {
      GlyphShape s = entry.getKey();
      double score = shape.getLikenessScore(s);
      if (score > bestScore) {
        bestScore = score;
        bestMatch = entry;
      }
    }
    if(bestScore > .9){
      logger.debug("returning closest match: " + bestMatch.getValue());
      return bestMatch.getValue();
    }
    logger.warn("didn't find a match. best score was: " + bestScore);
    return null;
  }

  public String getString(GlyphShape shape) {
    String ret = shapeToStringMap.get(shape);
    if (ret != null) {
      return ret;
    }
    return getStringByBestMatch(shape);
  }

}
