import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Random;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.awt.image.BufferedImage;

public class GamePanel extends JPanel implements ActionListener, KeyListener {

    private static final long serialVersionUID = 1L;

    // Center the car initially (Screen width is likely around 1366 or 1920)
    int playerX = 500;
    int carX = playerX;
    int playerY = 600;
    private int groundY = playerY;

    ArrayList<Rectangle> enemies;
    Random random;
    boolean isGameOver = false;
    int score = 0;
    Timer gameLoop;
    int highScore = 0;

    BufferedImage carImg, enemyImg, roadImg;
    boolean isStartMenu = true;
    int roadY = 0;

    int velocityY = 0;
    int gravity = 2;

    public GamePanel() {
        setBackground(Color.GRAY);
        addKeyListener(this);
        setFocusable(true);

        enemies = new ArrayList<>();
        random = new Random();
        loadHighScore();

        loadImages();

        // Start Timer
        gameLoop = new Timer(30, this); // Faster timer for smoother movement
        gameLoop.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        int width = getWidth();
        int height = getHeight();
        updateGroundPosition(height);

        // 1. DRAW ROAD (Always draw this first)
        if (roadImg != null) {
            g.drawImage(roadImg, 0, roadY, width, height, null);
            g.drawImage(roadImg, 0, roadY - height, width, height, null);
        } else {
            g.setColor(Color.GRAY);
            g.fillRect(0, 0, width, height);
        }

        // 2. DRAW MENU OR GAME
        if (isStartMenu) {
            g.setColor(new Color(0, 0, 0, 150)); // Semi-transparent background
            g.fillRect(0, 0, width, height);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            g.drawString("SUPER CAR DODGE", width/2 - 250, height/2 - 50);
            g.setFont(new Font("Arial", Font.PLAIN, 25));
            g.drawString("Press ENTER to Start", width/2 - 130, height/2 + 20);
            g.drawString("Press ESC to Quit", width/2 - 110, height/2 + 60);
        } else {
            // Draw Player
            if (carImg != null) g.drawImage(carImg, carX, playerY, 50, 80, null);
            else { g.setColor(Color.RED); g.fillRect(carX, playerY, 50, 80); }

            // Draw Enemies
            for (Rectangle rect : enemies) {
                if (enemyImg != null) g.drawImage(enemyImg, rect.x, rect.y, 50, 80, null);
                else { g.setColor(Color.BLUE); g.fillRect(rect.x, rect.y, 50, 80); }
            }

            // Draw UI
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("Score: " + score, 20, 30);
            g.drawString("High Score: " + highScore, width - 200, 30);

            if (isGameOver) {
                g.setColor(Color.RED);
                g.setFont(new Font("Arial", Font.BOLD, 60));
                g.drawString("CRASH!", width/2 - 120, height/2);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 30));
                g.drawString("Press ENTER to Retry", width/2 - 150, height/2 + 60);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        playerY += velocityY; // Apply current movement
        updateGroundPosition(getHeight());
        if (playerY < groundY) {
            velocityY += gravity; // Pull down if in the air
        } else {
            playerY = groundY; // Stay on ground
            velocityY = 0;
        }

        if (isStartMenu) {
            repaint();
            return;
        }

        // 1. Move Road
        roadY += 15;
        if (roadY >= getHeight()) {
            roadY = 0;
        }

        // 2. Move Enemies & Check Collision
        Rectangle carRect = new Rectangle(carX, playerY, 50, 80);

        for (int i = 0; i < enemies.size(); i++) {
            Rectangle rect = enemies.get(i);

            // --- THIS IS THE LINE THAT MAKES THEM FALL ---
            rect.y += 15;
            // ---------------------------------------------

            // Check Collision
            if (carRect.intersects(rect)) {
                isGameOver = true;
                ((Timer)e.getSource()).stop(); // Stop the game on crash
                if (score > highScore) {
                    highScore = score;
                    saveHighScore();
                }
            }
        }

        // 3. Spawn New Enemies
        if (!isGameOver && random.nextInt(100) < 5) {
            // Define 4 Lanes
            int center = getWidth() / 2;
            int laneOffset = Math.min(150, Math.max(50, getWidth() / 5));
            int[] lanes = { center - laneOffset, center - 50, center + 50, center + laneOffset };
            int laneX = lanes[random.nextInt(4)];

            // Spawn OFF SCREEN at -100 so they drive in smoothly
            Rectangle newEnemy = new Rectangle(laneX, -100, 50, 80);

            // Overlap Check
            boolean isSafe = true;
            for (Rectangle r : enemies) {
                if (newEnemy.intersects(r) || Math.abs(newEnemy.y - r.y) < 200) {
                    isSafe = false;
                    break;
                }
            }
            if (isSafe) enemies.add(newEnemy);
        }

        // 4. Cleanup off-screen cars
        for (int i = 0; i < enemies.size(); i++) {
            if (enemies.get(i).y > getHeight()) {
                enemies.remove(i);
                i--;
                score++;
            }
        }

        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_ESCAPE) {
            Timer timer = gameLoop;
            timer.stop();
            System.exit(0);
        }

        if (isStartMenu) {
            if (code == KeyEvent.VK_ENTER) {
                isStartMenu = false;
                // Reset car to center when game starts
                carX = getWidth() / 2 - 25;
            }
        } else if (isGameOver) {
            if (code == KeyEvent.VK_ENTER) restartGame();
        } else {
            // Gameplay Controls
            if (code == KeyEvent.VK_LEFT) carX -= 30; // Faster steering
            if (code == KeyEvent.VK_RIGHT) carX += 30;

            // Boundaries (Keep car on screen)
            if (carX < 0) carX = 0;
            if (carX > getWidth() - 50) carX = getWidth() - 50;
        }
    }

    public void restartGame() {
        carX = getWidth() / 2 - 25;
        playerY = groundY;
        velocityY = 0;
        score = 0;
        isGameOver = false;
        enemies.clear();
        gameLoop.start();
    }

    private void updateGroundPosition(int height) {
        groundY = Math.max(0, height - 120);
        if (isStartMenu || isGameOver) {
            playerY = groundY;
        }
    }

    private void loadImages() {
        try {
            carImg = ImageIO.read(new File("player.png"));
            enemyImg = ImageIO.read(new File("enemy.png"));
            roadImg = ImageIO.read(new File("road.jpeg"));
        } catch (IOException e) {
            System.err.println("Unable to load game images from the project directory: " + e.getMessage());
        }
    }

    // --- High Score Helpers ---
    private void loadHighScore() {
        try {
            File f = new File("highscore.txt");
            if (f.exists()) {
                try (Scanner s = new Scanner(f)) {
                    if (s.hasNextInt()) highScore = s.nextInt();
                }
            }
        } catch (IOException e) {
            System.err.println("Unable to read highscore.txt: " + e.getMessage());
        }
    }

    private void saveHighScore() {
        try {
            try (FileWriter w = new FileWriter("highscore.txt")) {
                w.write(String.valueOf(highScore));
            }
        } catch (IOException e) {
            System.err.println("Unable to save highscore.txt: " + e.getMessage());
        }
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
}