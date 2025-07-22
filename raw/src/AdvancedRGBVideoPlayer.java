import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class AdvancedRGBVideoPlayer extends JPanel {

    private BufferedImage image;
    private DataInputStream in;
    private int width, height, fps, frameCount, keyframeInterval;
    private byte[] frameBuffer;

    public AdvancedRGBVideoPlayer(String path) throws IOException {
        in = new DataInputStream(new FileInputStream(path));
        width = in.readInt();
        height = in.readInt();
        fps = in.readInt();
        frameCount = in.readInt();
        keyframeInterval = in.readInt();
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        frameBuffer = new byte[width * height * 3];
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, null);
    }

    public void play() throws IOException, InterruptedException {
        JFrame f = new JFrame("My Video Player");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(width, height);
        f.add(this);
        f.setVisible(true);

        for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
            boolean isKeyframe = in.readBoolean();
            if (isKeyframe) {
                in.readFully(frameBuffer);
            } else {
                int runCount = in.readInt();
                for (int i = 0; i < runCount; i++) {
                    int startPixel = in.readInt();
                    int runLength = in.readUnsignedShort();
                    int r = in.readUnsignedByte();
                    int g = in.readUnsignedByte();
                    int b = in.readUnsignedByte();

                    for (int j = 0; j < runLength; j++) {
                        int offset = (startPixel + j) * 3;
                        frameBuffer[offset] = (byte) r;
                        frameBuffer[offset + 1] = (byte) g;
                        frameBuffer[offset + 2] = (byte) b;
                    }
                }
            }

            // Set pixels to image
            for (int i = 0; i < width * height; i++) {
                int r = frameBuffer[i * 3] & 0xFF;
                int g = frameBuffer[i * 3 + 1] & 0xFF;
                int b = frameBuffer[i * 3 + 2] & 0xFF;
                image.setRGB(i % width, i / width, (r << 16) | (g << 8) | b);
            }

            repaint();
            Thread.sleep(1000 / fps);
        }

        in.close();
    }

    public static void main(String[] args) throws Exception {
        new AdvancedRGBVideoPlayer("/home/haldun-altuntop/raw-frames/pavane.video").play();
    }
}
