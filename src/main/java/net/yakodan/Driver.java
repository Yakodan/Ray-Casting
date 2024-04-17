package net.yakodan;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferStrategy;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

public class Driver implements Runnable, MouseMotionListener, KeyListener {


    public static final int numLines = 8;
    private static final Random rand = new Random(234);
    public static int WIDTH, HEIGHT;
    private static double viewDirection = 0;
    private static final double viewAngle = (Math.PI / 3);
    private double cameraX, cameraY;
    private static double cameraDirection = viewDirection - viewAngle / 2;
    private static final double cameraSpeed = 4.5f;
    private static final int maxDist = 1200;
    private static final int resolution = 100;
    private static final int mapScale = 10;
    private static final Color rayColor = Color.WHITE;
    private final Canvas canvas;
    private static LinkedList<Line2D.Double> lines;
    private static LinkedList<Line2D.Double> rays;
    private static boolean isRunning = true;

    public Driver(int width, int height, Canvas canvas) {
        WIDTH = width;
        HEIGHT = height;
        cameraX = (double) WIDTH / 2;
        cameraY = (double) HEIGHT / 2;
        this.canvas = canvas;
        lines = buildLines();
    }

    @Override
    public void run() {
        while (isRunning) {
            render();
        }
        System.exit(0);
    }

    private LinkedList<Line2D.Double> buildLines() {
        LinkedList<Line2D.Double> lines = new LinkedList<>();
        int i = 0;
        while (i < numLines) {
            int[] x = new int[2];
            int[] y = new int[2];
            x[0] = rand.nextInt(WIDTH);
            y[0] = rand.nextInt(HEIGHT);
            x[1] = rand.nextInt(WIDTH);
            y[1] = rand.nextInt(HEIGHT);

            Arrays.sort(x);
            Arrays.sort(y);

            if (cameraX >= x[0] && cameraX <= x[1] && cameraY >= y[0] && cameraY <= y[1]) {
                continue;
            }

            lines.add(new Line2D.Double(x[0], y[0], x[0], y[1]));
            lines.add(new Line2D.Double(x[0], y[0], x[1], y[0]));
            lines.add(new Line2D.Double(x[0], y[1], x[1], y[1]));
            lines.add(new Line2D.Double(x[1], y[0], x[1], y[1]));

            i++;
        }

        lines.add(new Line2D.Double(0, 0, 0, HEIGHT));
        lines.add(new Line2D.Double(0, 0, WIDTH, 0));
        lines.add(new Line2D.Double(0, HEIGHT, WIDTH, HEIGHT));
        lines.add(new Line2D.Double(WIDTH, 0, WIDTH, HEIGHT));

        return lines;
    }

    private void render() {
        BufferStrategy bs = canvas.getBufferStrategy();
        if (bs == null) {
            canvas.createBufferStrategy(2);
            return;
        }

        Graphics g = bs.getDrawGraphics();
        g.setColor(new Color(0x2B2B2B));
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        draw3DVariant(g);
        draw2DVariant(g);

        drawHint(g);

        lockCursor();

        g.dispose();
        bs.show();
    }

    private void draw2DVariant(Graphics g) {
        g.setColor(new Color(0x000000));
        g.fillRect(0, 0, canvas.getWidth()/mapScale, canvas.getHeight()/mapScale);
        drawWalls(g);
        drawRays(g);
    }

    private void draw3DVariant(Graphics g) {
        rays = calcRays(lines, cameraX, cameraY);

        double colWidth = (double) WIDTH / resolution;
        for (int i = 0; i < resolution; i++) {
            Line2D.Double ray = rays.get(rays.size() - i - 1);
            double distance = dist(ray.getX1(), ray.getY1(), ray.getX2(), ray.getY2());
            double colHeight = calcWallHeight(distance);

            g.setColor(calcColor(distance));
            g.fillRect((int) (colWidth * i), (int) (HEIGHT - colHeight) / 2, (int) (colWidth), (int) (colHeight));
            g.drawRect((int) (colWidth * i), (int) (HEIGHT - colHeight) / 2, (int) (colWidth), (int) (colHeight));
        }
    }

    private Color calcColor(double distance) {
        if(distance<50){
             return new Color(255, 255, 255);
        } else if(distance<300){
            return new Color(169, 169, 169);
        } else if(distance<700){
            return new Color(102, 102, 102);
        } else if(distance<1000){
            return new Color(51, 51, 51);
        } else if(distance<maxDist){
            return new Color(25, 25, 25);
        }
        return new Color(0, 0, 0);
    }

    private double calcWallHeight(double distance) {
        if (distance < 10) {
            return HEIGHT;
        }
        return HEIGHT - distance;
    }

    private static void drawHint(Graphics g) {
        g.setColor(new Color(0x208000));
        g.setFont(new Font("Arial", Font.ITALIC, (int) (HEIGHT * 0.02)));
        g.drawString("H to Help", (int) (WIDTH * 0.94), (int) (HEIGHT * 0.98));
    }

