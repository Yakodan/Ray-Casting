package net.yakodan;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

public class HelpWindow extends JFrame implements KeyListener {

    public HelpWindow() {
        setTitle("Help");
        setSize(400, 200);

        String help = "<html>"+
                "<i>W,A,S,D</i> - move<br>" +
                "<i>Mouse</i> - rotate camera<br>" +
                "<i>Escape</i> - exit from program<br>" +
                "</html>";
        JLabel label = new JLabel(help);
        Font font = new Font("Arial", Font.BOLD, 24);
        label.setFont(font);
        getContentPane().add(label);

        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                cursorImg, new Point(0, 0), "blank cursor");
        this.setCursor(blankCursor);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);

        label.addKeyListener(this);
        label.setFocusable(true);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        switch (e.getKeyChar()) {
            case KeyEvent.VK_ESCAPE -> {
                this.dispose();
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e){

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

//    public static void main(String[] args) {
//        new net.yakodan.HelpWindow();
//    }
}
