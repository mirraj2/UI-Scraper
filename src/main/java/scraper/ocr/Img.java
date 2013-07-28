package scraper.ocr;

import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

public abstract class Img {

  public abstract int getWidth();

  public abstract int getHeight();

  public abstract int getRGB(int x, int y);

  public void reload() {}

  public static Img wrap(final BufferedImage im) {
    return new Img() {
      @Override
      public int getWidth() {
        return im.getWidth();
      }

      @Override
      public int getHeight() {
        return im.getHeight();
      }

      @Override
      public int getRGB(int x, int y) {
        return im.getRGB(x, y);
      }
    };
  }

  public static Img wrap(final Image im) {
    final AtomicReference<ImageData> ref = new AtomicReference<ImageData>(im.getImageData());
    return new Img() {
      @Override
      public int getWidth() {
        return ref.get().width;
      }

      @Override
      public int getHeight() {
        return ref.get().height;
      }

      @Override
      public int getRGB(int x, int y) {
        return ref.get().getPixel(x, y);
      }

      @Override
      public void reload() {
        ref.set(im.getImageData());
      }
    };
  }

}
