import javax.swing.*;
import java.awt.*;

public class IAViewer extends JPanel {

    private byte[] iaData;
    private int width, height;

    private int spacing = 0;
    private int pixelSize = 1;
    private int stride = spacing + pixelSize;

    public void drawPixelByPixel(byte[] iaData, int width, int height) {
        this.iaData = iaData;
        this.width = width;
        this.height = height;

        setPreferredSize(new Dimension(width, height));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                int red = iaData[index++] & 0xFF;
                int green = iaData[index++] & 0xFF;
                int blue = iaData[index++] & 0xFF;

                g.setColor(new Color(red, green, blue));
                g.fillRect(x * stride, y * stride, pixelSize, pixelSize);
            }
        }


        /*
        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                int red = iaData[index++] & 0xFF;
                int green = iaData[index++] & 0xFF;
                int blue = iaData[index++] & 0xFF;

                g.setColor(new Color(red, green, blue));
                g.fillRect(x, y, 1, 1);
            }
        }
         */
    }
}
