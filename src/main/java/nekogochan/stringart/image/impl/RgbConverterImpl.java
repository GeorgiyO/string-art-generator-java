package nekogochan.stringart.image.impl;

import nekogochan.stringart.fn.Generate;
import nekogochan.stringart.image.GrayscaleConverter;
import nekogochan.stringart.image.ImageConverter;
import nekogochan.stringart.image.RGB;
import nekogochan.stringart.image.RgbConverter;

import java.awt.image.BufferedImage;
import java.util.List;

public class RgbConverterImpl implements RgbConverter {

  private List<List<RGB>> data;

  public RgbConverterImpl(List<List<RGB>> data) {
    this.data = data;
  }

  public static RgbConverterImpl fromImage(BufferedImage image) {
    return new RgbConverterImpl(Generate.biList(0, image.getWidth(),
                                                0, image.getHeight(),
                                                (x, y) -> RGB.ofPlain(image.getRGB(x, y))));
  }

  @Override
  public RgbConverter map(MapOperator operator) {
    data = Generate.biList(0, data.size(),
                           0, data.get(0).size(),
                           (x, y) -> operator.apply(data.get(x).get(y), data, x, y));
    return this;
  }

  @Override
  public ImageConverter toImage() {
    var image = new BufferedImage(data.size(), data.get(0).size(), BufferedImage.TYPE_INT_RGB);
    for (int i = 0; i < data.size(); i++) {
      var row = data.get(i);
      for (int j = 0; j < row.size(); j++) {
        var cell = row.get(j);
        image.setRGB(i, j, cell.toPlain());
      }
    }
    return new ImageConverterImpl(image);
  }

  @Override
  public GrayscaleConverter toGrayscale() {
    return GrayscaleConverterImpl.fromRgb(data);
  }
}
