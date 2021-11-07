package nekogochan.stringart.usagelocal;

import nekogochan.stringart.StringArt;
import nekogochan.stringart.binds.BindsAllFree;
import nekogochan.stringart.fn.Unchecked;
import nekogochan.stringart.image.impl.ImageConverterImpl;
import nekogochan.stringart.nail.Nail;
import nekogochan.stringart.nail.NailImpl;
import nekogochan.stringart.pair.Pair;
import nekogochan.stringart.point.RectPoint;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Supplier;

public class Main {

  static String SOURCE = "Gcpr8ZTEnVU.jpg";

  static Path DESKTOP = Path.of("C:/Users/sgs08/Desktop");
  static Path SOURCE_PATH = DESKTOP.resolve(SOURCE);
  static Path SAVE_DIR = DESKTOP.resolve("result");

  static int WIDTH = 500;
  static int HEIGHT = 500;
  static double RADIUS = 5.0;
  static double REMOVE_VALUE = 0.1;
  static double DRAW_MP = 7.0;
  static int ITERATIONS = 10000;
  static int SHOW_PERIOD = 500;
  static int SAVE_PERIOD = 1000;
  static int NAILS_COUNT = 240;

  static boolean RUN_TEST = true;

  static int DRAW_WIDTH = (int) (WIDTH * DRAW_MP);
  static int DRAW_HEIGHT = (int) (HEIGHT * DRAW_MP);

  public static void main(String[] args) throws InterruptedException {
//    var n1 = new NailImpl(new RectPoint(700.0, 300.0), 150.0);
//    var n2 = new NailImpl(new RectPoint(300.0, 700.0), 50.0);
//    var pair = n2.lookTo(n1, true, false);
//    DRAW_MP = 1.0;
//
//    var img = Unchecked.call(() -> ImageIO.read(SOURCE_PATH.toFile()));
//    var gx = initGx(img, 10.0f);
//
//    gx.setColor(Color.RED);
//    drawNail(gx, n1);
//    drawNail(gx, n2);
//
//    gx.setColor(Color.GREEN);
//    drawPath(gx, pair);
//
//    pair = n1.lookTo(n2, true, true);
//
//    gx.setColor(Color.BLUE);
//    drawPath(gx, pair);
//    showImage(ImageUtil.copy(img));

    if (RUN_TEST) {
      runTest();
    } else {
      runMain();
    }
  }

  static void runMain() {
    var img = Unchecked.call(() -> ImageIO.read(SOURCE_PATH.toFile()));
    WIDTH = img.getWidth();
    HEIGHT = img.getHeight();
    DRAW_WIDTH = (int) (WIDTH * DRAW_MP);
    DRAW_HEIGHT = (int) (HEIGHT * DRAW_MP);
    var nails = new BindsRect(NAILS_COUNT, WIDTH, HEIGHT, RADIUS).nails();
    var data = getData(img, WIDTH, HEIGHT);
    var stringArt = new StringArt(data, nails, REMOVE_VALUE);

    var image = new BufferedImage(DRAW_WIDTH, DRAW_HEIGHT, BufferedImage.TYPE_INT_RGB);
    var gx = initGx(image, 0.0f);

    gx.setColor(Color.GRAY);
    nails.forEach(n -> drawNail(gx, n));
    gx.setColor(Color.BLACK);

    Supplier<BufferedImage> imageForShow = () -> new ImageConverterImpl(image).resize(WIDTH, HEIGHT).get();

    var time = System.currentTimeMillis();
    for (int i = 1; i <= ITERATIONS; i++) {
      var pair = stringArt.next().pair();
      drawPath(gx, pair);
      if (i % SHOW_PERIOD == 0) {
        System.out.println(i);
        showResult(imageForShow.get());
      }
      if (i % SAVE_PERIOD == 0) {
        saveImage(imageForShow.get(), SAVE_DIR.resolve(i + "_" + SOURCE));
      }
    }
    System.out.println("time: " + (System.currentTimeMillis() - time));

    showResult(imageForShow.get());
    saveImage(image, SAVE_DIR.resolve(SOURCE));
  }

  static void runTest() throws InterruptedException {
    var img = Unchecked.call(() -> ImageIO.read(SOURCE_PATH.toFile()));
    DRAW_MP = 1.0;

    var data = getData(img, 1000, 1000);

//    var nails = new BindsRect(20, 1000, 1000, 50).nails();
//    var nails = new BindsCircle(10, 500, 100).nails();

    var nails = new BindsAllFree(
      Map.of(100, 100,
             200, 200,
             400, 200,
             567, 365,
             886, 475,
             734, 865,
             123, 748,
             178, 344)
         .entrySet()
         .stream()
         .map(e -> new NailImpl(new RectPoint(e.getKey(), e.getValue()), 50))
         .toList()).nails();

    var stringArt = new StringArt(data, nails, 1.0);

    var image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
    var gx = initGx(image, 3.0f);

    gx.setColor(Color.RED);
    nails.forEach(n -> drawNail(gx, n));
    gx.setColor(Color.BLACK);

    showResult(image);

    for (int i = 0; i < 10000; i++) {
//      Thread.sleep(500);
      var path = stringArt.next().pair();
      gx.drawLine(
        path.from().x(),
        path.from().y(),
        path.to().x(),
        path.to().y()
      );
      if (i % 1000 == 0)
        showResult(image);
    }
  }

  static double[][] getData(BufferedImage img, int width, int height) {
    return new ImageConverterImpl(img)
      .resizeCropping(width, height)
      .proceed(Main::showImage)
      .toRgb()
      .toGrayscale()
      .inverse()
      .toArray();
  }

  static Frame showImage(BufferedImage image) {
    var frame = new JFrame();
    frame.setLayout(new FlowLayout());
    frame.setSize(image.getWidth(), image.getHeight() + 44);
    var label = new JLabel();
    label.setIcon(new ImageIcon(image));
    frame.add(label);
    frame.setVisible(true);
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    return frame;
  }

  static Graphics2D initGx(BufferedImage image, float strokeWidth) {
    var gx = image.createGraphics();
    gx.fillRect(0, 0, image.getWidth(), image.getHeight());
    gx.setColor(Color.BLACK);
    gx.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    return gx;
  }

  static void drawNail(Graphics2D gx, Nail nail) {
    var c = nail.center();
    var x = (int) (c.x() * DRAW_MP);
    var y = (int) (c.y() * DRAW_MP);
    var r = (int) (nail.radius() * 2 * DRAW_MP);
    gx.fillArc(
      x - (r / 2), y - (r / 2),
      r, r,
      0, 360
    );
  }

  static void drawPath(Graphics2D gx, Pair pair) {
    gx.drawLine(
      (int) DRAW_MP * pair.from().x(),
      (int) DRAW_MP * pair.from().y(),
      (int) DRAW_MP * pair.to().x(),
      (int) DRAW_MP * pair.to().y()
    );
  }

  static Frame prev;

  static void showResult(BufferedImage img) {
    var next = showImage(img);
    if (prev != null) prev.dispose();
    prev = next;
  }

  static void saveImage(BufferedImage image, Path path) {
    System.out.println(path);
    Unchecked.call(() -> ImageIO.write(image, "png", path.toFile()));
  }
}