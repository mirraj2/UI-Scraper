package scraper.ocr;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
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
    Display display = Display.getCurrent();
    int h = (int) (font.getSize() * 1.5);
    int w = h * 2;
    Image im = new Image(display, w, h);
    GC g = new GC(im);
    g.setAdvanced(true);
    g.setTextAntialias(antialias ? SWT.ON : SWT.OFF);
    int style = font.isBold() ? SWT.BOLD : SWT.NORMAL;
    g.setFont(new org.eclipse.swt.graphics.Font(display, font.getName(), font
        .getSize(), style));

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

    Img img = Img.wrap(im);

    for (String c : glyphs) {

      g.setBackground(new org.eclipse.swt.graphics.Color(null, 255, 255, 255));
      g.fillRectangle(0, 0, w, h);
      g.setForeground(new org.eclipse.swt.graphics.Color(null, 0, 0, 0));
      g.drawString(c, 0, 0);
      img.reload();

      // if (c.equals("F")) {
      // ImageLoader loader = new ImageLoader();
      // loader.data = new ImageData[] {im.getImageData()};
      // loader.save("C:/shit/shit_" + tt++ + ".png", SWT.IMAGE_PNG);
      // }

      GlyphShape shape = OCR.generateCharacterShape(c, img, 0, Color.black, antialias, true);
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
      logger.debug(shape.getInfoString());
      logger.debug(bestMatch.getKey().getInfoString());
      return bestMatch.getValue();
    }
    logger
        .warn("didn't find a match, best score was: " + bestScore + " \n" + shape.getInfoString());
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
