import java.awt.*;
import java.awt.event.*;

public class Simple3DCamera extends Frame implements KeyListener, WindowListener, MouseListener, MouseMotionListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private double cameraX = 0.0;
    private double cameraY = 0.0;
    private double cameraZ = -5.0;
    private double cameraAngleX = 0.0;
    private double cameraAngleY = 0.0;

    private int lastMouseX;
    private int lastMouseY;
    private boolean dragging = false;

    private double[][] cubeVertices = {
            {-1, -1, -1},
            {-1, -1, 1},
            {-1, 1, -1},
            {-1, 1, 1},
            {1, -1, -1},
            {1, -1, 1},
            {1, 1, -1},
            {1, 1, 1}
    };

    private int[][] cubeFaces = {
            {0, 1, 3, 2}, // 前面
            {4, 5, 7, 6}, // 后面
            {0, 1, 5, 4}, // 底面
            {2, 3, 7, 6}, // 上面
            {0, 2, 6, 4}, // 左面
            {1, 3, 7, 5}  // 右面
    };

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
    }

    private double[] projectPoint(double x, double y, double z) {
        double fov = 500; // 视野距离
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

    public void update(Graphics g) {
        paint(g);
    }

    public void paint(Graphics g) {
        Image offscreen = createImage(WIDTH, HEIGHT);
        Graphics2D g2d = (Graphics2D) offscreen.getGraphics();

        // 清空画布，设置背景为黑色
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // 绘制3D平台，平台上的立方体为白色
        int platformWidth = 30;
        int platformHeight = 10;
        int platformDepth = 30;
        g2d.setColor(Color.WHITE);
        for (int x = 0; x < platformWidth; x++) {
            for (int y = 0; y < platformHeight; y++) {
                for (int z = 0; z < platformDepth; z++) {
                    drawCube(g2d, x * 2 - platformWidth + 1, y * 2 - platformHeight, z * 2 - platformDepth + 1);
                }
            }
        }

        g.drawImage(offscreen, 0, 0, this);
    }

    private void drawCube(Graphics2D g2d, double x, double y, double z) {
        double[][] transformedVertices = new double[8][];
        for (int i = 0; i < cubeVertices.length; i++) {
            double[] v = cubeVertices[i];
            transformedVertices[i] = transformPoint(v[0] + x, v[1] + y, v[2] + z);
        }

        for (int i = 0; i < cubeFaces.length; i++) {
            int[] face = cubeFaces[i];
            Polygon poly = new Polygon();
            for (int j : face) {
                double[] v = transformedVertices[j];
                double[] proj = projectPoint(v[0], v[1], v[2]);
                poly.addPoint((int) proj[0], (int) proj[1]);
            }
            g2d.fillPolygon(poly);
        }
    }

    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        double moveStep = 0.1;
        double rotateStep = 0.05;
        if (keyCode == KeyEvent.VK_W) {
            cameraZ += moveStep;
        } else if (keyCode == KeyEvent.VK_S) {
            cameraZ -= moveStep;
        } else if (keyCode == KeyEvent.VK_A) {
            cameraX -= moveStep;
        } else if (keyCode == KeyEvent.VK_D) {
            cameraX += moveStep;
        } else if (keyCode == KeyEvent.VK_UP) {
            cameraAngleX -= rotateStep;
        } else if (keyCode == KeyEvent.VK_DOWN) {
            cameraAngleX += rotateStep;
        } else if (keyCode == KeyEvent.VK_LEFT) {
            cameraAngleY -= rotateStep;
        } else if (keyCode == KeyEvent.VK_RIGHT) {
            cameraAngleY += rotateStep;
        }
        repaint();
    }

    public void keyReleased(KeyEvent e) {}

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
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        dragging = true;
        lastMouseX = e.getX();
        lastMouseY = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        dragging = false;
    }

    public void windowClosing(WindowEvent e) {
        dispose();
        System.exit(0);
    }

    public void windowOpened(WindowEvent e) {}

    public void windowClosed(WindowEvent e) {}

    public void windowIconified(WindowEvent e) {}

    public void windowDeiconified(WindowEvent e) {}

    public void windowActivated(WindowEvent e) {}

    public void windowDeactivated(WindowEvent e) {}

    public static void main(String[] args) {
        Simple3DCamera camera = new Simple3DCamera();
    }
}
