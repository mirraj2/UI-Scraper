package scraper.ocr;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OCRFont {

  private HashMap<CharacterShape, Character> shapeToCharacterMap =
      new HashMap<CharacterShape, Character>();

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
        new BufferedImage((int) (font.getSize() * 1.2), (int) (font.getSize() * 1.2),
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

    final List<Character> supportedCharacters = new ArrayList<Character>(128);

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

    for (Character c : supportedCharacters) {
      g.setColor(Color.white);
      g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
      g.setColor(Color.black);
      g.drawString(c.charValue() + "", 0, g.getFontMetrics().getAscent());

      /*
       * try {
       * ImageIO.write(bi, "png", new File("C:\\DATA_DATA\\dump\\" + c.charValue() + ".png"));
       * } catch (IOException e) {
       * e.printStackTrace();
       * }
       */

      CharacterShape shape = OCR.generateCharacterShape(bi, 0, Color.white.getRGB(), antialias);
      if (shape == null) {
        throw new RuntimeException("Could not generate CharacterShape for character= " + c);
      }
      // System.out.println(c + " --> " + shape.hashCode() + " --> "
      // + shape.toString());
      // System.out.println(shape.getInfoString());
      if (shapeToCharacterMap.get(shape) != null) {
        if (c == 'I') { // because I and l look exactly the same in
          // arial
          continue;
        } else {
          System.out.println("Trying to add:" + shape.getInfoString());
          for (CharacterShape key : shapeToCharacterMap.keySet()) {
            if (key.hashCode() == shape.hashCode()) {
              System.out.println("But already have: " + key.getInfoString());
            }
          }
          throw new RuntimeException("HashCode() method for CharacterShape has a duplicate.");
        }
      }
      shapeToCharacterMap.put(shape, new Character(c));
    }

  }

  private Character getCharacterByBestMatch(CharacterShape shape) {
    double bestScore = -1;
    Map.Entry<CharacterShape, Character> bestMatch = null;
    for (Map.Entry<CharacterShape, Character> entry : shapeToCharacterMap.entrySet()) {
      CharacterShape s = entry.getKey();
      double score = shape.getLikenessScore(s);
      if (score > bestScore) {
        bestScore = score;
        bestMatch = entry;
      }
    }
    // System.out.println("attempting to match: "+shape.getInfoString());
    // System.out.println("best score = "+bestScore);
    // System.out.println("gotten by: "+bestMatch.getKey().getInfoString());
    return bestMatch.getValue();
  }

  public char getCharacter(CharacterShape shape) {
    Character ret = shapeToCharacterMap.get(shape);
    if (ret == null) {
      // try to find the character smartly
      ret = getCharacterByBestMatch(shape);
    }
    if (ret == null) {
      throw new IllegalStateException("Could not find character for shape: " + shape);
    }
    return ret.charValue();
  }

}
