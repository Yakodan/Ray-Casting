package net.yakodan;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.Random;

public class Driver implements Runnable, MouseMotionListener, KeyListener {

    static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    public static int WIDTH = (int) screenSize.getWidth(), HEIGHT = (int) screenSize.getHeight();
    public static final int numLines = 8;
    private static final Random rand = new Random(53);
    private Canvas canvas;
    private JFrame frame;
    private static float viewDirection = 0;
    private static float viewAngle = (float) (Math.PI / 3);
    private float cameraX = WIDTH / 2, cameraY = HEIGHT / 2;
    private static float cameraDirection = viewDirection - viewAngle / 2;
    private static float cameraSpeed = 4.5f;
    private static int maxDist = 1200;
    private static int resolution = 100;
    private static Color rayColor = Color.WHITE;
    //    private static float attentionDistance = 80;
    LinkedList<Line2D.Float> lines;

    private Driver() {
        lines = buildLines();
        frame = new JFrame("Java Raycasting");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(canvas = new Canvas());
        canvas.addKeyListener(this);
        canvas.addMouseMotionListener(this);

        // Remove cursor from the screen
        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                cursorImg, new Point(0, 0), "blank cursor");
        frame.setCursor(blankCursor);


        canvas.setFocusable(true);

        frame.setSize(WIDTH, HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        new Thread(this).start();
    }

    private LinkedList<Line2D.Float> buildLines() {
        LinkedList<Line2D.Float> lines = new LinkedList<>();
        for (int i = 0; i < numLines; i++) {
            int x1 = rand.nextInt(WIDTH);
            int y1 = rand.nextInt(HEIGHT);
            int x2 = rand.nextInt(WIDTH);
            int y2 = rand.nextInt(HEIGHT);
            lines.add(new Line2D.Float(x1, y1, x2, y2));
        }
        return lines;
    }

    @Override
    public void run() {
        while (true) {
            render();
        }
    }

    private void render() {
        BufferStrategy bs = canvas.getBufferStrategy();
        if (bs == null) {
            canvas.createBufferStrategy(2);
            return;
        }
        Graphics g = bs.getDrawGraphics();

        // Fill the background
        g.setColor(new Color(0x2B2B2B));
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw the walls
        g.setColor(new Color(0x499B54));
        for (Line2D.Float line : lines) {
            g.drawLine((int) line.getX1(), (int) line.getY1(), (int) line.getX2(), (int) line.getY2());
        }

        // Draw the rays
        g.setColor(rayColor);
        LinkedList<Line2D.Float> rays = calcRays(lines, (int) cameraX, (int) cameraY, resolution, maxDist);
        for (Line2D.Float ray : rays) {
            g.drawLine((int) ray.getX1(), (int) ray.getY1(), (int) ray.getX2(), (int) ray.getY2());
        }

//        // Draw the attention circle (when any wall is in it, rays change their color to red)
//        g.setColor(new Color(0x9F1E1E));
//        g.drawOval((int) (cameraX - attentionDistance), (int) (cameraY - attentionDistance), (int) attentionDistance * 2, (int) attentionDistance * 2);

        // Draw help hint in the corner
        g.setColor(new Color(0x208000));
        g.setFont(new Font("Arial", Font.ITALIC, (int) (HEIGHT*0.02)));
        g.drawString("H to Help", (int) (WIDTH*0.01), (int) (HEIGHT * 0.02));
//        g.drawString(""+viewDirection, cameraX, cameraY);

        try {
            new Robot().mouseMove(WIDTH / 2, HEIGHT / 2);
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }

        g.dispose();
        bs.show();
    }

