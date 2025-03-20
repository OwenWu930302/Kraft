import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Simple3DCamera extends Frame implements KeyListener, WindowListener, MouseListener, MouseMotionListener {
    private static final int WIDTH = 500;
    private static final int HEIGHT = 400;

    private double cameraX = 0.0, cameraY = 0.0, cameraZ = -5.0;
    private double cameraAngleX = 0.0, cameraAngleY = 0.0;

    private int lastMouseX, lastMouseY;
    private boolean dragging = false;

    private BufferedImage background_pic;
    private BufferedImage[] images = new BufferedImage[6];

    private double moveStep = 2;

    private boolean gameStarted = false;
    private boolean gameFailed = false;

    private Rectangle restartButtonBounds; // restart button

    private double max_X = 0, min_X = 0;
    private double max_Y = 0, min_Y = 0;

    private ArrayList<SmallCube> smallCubes = new ArrayList<>();

    private boolean isAnimating = false;
    private double relativeCameraZ = -5.0;
    private double smallCubeZ = 0.0;

    public Simple3DCamera() {
        super("Simple 3D Camera");

        setSize(WIDTH, HEIGHT);

        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        addWindowListener(this);

        setFocusable(true);
        setResizable(false);
        setVisible(true);

        loadCubeImages();
        initializeSmallCubes();
        find_X_Y();
    }

    public void find_X_Y() {
        if (smallCubes.isEmpty()) {
            System.out.println("No small cubes to process.");
            return;
        }

        min_X = Double.MAX_VALUE;
        max_X = -Double.MAX_VALUE; // Use -Double.MAX_VALUE instead of Double.MIN_VALUE for proper initialization
        min_Y = Double.MAX_VALUE;
        max_Y = -Double.MAX_VALUE; // Use -Double.MAX_VALUE instead of Double.MIN_VALUE for proper initialization

        for (SmallCube cube : smallCubes) {
            if (cube.y < min_Y) {
                min_Y = cube.y;
            }
            if (cube.y > max_Y) {
                max_Y = cube.y;
            }
            if (cube.x < min_X) {
                min_X = cube.x;
            }
            if (cube.x > max_X) {
                max_X = cube.x;
            }
        }
        System.out.println("Final min_X: " + min_X + ", max_X: " + max_X + ", min_Y: " + min_Y + ", max_Y: " + max_Y);
    }

    // Load cube images
    // Change picture directory before run
    // Template: "C:\\Users\\User\\Pictures\\Saved Pictures\\1.jpg"
    public void loadCubeImages() {
        try {
            background_pic = ImageIO.read(new File("C:/Users/user/Desktop/bg.png"));
            for (int i = 0; i < 6; i++) {
                images[i] = ImageIO.read(new File("C:/Users/user/Desktop/grass.jpg"));
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading images");
        }
    }

    // Initialize small cubes
    public void initializeSmallCubes() {
        double startX = -5;
        double startY = -5;
        for (int i = 0; i <= 10; i++) {
            for (int j = 0; j <= 5; j++) {
                smallCubes.add(new SmallCube(startX + i, startY + j, 0.0));
            }
        }
    }

    private double[][] mainCubeVertices = {
            {-1, -1, -1}, {-1, -1, 1}, {-1, 1, -1}, {-1, 1, 1},
            {1, -1, -1}, {1, -1, 1}, {1, 1, -1}, {1, 1, 1}
    };

    private double[][] smallCubeVertices = {
            {-0.5, -0.5, -0.5}, {-0.5, -0.5, 0.5}, {-0.5, 0.5, -0.5}, {-0.5, 0.5, 0.5},
            {0.5, -0.5, -0.5}, {0.5, -0.5, 0.5}, {0.5, 0.5, -0.5}, {0.5, 0.5, 0.5}

    };

    private int[][] cubeFaces = {
            {0, 1, 3, 2}, {4, 5, 7, 6}, {0, 1, 5, 4},
            {2, 3, 7, 6}, {0, 2, 6, 4}, {1, 3, 7, 5}
    };

    private double[] projectPoint(double x, double y, double z) {
        double fov = 256;
        double scale = fov / (fov + z);
        double projectedX = x * scale + WIDTH / 2;
        double projectedY = -y * scale + HEIGHT / 2;
        return new double[]{projectedX, projectedY};
    }

    private double[] transformPoint(double x, double y, double z) {
        x -= cameraX;
        y -= cameraY;
        z -= cameraZ;

        double cosZ = Math.cos(cameraAngleY);
        double sinZ = Math.sin(cameraAngleY);
        double tmpX = x * cosZ - y * sinZ;
        double tmpY = x * sinZ + y * cosZ;

        double cosX = Math.cos(cameraAngleX);
        double sinX = Math.sin(cameraAngleX);
        double tmpZ = z * cosX - tmpY * sinX;
        tmpY = z * sinX + tmpY * cosX;

        return new double[]{tmpX, tmpY, tmpZ};
    }

    @Override
    public void update(Graphics g) {
        paint(g);
    }

    private boolean isSmallCubeOnTopOfCube(double cubeX, double cubeY) {
        boolean isInRangeX = cubeX >= min_X - 2.5 && cubeX <= max_X + 3.5;
        boolean isInRangeY = cubeY >= min_Y - 2.5 && cubeY <= max_Y + 3.5;
        boolean isInRangeX_Y = isInRangeX && isInRangeY;
        return isInRangeX_Y;
    }

    private void print_d(double cubeX, double cubeY) {
        boolean isInRangeX = cubeX >= min_X && cubeX <= max_X;
        boolean isInRangeY = cubeY >= min_Y && cubeY <= max_Y;
        boolean isInRangeX_Y = isInRangeX && isInRangeY;
        System.out.println(isInRangeX + "  " + isInRangeY + "  " + isInRangeX_Y + " " + gameFailed);
    }

    public void animateSmallCube() {
        final double gravity = 98;  // Gravity acceleration, unit: m/s^2
        final double[] fallingSpeed = {0};
        new Thread(() -> {
            while (true) {
                fallingSpeed[0] += gravity * 0.1;  // update every 0.1 second

                // Update small cube's position based on falling speed
                double newZ = smallCubeZ + fallingSpeed[0] * 0.1;  // update every 0.1 second

                if (isSmallCubeOnTopOfCube(0, 0) && newZ > 0) {
                    smallCubeZ = cameraZ;
                    break;
                } else {
                    smallCubeZ = newZ;

                    // If fall to a specific height, mark as failed
                    if (newZ > 30) {
                        gameFailed = true;
                        repaint();  // Refresh screen
                    }
                }
                repaint();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void drawGameFailedScreen(Graphics2D g2d) {
        Font font = new Font("微軟雅黑", Font.BOLD, 48);
        g2d.setFont(font);
        g2d.setColor(Color.RED);
        String message = "Failed";

        int messageWidth = g2d.getFontMetrics().stringWidth(message);
        int x = (WIDTH - messageWidth) / 2;
        int y = HEIGHT / 2;
        g2d.drawString(message, x, y);

        Font buttonFont = new Font("微軟雅黑", Font.PLAIN, 24);
        g2d.setFont(buttonFont);
        g2d.setColor(Color.WHITE);
        String restartMessage = "Restart";

        int restartWidth = g2d.getFontMetrics().stringWidth(restartMessage);
        int restartX = (WIDTH - restartWidth) / 2;
        int restartY = HEIGHT / 2 + 50;

        restartButtonBounds = new Rectangle(restartX, restartY - 24, restartWidth, 24);
        g2d.drawString(restartMessage, restartX, restartY);
    }

    public void paint(Graphics g) {
        print_d(cameraX, cameraY);
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;

        if (gameFailed) {
            drawGameFailedScreen(g2d);
        } else if (gameStarted) {
            Image offscreen = createImage(WIDTH, HEIGHT);
            Graphics2D offG2d = (Graphics2D) offscreen.getGraphics();

            offG2d.drawImage(background_pic, 0, 0, WIDTH, HEIGHT, null);

            if (smallCubeZ > 0) {
                drawCube(offG2d, 0, 0, smallCubeZ, mainCubeVertices);
                for (SmallCube cube : smallCubes) {
                    drawSmallCube(offG2d, cube.x, cube.y, cube.z, smallCubeVertices);
                }
            } else {
                for (SmallCube cube : smallCubes) {
                    drawSmallCube(offG2d, cube.x, cube.y, cube.z, smallCubeVertices);
                }
                drawCube(offG2d, 0, 0, smallCubeZ, mainCubeVertices);
            }
            g.drawImage(offscreen, 0, 0, this);
        } else {
            drawStartScreen(g2d);
        }
    }

    private void drawCube(Graphics2D g2d, double x, double y, double z, double[][] vertices) {
        draw(g2d, x, y, z, vertices, 100);
    }

    private void drawSmallCube(Graphics2D g2d, double x, double y, double z, double[][] vertices) {
        draw(g2d, x, y, z, vertices, 50);
    }

    private void draw(Graphics2D g2d, double x, double y, double z, double[][] vertices, double size) {
        double[][] transformedVertices = new double[8][];
        for (int i = 0; i < vertices.length; i++) {
            double[] v = vertices[i];
            double[] transformedV = transformPoint((v[0] + x) * size, (v[1] + y) * size, (v[2] + z));
            transformedVertices[i] = transformedV;
        }

        for (int i = 0; i < cubeFaces.length; i++) {
            int[] face = cubeFaces[i];
            Polygon poly = new Polygon();

            for (int j = 0; j < face.length; j++) {
                double[] v = transformedVertices[face[j]];
                double[] proj = projectPoint(v[0], v[1], v[2]);
                poly.addPoint((int) proj[0], (int) proj[1]);
            }
            g2d.setClip(poly);
            g2d.drawImage(images[i], poly.getBounds().x, poly.getBounds().y, poly.getBounds().width, poly.getBounds().height, null);
        }
    }

    private void drawStartScreen(Graphics2D g2d) {

        g2d.setColor(Color.BLACK);  // set background color
        g2d.fillRect(0, 0, WIDTH, HEIGHT);  // fill fullscreen

        // title
        Font titleFont = new Font("Arial", Font.BOLD, 36);
        g2d.setFont(titleFont);
        g2d.setColor(Color.WHITE);
        String title = "Simple 3D Camera Game";
        int titleWidth = g2d.getFontMetrics().stringWidth(title);
        int titleX = (WIDTH - titleWidth) / 2;
        int titleY = HEIGHT / 4;
        g2d.drawString(title, titleX, titleY);

        // start button
        Font buttonFont = new Font("Arial", Font.PLAIN, 24);
        g2d.setFont(buttonFont);
        String startMessage = "Start Game";
        int startWidth = g2d.getFontMetrics().stringWidth(startMessage);
        int startX = (WIDTH - startWidth) / 2;
        int startY = HEIGHT / 2;
        g2d.drawString(startMessage, startX, startY);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int mouseX = e.getX();
                int mouseY = e.getY();

                // Check if the mouse click is within the bounds of the start button
                if (mouseX >= startX && mouseX <= startX + startWidth &&
                        mouseY >= startY - g2d.getFontMetrics().getHeight() &&
                        mouseY <= startY) {
                    gameStarted = true;  // start the game
                    repaint();
                }
            }
        });
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (gameFailed) {
            if (keyCode == KeyEvent.VK_SPACE) {
                resetGame();
            }
            return;
        }

        if (keyCode == KeyEvent.VK_SPACE && isSmallCubeOnTopOfCube(0, 0)) {
            smallCubeZ -= moveStep * 20;
            animateSmallCube();
        } else if (keyCode == KeyEvent.VK_A) {
            for (SmallCube cube : smallCubes) {
                cube.x += moveStep;
            }
            find_X_Y();
        } else if (keyCode == KeyEvent.VK_D) {
            for (SmallCube cube : smallCubes) {
                cube.x -= moveStep;
            }
            find_X_Y();
        } else if (keyCode == KeyEvent.VK_W) {
            for (SmallCube cube : smallCubes) {
                cube.y += moveStep;
            }
            find_X_Y();
        } else if (keyCode == KeyEvent.VK_S) {
            for (SmallCube cube : smallCubes) {
                cube.y -= moveStep;
            }
            find_X_Y();
        }

        //如果離開地板的範圍則落下
        if (!isSmallCubeOnTopOfCube(0, 0)) {
            animateSmallCube();
        }
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e) {
        if (dragging) {
            int dx = e.getX() - lastMouseX;
            int dy = e.getY() - lastMouseY;
            double sensitivity = 0.01;
            cameraAngleY += dx * sensitivity;
            cameraAngleX -= dy * sensitivity;
            lastMouseX = e.getX();
            lastMouseY = e.getY();
            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {}

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        if (gameFailed) {
            Point point = e.getPoint();
            if (restartButtonBounds != null && restartButtonBounds.contains(point)) {
                resetGame();
            }
        } else {
            dragging = true;
            lastMouseX = e.getX();
            lastMouseY = e.getY();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        dragging = false;
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void windowOpened(WindowEvent e) {}

    @Override
    public void windowClosing(WindowEvent e) {
        dispose();
    }

    @Override
    public void windowClosed(WindowEvent e) {}

    @Override
    public void windowIconified(WindowEvent e) {}

    @Override
    public void windowDeiconified(WindowEvent e) {}

    @Override
    public void windowActivated(WindowEvent e) {}

    @Override
    public void windowDeactivated(WindowEvent e) {}

    private class SmallCube {
        double x, y, z;

        SmallCube(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private void resetGame() {
        cameraX = 0.0;
        cameraY = 0.0;
        cameraZ = -5.0;
        relativeCameraZ = -5.0;
        gameFailed = false;
        smallCubes.clear();
        initializeSmallCubes();
        find_X_Y();
        repaint();
    }

    public static void main(String[] args) {
        new Simple3DCamera();
    }
}
