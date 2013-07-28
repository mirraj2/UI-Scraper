package scraper.ocr;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Panel;
import java.awt.RenderingHints;
import java.awt.SystemColor;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class FontTester extends Panel {

  private final Font font = new Font("Tahoma", Font.BOLD, 10);
  private final int line = 25;

  FontTester() {
    setBackground(SystemColor.control);
  }

  public void paint(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;

    int py = 0;

    py = paintText(g2d, py, null, false);
    py = paintText(g2d, py, null, true);
    py += line;

    py = paintText(g2d, py, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF, false);
    py = paintText(g2d, py, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT, false);
    py = paintText(g2d, py, RenderingHints.VALUE_TEXT_ANTIALIAS_ON, false);
    py = paintText(g2d, py, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP, false);
    py = paintText(g2d, py, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB, false);
    py += line;

    py = paintText(g2d, py, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF, true);
    py = paintText(g2d, py, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT, true);
    py = paintText(g2d, py, RenderingHints.VALUE_TEXT_ANTIALIAS_ON, true);
    py = paintText(g2d, py, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP, true);
    py = paintText(g2d, py, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB, true);
    py += line;
  }

  private int paintText(Graphics2D g2d, int py, Object val, boolean aa) {
    Graphics2D dgc = g2d;
    char[] txt = "Ft".toCharArray();
    Image img = null;

    GraphicsConfiguration cfg = getGraphicsConfiguration();
    img = cfg.createCompatibleImage(getWidth(), line);
    dgc = (Graphics2D) img.getGraphics();
    dgc.setColor(getBackground());
    dgc.fillRect(0, 0, getWidth(), line);
    dgc.setColor(g2d.getColor());

    if (aa) {
      dgc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    } else {
      dgc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }
    if (val != null) {
      dgc.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, val);
    }
    dgc.setFont(font);

    dgc.drawChars(txt, 0, txt.length, 10, line - 5);
    g2d.drawImage(img, 0, py, null);

    dgc.dispose();
    img.flush();

    return (py + line);
  }

  public static void main(String[] args) {
    Frame wnd = new Frame("Font Tester");

    wnd.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    wnd.add(new FontTester());
    wnd.setSize(new Dimension(1000, 600));
    wnd.setVisible(true);
  }

}
