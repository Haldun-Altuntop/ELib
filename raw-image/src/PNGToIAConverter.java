import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PNGToIAConverter { // IA = Image Asset

    public static void convertToIA(String inputPNGFile, String outputIAFile) throws IOException {

        File pngFile = new File(inputPNGFile);

        BufferedImage image = ImageIO.read(pngFile);

        int width = image.getWidth();
        int height = image.getHeight();

        byte[] rgbData = new byte[width * height * 3];

        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);

                rgbData[index++] = (byte) ((pixel >> 16) & 0xFF); // RED
                rgbData[index++] = (byte) ((pixel >> 8) & 0xFF); // GREEN
                rgbData[index++] = (byte) (pixel & 0xFF); // BLUE
            }
        }

        File output = new File(outputIAFile);
        output.createNewFile();

        FileOutputStream fos = new FileOutputStream(outputIAFile);
        fos.write(rgbData);
        fos.close();
    }
}
