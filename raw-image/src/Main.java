import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {

        //PNGToIAConverter.convertToIA("test-foto-mini.png", "test-foto.ia");

        FileInputStream fis = new FileInputStream("test-foto.ia");
        byte[] data = fis.readAllBytes();
        fis.close();

        IAViewer iaViewer = new IAViewer();
        iaViewer.drawPixelByPixel(data, 220, 293);

        JFrame frame = new JFrame("Image Asset Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(iaViewer);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