    private void lockCursor() {
        if (canvas.isFocusOwner()) {
            try {
                new Robot().mouseMove(WIDTH / 2, HEIGHT / 2);
            } catch (AWTException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void drawWalls(Graphics g) {
        g.setColor(new Color(0x499B54));
        for (Line2D.Double line : lines) {
            g.drawLine((int) line.getX1()/mapScale, (int) line.getY1()/mapScale, (int) line.getX2()/mapScale, (int) line.getY2()/mapScale);
        }
    }

    private void drawRays(Graphics g) {
        g.setColor(rayColor);
        rays = calcRays(lines, cameraX, cameraY);
        for (Line2D.Double ray : rays) {
            g.drawLine((int) ray.getX1()/mapScale, (int) ray.getY1()/mapScale, (int) ray.getX2()/mapScale, (int) ray.getY2()/mapScale);
        }
    }

    // From here https://gist.github.com/Oisann/5d78b9a3d4db357a30f2e83d9b8fdff0
    public static double dist(double x1, double y1, double x2, double y2) {
        return Math.hypot((x2 - x1), (y2 - y1));
    }

    // From here https://gist.github.com/Oisann/5d78b9a3d4db357a30f2e83d9b8fdff0
    public static double getRayCast(double p0_x, double p0_y, double p1_x, double p1_y, double p2_x, double p2_y, double p3_x, double p3_y) {
        double s1_x, s1_y, s2_x, s2_y;
        s1_x = p1_x - p0_x;
        s1_y = p1_y - p0_y;
        s2_x = p3_x - p2_x;
        s2_y = p3_y - p2_y;

        double s, t;
        s = (-s1_y * (p0_x - p2_x) + s1_x * (p0_y - p2_y)) / (-s2_x * s1_y + s1_x * s2_y);
        t = (s2_x * (p0_y - p2_y) - s2_y * (p0_x - p2_x)) / (-s2_x * s1_y + s1_x * s2_y);

        if (s >= 0 && s <= 1 && t >= 0 && t <= 1) {
            // Collision detected
            double x = p0_x + (t * s1_x);
            double y = p0_y + (t * s1_y);

            return dist(p0_x, p0_y, x, y);
        }

        return -1; // No collision
    }

    private LinkedList<Line2D.Double> calcRays(LinkedList<Line2D.Double> lines, double cameraX, double cameraY) {
        LinkedList<Line2D.Double> rays = new LinkedList<>();
        for (int i = 0; i < resolution; i++) {
            // Shot rays in some part of circle (PI/3) which we can rotate
            double dir = viewDirection + -viewAngle * ((double) i / resolution);
            double minDist = maxDist;
            for (Line2D.Double line : lines) {
                // Calculate distance from our "cursor" to all lines in every direction
                double dist = getRayCast(
                        cameraX, cameraY,
                        cameraX + Math.cos(dir) * maxDist,
                        cameraY + Math.sin(dir) * maxDist,
                        line.x1, line.y1, line.x2, line.y2);

                // Then find first collision, which has min distance
                if (dist < minDist && dist > 0) {
                    minDist = dist;
                }
            }
            // Fill rays list
            rays.add(new Line2D.Double(
                    cameraX, cameraY,
                    cameraX + Math.cos(dir) * minDist,
                    cameraY + Math.sin(dir) * minDist));
        }

        return rays;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        rotateCamera(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        rotateCamera(e);
    }

    private static void rotateCamera(MouseEvent e) {
        double shift = e.getX() - (double) WIDTH / 2;
        viewDirection = ((viewDirection + shift / 500) % (Math.PI * 2));
        cameraDirection = ((cameraDirection + shift / 500) % (Math.PI * 2));
    }

    private boolean isPossibleStep(double x1, double y1, double x2, double y2) {
        for (Line2D.Double line : lines) {
            if (line.intersectsLine(x1, y1, x2 * 2 - x1, y2 * 2 - y1)) {
                return false;
            }
        }
        return true;
    }

    private void controller(KeyEvent e) {
        double newX = cameraX;
        double newY = cameraY;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> {
                newX = cameraX + Math.cos(cameraDirection) * cameraSpeed;
                newY = cameraY + Math.sin(cameraDirection) * cameraSpeed;
            }
            case KeyEvent.VK_S -> {
                newX = cameraX - Math.cos(cameraDirection) * cameraSpeed;
                newY = cameraY - Math.sin(cameraDirection) * cameraSpeed;
            }
            case KeyEvent.VK_A -> {
                newX = cameraX + Math.sin(cameraDirection) * cameraSpeed;
                newY = cameraY - Math.cos(cameraDirection) * cameraSpeed;
            }
            case KeyEvent.VK_D -> {
                newX = cameraX - Math.sin(cameraDirection) * cameraSpeed;
                newY = cameraY + Math.cos(cameraDirection) * cameraSpeed;
            }
            case KeyEvent.VK_H -> {
                new HelpWindow();
            }
            case KeyEvent.VK_ESCAPE -> {
                isRunning = false;
            }
        }
        if (isPossibleStep(cameraX, cameraY, newX, newY)) {
            cameraX = newX;
            cameraY = newY;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        controller(e);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        controller(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        controller(e);
    }

}
