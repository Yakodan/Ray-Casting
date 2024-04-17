package net.yakodan;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

public class MainApplication extends JFrame {

    static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    public static int WIDTH = (int) screenSize.getWidth(), HEIGHT = (int) screenSize.getHeight();
    private final Canvas canvas;
    private final JFrame frame;
    private Driver driver;

    private MainApplication(){
        frame = new JFrame("Java Raycasting");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(canvas = new Canvas());
        driver = new Driver(WIDTH, HEIGHT,canvas);
        canvas.addKeyListener(driver);
        canvas.addMouseMotionListener(driver);

        // Remove cursor from the screen
        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                cursorImg, new Point(0, 0), "blank cursor");
        frame.setCursor(blankCursor);


        canvas.setFocusable(true);

        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setUndecorated(true);
        frame.setVisible(true);
        WIDTH = (int) frame.getSize().getWidth();
        HEIGHT = (int) frame.getSize().getHeight();

        new Thread(driver).start();
    }

    public static void main(String[] args) {
        new MainApplication();
    }
}
