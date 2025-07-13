package arc.haldun.mylibrary.desktop.components;

import javax.swing.*;
import java.awt.*;

public class BookInfoElement extends JPanel {

    private JLabel lbl_title, lbl_text;
    private int margin = 4;
    private int cornerRadius = 10;

    public BookInfoElement(String title, String text) {

        setSize(100, 100);
        setBackground(Color.DARK_GRAY);
        setOpaque(false);

        setLayout(null);

        lbl_title = new JLabel(title);
        lbl_text = new JLabel(text);

        lbl_title.setFont(new Font("Arial", Font.BOLD, 20));
        lbl_text.setFont(new Font("Arial", Font.ITALIC, 15));

        lbl_title.setHorizontalAlignment(SwingConstants.CENTER);
        lbl_text.setHorizontalAlignment(SwingConstants.CENTER);

        lbl_title.setSize(getWidth(), (getHeight() - margin * 3) / 2);
        lbl_text.setSize(getWidth(), (getHeight() - margin * 3) / 2);

        lbl_title.setLocation(0, margin);
        lbl_text.setLocation(0, lbl_title.getY() + lbl_title.getHeight() + margin);

        lbl_title.setForeground(Color.WHITE);
        lbl_text.setForeground(Color.LIGHT_GRAY);

        add(lbl_title);
        add(lbl_text);

    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);

        resizeComponents();
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Grafik nesnesini 2D moduna çevir
        Graphics2D g2 = (Graphics2D) g;

        // Yumuşak kenarlar için antialiasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Arka plan rengini ayarla
        g2.setColor(getBackground());

        // Yuvarlatılmış dikdörtgen çiz
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

        // Normal bileşen çizimini çağır
        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Kenarlık çizmek isterseniz burada özelleştirebilirsiniz
        g2.setColor(getForeground());
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
    }

    private void resizeComponents() {

        if (lbl_title != null) {

            lbl_title.setSize(getWidth(), (getHeight() / 5) * 2);
            lbl_title.setLocation(0, getHeight() / 5);
        }

        if (lbl_text != null) {

            lbl_text.setSize(getWidth(), (getHeight() / 5) * 2);
            lbl_text.setLocation(0, lbl_title.getY() + lbl_title.getHeight() - lbl_title.getHeight() / 2);
        }
    }

    public void setTitle(String title) {
        lbl_title.setText(title);
    }

    public void setText(String text) {
        lbl_text.setText(text);
    }

    public int getMargin() {
        return margin;
    }

    public void setMargin(int margin) {
        this.margin = margin;
    }
}
