package scraper.ocr;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

public class SWTTest {

  public static void main(String[] args) {
    Display d = Display.getCurrent();
    Image image = new Image(d, 200, 200);
    GC g = new GC(image);
    g.setForeground(new Color(d, 0, 0, 0));
    g.setTextAntialias(SWT.OFF);
    g.setAdvanced(true);
    g.setFont(new Font(d, "Tahoma", 7, SWT.BOLD));
    g.drawString("Ft", 30, 30);

    ImageLoader loader = new ImageLoader();
    loader.data = new ImageData[] {image.getImageData()};
    loader.save("C:/shit/test.png", SWT.IMAGE_PNG);
  }

}
