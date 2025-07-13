package arc.haldun.mylibrary.desktop.ui;

import arc.haldun.database.database.Manager;
import arc.haldun.database.database.MariaDB;
import arc.haldun.database.exception.OperationFailedException;
import arc.haldun.database.objects.Book;
import arc.haldun.database.objects.CurrentUser;
import arc.haldun.mylibrary.desktop.components.Screen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ProfileScreen extends Screen {

    public static ProfileScreen Instance;

    private JLabel lblBorrowedBook;

    @Override
    public void init() {

        Instance = this;

        setTitle("Hesap Bilgileri");
        setSize(400, 250);
        setLocationRelativeTo(null); // Ortala
        setResizable(false);

        // Ana panel ve başlık
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // boşluk

        JLabel baslik = new JLabel("Hesap Bilgileri", JLabel.CENTER);
        baslik.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(baslik, BorderLayout.NORTH);

        // Bilgi alanlarını içeren grid panel
        JPanel bilgiPaneli = new JPanel(new GridLayout(5, 2, 10, 10));

        bilgiPaneli.add(new JLabel("Kullanıcı Adı:"));
        bilgiPaneli.add(new JLabel(CurrentUser.user.getName()));

        bilgiPaneli.add(new JLabel("E-posta:"));
        bilgiPaneli.add(new JLabel(CurrentUser.user.getEMail()));

        bilgiPaneli.add(new JLabel("Şifre:"));
        bilgiPaneli.add(new JLabel(CurrentUser.user.getPassword()));

        bilgiPaneli.add(new JLabel("Kayıt Tarihi:"));
        bilgiPaneli.add(new JLabel(CurrentUser.user.getRegistrationDate().toString()));

        bilgiPaneli.add(new JLabel("Ödünç Aldığı Kitap:"));
        bilgiPaneli.add(lblBorrowedBook = new JLabel("Yükleniyor..."));

        panel.add(bilgiPaneli, BorderLayout.CENTER);

        add(panel);

        initBorrowedBook();

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);

                Instance = null;
            }
        });
    }

    private void initBorrowedBook() {

        new Thread(() -> {

            try {
                Book borrowedBook = new Manager(new MariaDB()).getBook(CurrentUser.user.getBorrowedBook());
                lblBorrowedBook.setText(borrowedBook.getName());
            } catch (OperationFailedException e) {
                lblBorrowedBook.setText("Ödünç alınmamış");
            }

        }).start();
    }
}
