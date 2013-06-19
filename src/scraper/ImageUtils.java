package scraper;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class ImageUtils {

  public static BufferedImage load(String name) {
    InputStream is = ImageUtils.class.getResourceAsStream(name);
    try {
      return ImageIO.read(is);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
