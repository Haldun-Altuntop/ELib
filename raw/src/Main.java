import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.spi.BreakIteratorProvider;

public class Main extends JPanel {
    private BufferedImage image;
    private static BufferedImage[] frames = new BufferedImage[1093];

    private static int i = 0;

    public Main(BufferedImage img) {
        this.image = img;
        setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
    }

    public Main() {

    }

    @Override
    protected void paintComponent(Graphics gr) {
        super.paintComponent(gr);
        if (image != null)
            gr.drawImage(image, 0, 0, null);

        String fileName = "/home/haldun-altuntop/USB Drives/Altuntop Family-1/Haldun/raw-frames/oyun-fikir-videosu_raw/oyun-fikir-videosu.raw";

        try {
            RandomAccessFile raf = new RandomAccessFile(fileName, "r");
            raf.seek(i);

            int x = raf.read(); i++;
            int y = raf.read(); i++;
            int r = raf.read(); i++;
            int g = raf.read(); i++;
            int b = raf.read(); i++;

            gr.drawRect(x, y, 1, 1);
            gr.setColor(new Color(r,g,b));

            raf.close();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }

        System.out.println(i);

    }

    public static BufferedImage readRGBFrame(String filename, int width, int height, int frameIndex) throws IOException {
        int frameSize = width * height * 3;
        RandomAccessFile file = new RandomAccessFile(filename, "r");

        // Gidilecek yer = frameIndex * frameSize
        file.seek((long) frameIndex * frameSize);

        byte[] buffer = new byte[frameSize];
        file.readFully(buffer);
        file.close();

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int idx = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = buffer[idx++] & 0xFF;
                int g = buffer[idx++] & 0xFF;
                int b = buffer[idx++] & 0xFF;
                int rgb = (r << 16) | (g << 8) | b;
                img.setRGB(x, y, rgb);
            }
        }
        return img;
    }

    public static void main(String[] args) throws Exception {
        int width = 720;
        int height = 1280;
        int frameIndex = 1092;
        String fileName = "/home/haldun-altuntop/USB Drives/Altuntop Family-1/Haldun/raw-frames/oyun-fikir-videosu_raw/oyun-fikir-videosu.raw";

        JFrame frame = new JFrame("Raw RGB Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(720, 1280);
        //frame.setContentPane(new Main(readRGBFrame(fileName, width, height, frameIndex)));

        JPanel panel = new Main();
        frame.add(panel);




        for (int i = 0; i < width * height * 3; i++) {


            //System.out.println(width * height * 3 + " / " + i);

            frame.pack();

        }

        frame.setLayout(null);
        frame.setVisible(true);

    }

    /*
    for (int i = 0; i < 30; i++) {
            //frames[i] = readRGBFrame(fileName, width, height, i);
        }

        new Thread(() -> {

            for (int i2 = 0; i2 * 2 < frames.length; i2++) {
                try {
                    frames[i2] = readRGBFrame(fileName, width, height, i2);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        }).start();

        new Thread(() -> {

            for (int i1 = 0; (i1 * 2) + 1 < frames.length; i1++) {
                try {
                    frames[i1] = readRGBFrame(fileName, width, height, i1);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        }).start();

        new Thread(() -> {

            for (int i = 0; i < 1093; i ++) {

                if (i > 0) {
                    frames[i - 1] = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
                }

                if (i > 20) {
                    frame.setContentPane(new Main(frames[i]));
                    frame.pack();
                }

                try {
                    Thread.sleep(33);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();


        /*

         */

}
