import javax.sound.sampled.*;
import javax.swing.*;
import javax.xml.datatype.Duration;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.DirectoryIteratorException;

public class Player extends JPanel {

    private static JFrame frame;

    private Clip audioClip;

    private BufferedImage image;
    private RandomAccessFile file;

    public static int with = 640;
    public static int height = 480;

    public Player(String filePath) throws IOException {
        file = new RandomAccessFile(filePath, "r");

        image = new BufferedImage(with, height, BufferedImage.TYPE_3BYTE_BGR);
        setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));


        try {
            audioClip = AudioSystem.getClip();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if (image != null) g.drawImage(image, 0, 0, null);
    }

    public void play() {

        new Thread(() -> {

            try {
                File ses = new File("/home/haldun-altuntop/raw-frames/belloff");
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(ses);

                audioClip = AudioSystem.getClip();
                audioClip.open(audioIn);
                audioClip.start();

            } catch (UnsupportedAudioFileException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (LineUnavailableException e) {
                throw new RuntimeException(e);
            }

        }).start();

        new Thread(() -> {
            try {

                int frames = Math.toIntExact(file.length() / (image.getHeight() * image.getWidth() * 3));
                int i = 0;
                while (i < frames) {

                    long frameStart = i * 41_666;
                    long current = audioClip.getMicrosecondPosition();

                    if (current > frameStart) {
                        image.getRaster().setDataElements(0, 0, with, height, readRGBData(with, height, i));
                        repaint();
                        i++;
                    }

                    if (current < frameStart) {
                        image.getRaster().setDataElements(0, 0, with, height, readRGBData(with, height, i));
                        repaint();
                        i--;
                    }
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();



    }
    public byte[] readRGBData(int width, int height, int frameIndex) throws IOException {
        int frameSize = width * height * 3;

        // Gidilecek yer = frameIndex * frameSize
        file.seek((long) frameIndex * frameSize);

        byte[] buffer = new byte[frameSize];
        file.readFully(buffer);
        //file.close();

        return buffer;
    }

    public BufferedImage readRGBFrame(int width, int height, int frameIndex) throws IOException {

        byte[] buffer = readRGBData(width, height, frameIndex);

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

    public static void main(String[] args) throws IOException {

        String fileName = "/home/haldun-altuntop/raw-frames/belloff.rgb";

        frame = new JFrame("Raw RGB Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(with, height);

        Player player = new Player(fileName);

        frame.setContentPane(player);
        frame.pack();

        player.play();

        frame.setVisible(true);

        frame.setFocusable(true);

        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);

                if (e.getKeyCode() == KeyEvent.VK_LEFT) {

                    long lastPosition = player.audioClip.getMicrosecondPosition();
                    player.audioClip.setMicrosecondPosition(lastPosition - 5000000);
                }

                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {

                    long lastPosition = player.audioClip.getMicrosecondPosition();
                    player.audioClip.setMicrosecondPosition(lastPosition + 5000000);
                }
            }
        });
    }
}