    // From here https://gist.github.com/Oisann/5d78b9a3d4db357a30f2e83d9b8fdff0
    public static float dist(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    // From here https://gist.github.com/Oisann/5d78b9a3d4db357a30f2e83d9b8fdff0
    public static float getRayCast(float p0_x, float p0_y, float p1_x, float p1_y, float p2_x, float p2_y, float p3_x, float p3_y) {
        float s1_x, s1_y, s2_x, s2_y;
        s1_x = p1_x - p0_x;
        s1_y = p1_y - p0_y;
        s2_x = p3_x - p2_x;
        s2_y = p3_y - p2_y;

        float s, t;
        s = (-s1_y * (p0_x - p2_x) + s1_x * (p0_y - p2_y)) / (-s2_x * s1_y + s1_x * s2_y);
        t = (s2_x * (p0_y - p2_y) - s2_y * (p0_x - p2_x)) / (-s2_x * s1_y + s1_x * s2_y);

        if (s >= 0 && s <= 1 && t >= 0 && t <= 1) {
            // Collision detected
            float x = p0_x + (t * s1_x);
            float y = p0_y + (t * s1_y);

            return dist(p0_x, p0_y, x, y);
        }

        return -1; // No collision
    }

    private LinkedList<Line2D.Float> calcRays(LinkedList<Line2D.Float> lines, int cameraX, int cameraY,
                                              int resolution, int maxDist) {
        LinkedList<Line2D.Float> rays = new LinkedList<>();
        boolean isAttention = false;
        for (int i = 0; i < resolution; i++) {
            // Shot rays in some part of circle (PI/3) which we can rotate
            double dir = viewDirection + -viewAngle * ((double) i / resolution);
            float minDist = maxDist;
            for (Line2D.Float line : lines) {
                // Calculate distance from our "cursor" to all lines in every direction
                float dist = getRayCast(cameraX, cameraY, cameraX + (float) Math.cos(dir) * maxDist,
                        cameraY + (float) Math.sin(dir) * maxDist, line.x1, line.y1, line.x2, line.y2);

                // Then find first collision, which has min distance
                if (dist < minDist && dist > 0) {
                    minDist = dist;
                }

//                // If distance to some line is lower than attention distance, then attention flag get pulled up
//                if (minDist < attentionDistance) {
//                    isAttention = true;
//                }
            }

            // Fill rays list
            rays.add(new Line2D.Float(cameraX, cameraY, cameraX + (float) Math.cos(dir) * minDist, cameraY + (float) Math.sin(dir) * minDist));
        }

//        // If some ray length is lower than attention distance, then all rays become red
//        if (isAttention) {
//            rayColor = new Color(0xCC2626); // Change color of rays to white
//        } else {
//            rayColor = Color.WHITE; // Change color of rays to white
//        }

        return rays;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int shift = e.getX() - WIDTH / 2 + WIDTH / 190;
        viewDirection = (float) ((viewDirection + (float) shift / 500) % (Math.PI * 2));
        cameraDirection = (float) ((cameraDirection + (float) shift / 500) % (Math.PI * 2));
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int shift = e.getX() - WIDTH / 2 + WIDTH / 190;
        viewDirection = (float) ((viewDirection + (float) shift / 500) % (Math.PI * 2));
        cameraDirection = (float) ((cameraDirection + (float) shift / 500) % (Math.PI * 2));
    }

    @Override
    public void keyTyped(KeyEvent e) {
        switch (e.getKeyChar()) {
            case KeyEvent.VK_W -> {
                cameraX += (float) Math.cos(cameraDirection) * cameraSpeed;
                cameraY += (float) Math.sin(cameraDirection) * cameraSpeed;
            }
            case KeyEvent.VK_S -> {
                cameraX -= (float) Math.cos(cameraDirection) * cameraSpeed;
                cameraY -= (float) Math.sin(cameraDirection) * cameraSpeed;
            }
            case KeyEvent.VK_A -> {
                cameraX += (float) Math.sin(cameraDirection) * cameraSpeed;
                cameraY -= (float) Math.cos(cameraDirection) * cameraSpeed;
            }
            case KeyEvent.VK_D -> {
                cameraX -= (float) Math.sin(cameraDirection) * cameraSpeed;
                cameraY += (float) Math.cos(cameraDirection) * cameraSpeed;
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> {
                cameraX += (float) Math.cos(cameraDirection) * cameraSpeed;
                cameraY += (float) Math.sin(cameraDirection) * cameraSpeed;
            }
            case KeyEvent.VK_S -> {
                cameraX -= (float) Math.cos(cameraDirection) * cameraSpeed;
                cameraY -= (float) Math.sin(cameraDirection) * cameraSpeed;
            }
            case KeyEvent.VK_A -> {
                cameraX += (float) Math.sin(cameraDirection) * cameraSpeed;
                cameraY -= (float) Math.cos(cameraDirection) * cameraSpeed;
            }
            case KeyEvent.VK_D -> {
                cameraX -= (float) Math.sin(cameraDirection) * cameraSpeed;
                cameraY += (float) Math.cos(cameraDirection) * cameraSpeed;
            }
            case KeyEvent.VK_H -> {
                new HelpWindow();
            }
            case KeyEvent.VK_ESCAPE -> {
                System.exit(0);
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> {
                cameraX += (float) Math.cos(cameraDirection)*cameraSpeed;
                cameraY += (float) Math.sin(cameraDirection)*cameraSpeed;
            }
            case KeyEvent.VK_S -> {
                cameraX -= (float) Math.cos(cameraDirection) * cameraSpeed;
                cameraY -= (float) Math.sin(cameraDirection) * cameraSpeed;
            }
            case KeyEvent.VK_A -> {
                cameraX += (float) Math.sin(cameraDirection) * cameraSpeed;
                cameraY -= (float) Math.cos(cameraDirection) * cameraSpeed;
            }
            case KeyEvent.VK_D -> {
                cameraX -= (float) Math.sin(cameraDirection) * cameraSpeed;
                cameraY += (float) Math.cos(cameraDirection) * cameraSpeed;
            }
        }
    }

    public static void main(String[] args) {
        new Driver();
    }

}
