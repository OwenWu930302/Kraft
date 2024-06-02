import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;

public class Simple3DCamera extends Frame implements KeyListener, WindowListener, MouseListener, MouseMotionListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private boolean gameStarted = false;
    private boolean gameFailed = false;
    private Rectangle restartButtonBounds;//重玩的按鈕

    private double cameraX = 0.0;
    private double cameraY = 0.0;
    private double cameraZ = -5.0;
    private double cameraAngleX = 0.0;
    private double cameraAngleY = 0.0;

    private int lastMouseX;
    private int lastMouseY;
    private boolean dragging = false;
    private BufferedImage[] images = new BufferedImage[6];

    private double smallCubeX = 0.0;
    private double smallCubeY = 0.0;
    private double smallCubeZ = 0.0;

    private double moveStep = 0.5;

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
        CubeDrawer();
    }

    public void CubeDrawer() {
        // Load images
        try {
            images[0] = ImageIO.read(new File("C:/Users/user/Desktop/grass.jpg"));
            images[1] = ImageIO.read(new File("C:/Users/user/Desktop/grass.jpg"));
            images[2] = ImageIO.read(new File("C:/Users/user/Desktop/grass.jpg"));
            images[3] = ImageIO.read(new File("C:/Users/user/Desktop/grass.jpg"));
            images[4] = ImageIO.read(new File("C:/Users/user/Desktop/grass.jpg"));
            images[5] = ImageIO.read(new File("C:/Users/user/Desktop/grass.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading images");
        }
    }

    private double[][] mainCubeVertices = {
            {-1, -1, -1},
            {-1, -1, 1},
            {-1, 1, -1},
            {-1, 1, 1},
            {1, -1, -1},
            {1, -1, 1},
            {1, 1, -1},
            {1, 1, 1}
    };

    private double[][] smallCubeVertices = {
            {-0.5, -0.5, -0.5},
            {-0.5, -0.5, 0.5},
            {-0.5, 0.5, -0.5},
            {-0.5, 0.5, 0.5},
            {0.5, -0.5, -0.5},
            {0.5, -0.5, 0.5},
            {0.5, 0.5, -0.5},
            {0.5, 0.5, 0.5}
    };

    private int[][] cubeFaces = {
            {0, 1, 3, 2}, // Front
            {4, 5, 7, 6}, // Back
            {0, 1, 5, 4}, // Bottom
            {2, 3, 7, 6}, // Top
            {0, 2, 6, 4}, // Left
            {1, 3, 7, 5}  // Right
    };

    private double[] projectPoint(double x, double y, double z) {
        // Apply perspective projection
        double fov = 256; // Field of view factor
        double scale = fov / (fov + z);
        double projectedX = x * scale + WIDTH / 2;
        double projectedY = -y * scale + HEIGHT / 2;
        return new double[]{projectedX, projectedY};
    }

    private double[] transformPoint(double x, double y, double z) {
        x -= cameraX;
        y -= cameraY;
        z -= cameraZ;

        double cosX = Math.cos(cameraAngleX);
        double sinX = Math.sin(cameraAngleX);
        double tmpY = y * cosX - z * sinX;
        double tmpZ = y * sinX + z * cosX;

        double cosY = Math.cos(cameraAngleY);
        double sinY = Math.sin(cameraAngleY);
        double tmpX = x * cosY + tmpZ * sinY;
        tmpZ = -x * sinY + tmpZ * cosY;
        return new double[]{tmpX, tmpY, tmpZ};
    }

    @Override
    public void update(Graphics g) {
        paint(g);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;

        if (gameFailed) {
            drawGameFailedScreen(g2d);
        }
        else if(gameStarted){
            Image offscreen = createImage(WIDTH, HEIGHT);
            Graphics2D offG2d = (Graphics2D) offscreen.getGraphics();

            //offG2d.setColor(Color.BLACK);
            //offG2d.fillRect(0, 0, WIDTH, HEIGHT);

            offG2d.drawImage(images[0], 0, 0, WIDTH, HEIGHT, null);

            if(smallCubeZ>0) {
                drawSmallCube(offG2d, smallCubeX, smallCubeY, smallCubeZ, smallCubeVertices); // Draw the small cube
                drawCube(offG2d, 0, 0, 0, mainCubeVertices); // Draw the main cube
            }
            else {
                drawCube(offG2d, 0, 0, 0, mainCubeVertices); // Draw the main cube
                drawSmallCube(offG2d, smallCubeX, smallCubeY, smallCubeZ, smallCubeVertices); // Draw the small cube
            }
            g.drawImage(offscreen, 0, 0, this);
        }
        else {
            drawStartScreen(g2d);
        }
    }

    private void drawStartScreen(Graphics2D g2d) {

        g2d.setColor(Color.BLACK); // 设置背景色为黑色
        g2d.fillRect(0, 0, WIDTH, HEIGHT); // 填充整个屏幕

        // 绘制标题
        Font titleFont = new Font("Arial", Font.BOLD, 36);
        g2d.setFont(titleFont);
        g2d.setColor(Color.WHITE);
        String title = "Simple 3D Camera Game";
        int titleWidth = g2d.getFontMetrics().stringWidth(title);
        int titleX = (WIDTH - titleWidth) / 2;
        int titleY = HEIGHT / 4;
        g2d.drawString(title, titleX, titleY);

        // 绘制开始按钮
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

                // 判断鼠标点击是否在开始按钮范围内
                if (mouseX >= startX && mouseX <= startX + startWidth &&
                        mouseY >= startY - g2d.getFontMetrics().getHeight() &&
                        mouseY <= startY) {
                    gameStarted = true; // 开始游戏
                    repaint(); // 重新绘制界面
                }
            }
        });
    }

    private void drawGameFailedScreen(Graphics2D g2d) {

        //遊戲失敗的畫面
        Font font = new Font("微软雅黑", Font.BOLD, 48);
        g2d.setFont(font);

        g2d.setColor(Color.RED);

        String message = "失敗!";
        int messageWidth = g2d.getFontMetrics().stringWidth(message);
        int x = (WIDTH - messageWidth) / 2;
        int y = HEIGHT / 2;
        g2d.drawString(message, x, y);

        // 绘制重玩按钮
        Font buttonFont = new Font("微软雅黑", Font.PLAIN, 24);
        g2d.setFont(buttonFont);
        g2d.setColor(Color.WHITE);
        String restartMessage = "重玩";
        int restartWidth = g2d.getFontMetrics().stringWidth(restartMessage);
        int restartX = (WIDTH - restartWidth) / 2;
        int restartY = HEIGHT / 2 + 50; // 在失败消息下方
        restartButtonBounds = new Rectangle(restartX, restartY - 24, restartWidth, 24); // 记录重玩按钮的边界框
        g2d.drawString(restartMessage, restartX, restartY);
    }

    private int size=20;
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
            double[] transformedV = transformPoint((v[0] + x)*size, (v[1] + y)*size, (v[2] + z));
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

    public void animateSmallCube() {
        final double gravity = 98; // 重力加速度，单位：m/s^2
        final double[] fallingSpeed = {0};
        new Thread(() -> {
            while (true) {
                fallingSpeed[0] += gravity * 0.1; // 假设每0.1秒更新一次速度

                // 根据下落速度更新小方块的位置
                double newZ=smallCubeZ + fallingSpeed[0] * 0.1; // 假设每0.1秒更新一次位置

                if (isSmallCubeOnTopOfCube(1.5,1.5)&&newZ>0) {
                    smallCubeZ = cameraZ;
                    break;
                } else {
                    smallCubeZ = newZ;
                    if(newZ>20) {
                        gameFailed = true;
                        repaint();
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

    private boolean isSmallCubeOnTopOfCube(double cubeX, double cubeY) {

        // 检查XY轴范围
        boolean isInRangeX = smallCubeX >= 0-cubeX  && smallCubeX <= cubeX ;
        boolean isInRangeY = smallCubeY >= 0-cubeY && smallCubeY <= cubeY;

        return isInRangeX && isInRangeY;
    }

    private boolean isSmallCubeOnTopOfCube(double cubeX, double cubeY, double cubeZ) {
        // 检查Z轴方向
        boolean isOnTop = smallCubeZ >= cubeZ && smallCubeZ <= cubeZ + 5;

        // 检查XY轴范围
        boolean isInRangeX = smallCubeX >= 0-cubeX  && smallCubeX <= cubeX ;
        boolean isInRangeY = smallCubeY >= 0-cubeY && smallCubeY <= cubeY;

        return isOnTop && isInRangeX && isInRangeY;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_SPACE && isSmallCubeOnTopOfCube(1.5,1.5,cameraZ)) {
            smallCubeZ -= moveStep * 200;
            animateSmallCube();
        } else if (keyCode == KeyEvent.VK_A) { // Replace LEFT with A
            smallCubeX -= moveStep;
        } else if (keyCode == KeyEvent.VK_D) { // Replace RIGHT with D
            smallCubeX += moveStep;
        } else if (keyCode == KeyEvent.VK_W) { // Replace UP with W
            smallCubeY += moveStep;
        } else if (keyCode == KeyEvent.VK_S) { // Replace DOWN with S
            smallCubeY -= moveStep;
        } else if (keyCode == KeyEvent.VK_SPACE && gameFailed) { // Add condition for spacebar when game failed
            // Restart the game
            gameFailed = false;
            smallCubeX = 0.0;
            smallCubeY = 0.0;
            smallCubeZ = 0.0;
            repaint();
        }

        if(!isSmallCubeOnTopOfCube(1.5,1.5)) {
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
            cameraAngleX += dy * sensitivity;

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
        dragging = true;
        lastMouseX = e.getX();
        lastMouseY = e.getY();

        // 检查是否点击了重玩按钮
        if (gameFailed && restartButtonBounds.contains(lastMouseX, lastMouseY)) {
            // 点击了重玩按钮，重置游戏状态
            gameFailed = false;
            smallCubeX = 0.0;
            smallCubeY = 0.0;
            smallCubeZ = 0.0;
            repaint();
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
    public void windowClosing(WindowEvent e) {
        dispose();
        System.exit(0);
    }

    @Override
    public void windowOpened(WindowEvent e) {}

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

    public static void main(String[] args) {
        new Simple3DCamera();
    }
}
