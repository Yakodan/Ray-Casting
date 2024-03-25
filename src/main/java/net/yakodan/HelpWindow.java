package net.yakodan;

import javax.swing.*;
import java.awt.*;

public class HelpWindow extends JFrame {

    public HelpWindow() {
        setTitle("Help");
        setSize(400, 200);

        String help = "<html>A - rotate to the left <br> D - rotate to the right <br> W - increase max distance<br>S - decrease max distance</html>";
        JLabel label = new JLabel(help);
        Font font = new Font("Arial", Font.BOLD, 24);
        label.setFont(font);
        getContentPane().add(label);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

//    public static void main(String[] args) {
//        new net.yakodan.HelpWindow();
//    }
}
