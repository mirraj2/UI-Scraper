package scraper;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class ScreenScraperDebugger {

  public static void debug(Rectangle windowRect, Rectangle[] rectangles, String[] strings) {
    BufferedImage bi = HumanInteraction.caputreScreen(windowRect);
    Graphics g = bi.getGraphics();
    for (int i = 0; i < rectangles.length; i++) {
      Rectangle rect = new Rectangle(rectangles[i]);
      String s = null;
      if (i < strings.length) {
        s = strings[i];
      }
      rect.x -= windowRect.x;
      rect.y -= windowRect.y;
      g.setColor(new Color(0, 0, 0, 170));
      g.fillRect(rect.x, rect.y, rect.width, rect.height);
      if (s != null) {
        g.setColor(Color.white);
        g.drawString(s, rect.x, rect.y + 20);
      }
    }
    g.dispose();
    HumanInteraction.dumpImage(bi);
  }

}
