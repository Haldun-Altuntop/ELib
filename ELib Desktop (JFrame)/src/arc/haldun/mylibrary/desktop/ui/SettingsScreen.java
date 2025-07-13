package arc.haldun.mylibrary.desktop.ui;

import arc.haldun.database.Sorting;
import arc.haldun.mylibrary.desktop.PreferenceManager;
import arc.haldun.mylibrary.desktop.components.Screen;
import arc.haldun.mylibrary.desktop.components.SlideMenu;
import arc.haldun.mylibrary.desktop.components.SlideMenuItem;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public class SettingsScreen extends Screen {

    public static SettingsScreen Instance;

    private static final int PANEL_BOOK_INDEX = 0;
    private static final int PANEL_BOOK_LOCALE = 1;

    private SlideMenu slideMenu;
    private ArrayList<SlideMenuItem> items;
    private ArrayList<JPanel> panels;

    private boolean showBusyBooks;

    private boolean automaticRestartNeeded = false;

    @Override
    public void init() {
        super.init();

        Instance = this;

        setResizable(false);
        setSize(854, 480);
        setTitle("Ayarlar");

        items = new ArrayList<>();
        panels = new ArrayList<>();

        slideMenu = new SlideMenu(this);
        initMenuItems();
        add(slideMenu);

        addCloseAction();

        panels.add(prepareBookPanel());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);

                if (automaticRestartNeeded) {
                    MainScreen.Instance.dispose();
                    new MainScreen().setVisible(true);
                }
            }
        });
    }

    private JPanel prepareBookPanel() {

        JPanel panel_book = new JPanel();
        panel_book.setLayout(null);

        int x = slideMenu.getX() + slideMenu.getWidth();
        int width = getWidth() - slideMenu.getWidth();

        panel_book.setLocation(x, 0);
        panel_book.setSize(width, getHeight());

        JCheckBox cb_showBusyBooks = new JCheckBox("Show Busy Books");
        cb_showBusyBooks.setSize(150, 50);
        cb_showBusyBooks.setSelected(PreferenceManager.showBusyBooks);
        cb_showBusyBooks.setLocation((panel_book.getWidth() - cb_showBusyBooks.getWidth()) / 2, 16);
        cb_showBusyBooks.addChangeListener(e -> {
            showBusyBooks = cb_showBusyBooks.isSelected();
            automaticRestartNeeded = true;
            PreferenceManager.setShowBusyBooks(showBusyBooks);
        });
        panel_book.add(cb_showBusyBooks);

        //----------

        String[] sorting = {"A'dan Z'ye", "Z'den A'ya", "Eskiden yeniye", "Yeniden eskiye", "A'dan Z'ye (Yazar)",
                "A'dan Z'ye (Yazar)", "Daha popüler", "Az popüler"};

        JComboBox<String> comboBoxSorting = new JComboBox<>(sorting);
        comboBoxSorting.setSize(150, 20);
        comboBoxSorting.setLocation((panel_book.getWidth() - comboBoxSorting.getWidth()) / 2, 74);
        comboBoxSorting.setSelectedIndex(PreferenceManager.bookSorting.getIndex());
        comboBoxSorting.addItemListener(e -> {

            if (e.getStateChange() == ItemEvent.SELECTED) {

                automaticRestartNeeded = true;

                Sorting bookSorting = Sorting.valueOf(comboBoxSorting.getSelectedIndex());
                PreferenceManager.setBookSorting(bookSorting);
                System.out.println(PreferenceManager.bookSorting);
            }
        });
        panel_book.add(comboBoxSorting);

        return panel_book;
    }

    private void initMenuItems() {

        int margin = 4;

        SlideMenuItem item_kitap = new SlideMenuItem(null, "Kitap");
        item_kitap.setSize(SlideMenu.MAX_WIDTH - margin * 2, 96);
        item_kitap.setLocation(margin, margin);
        item_kitap.addAction(() -> {

            for (SlideMenuItem item : items) {
                item.setSelected(false);
            }

            item_kitap.setSelected(true);
            setTitle("Ayarlar - Kitap");

            setPage(PANEL_BOOK_INDEX);

        });
        slideMenu.add(item_kitap);
        items.add(item_kitap);

        SlideMenuItem item_locale = new SlideMenuItem(null, "Diller ve Giriş");
        item_locale.setSize(SlideMenu.MAX_WIDTH - margin * 2, 96);
        item_locale.setLocation(margin, item_kitap.getY() + item_kitap.getHeight() + margin);
        item_locale.addAction(() -> {

            for (SlideMenuItem item : items) {
                item.setSelected(false);
            }

            item_locale.setSelected(true);
            setTitle("Ayarlar - Diller ve Giriş");

        });
        slideMenu.add(item_locale);
        items.add(item_locale);

    }

    private void setPage(int index) {
        add(panels.get(index));
        repaint();
    }

    private void addCloseAction() {
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);

                Instance = null;

            }
        });
    }
}
