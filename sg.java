
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class sg extends JPanel implements ActionListener {

    private final int WIDTH = 800;
    private final int HEIGHT = 600;
    private final int PLAY_HEIGHT = 540;
    private final int BLOCK_SIZE = 30;

    private final int NUM_BLOCKS_X = WIDTH / BLOCK_SIZE;
    private final int NUM_BLOCKS_Y = PLAY_HEIGHT / BLOCK_SIZE;

    private LinkedList<Point> snake = new LinkedList<>();
    private Point food;
    private List<Point> obstacles = new ArrayList<>();

    private int direction = KeyEvent.VK_RIGHT;
    private int score = 0;
    private int coins = 0;
    private int highestScore = 0;
    private int coinsAtGameStart = 0;
    private int coinsEarned = 0;

    private boolean gameOver = false;
    private boolean paused = false;
    private boolean hasSave = false;
    private boolean returnToGame = false;
    private boolean newRecord = false;

    private String screen = "MENU";
    private String difficulty = "Normal";

    private javax.swing.Timer timer;

    private String selectedSnake = "Green";
    private String selectedFruit = "Red";

    private Set<String> unlockedSnakes = new HashSet<>();
    private Set<String> unlockedFruits = new HashSet<>();

    private final String[] snakeColors = {
        "Green", "Blue", "Purple", "Yellow", "Golden"
    };

    private final String[] fruitColors = {
        "Red", "Blue", "Orange", "Purple", "Rainbow"
    };

    private final String SAVE_FILE =
        System.getProperty("user.home") +
        File.separator + "snake_game_save.properties";

    public sg() {

        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(15, 25, 35));
        setFocusable(true);

        unlockedSnakes.add("Green");
        unlockedFruits.add("Red");

        loadData();

        timer = new javax.swing.Timer(getSpeed(), this);

        addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {

                int key = e.getKeyCode();

                if (!screen.equals("GAME") || paused || gameOver) {
                    return;
                }

                if (key == KeyEvent.VK_ESCAPE) {
                    pauseGame();
                    return;
                }

                if ((key == KeyEvent.VK_LEFT ||
                     key == KeyEvent.VK_RIGHT ||
                     key == KeyEvent.VK_UP ||
                     key == KeyEvent.VK_DOWN) &&
                     Math.abs(key - direction) != 2) {

                    direction = key;
                    saveData();
                }
            }
        });

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e.getX(), e.getY());
                requestFocusInWindow();
            }
        });
    }

    // DIFFICULTY

    private int getSpeed() {

        switch (difficulty) {
            case "Medium":
                return 100;

            case "Difficult":
                return 65;

            default:
                return 150;
        }
    }

    // START NEW GAME

    private void startNewGame() {

        snake.clear();

        snake.add(new Point(
            NUM_BLOCKS_X / 2,
            NUM_BLOCKS_Y / 2
        ));

        direction = KeyEvent.VK_RIGHT;

        score = 0;
        coinsEarned = 0;
        coinsAtGameStart = coins;

        gameOver = false;
        paused = false;
        newRecord = false;
        hasSave = true;

        generateFood();
        generateObstacles();

        timer.setDelay(getSpeed());

        screen = "GAME";
        timer.start();

        saveData();
        repaint();
    }

    // GENERATE FOOD

    private void generateFood() {

        Random random = new Random();

        do {
            food = new Point(
                random.nextInt(NUM_BLOCKS_X),
                random.nextInt(NUM_BLOCKS_Y)
            );

        } while (
            snake.contains(food) ||
            obstacles.contains(food)
        );
    }

    // GENERATE STONES

    private void generateObstacles() {

        obstacles.clear();

        Random random = new Random();

        for (int i = 0; i < 8; i++) {

            Point obstacle;

            do {
                obstacle = new Point(
                    random.nextInt(NUM_BLOCKS_X),
                    random.nextInt(NUM_BLOCKS_Y)
                );

            } while (
                snake.contains(obstacle) ||
                obstacle.equals(food) ||
                obstacles.contains(obstacle)
            );

            obstacles.add(obstacle);
        }
    }

    // MOVE SNAKE

    private void moveSnake() {

        Point head = snake.getFirst();
        Point newHead = new Point(head);

        switch (direction) {

            case KeyEvent.VK_LEFT:
                newHead.x--;
                break;

            case KeyEvent.VK_RIGHT:
                newHead.x++;
                break;

            case KeyEvent.VK_UP:
                newHead.y--;
                break;

            case KeyEvent.VK_DOWN:
                newHead.y++;
                break;
        }

        // Wall crossing

        if (newHead.x < 0) {
            newHead.x = NUM_BLOCKS_X - 1;
        } else if (newHead.x >= NUM_BLOCKS_X) {
            newHead.x = 0;
        }

        if (newHead.y < 0) {
            newHead.y = NUM_BLOCKS_Y - 1;
        } else if (newHead.y >= NUM_BLOCKS_Y) {
            newHead.y = 0;
        }

        // Stop at stones

        if (obstacles.contains(newHead)) {
            return;
        }

        // Self collision

        if (snake.contains(newHead)) {

            gameOver = true;
            timer.stop();

            hasSave = false;

            coinsEarned = coins - coinsAtGameStart;

            newRecord = score > highestScore;

            if (newRecord) {
                highestScore = score;
            }

            screen = "GAME_OVER";

            saveData();
            repaint();
            return;
        }

        snake.addFirst(newHead);

        // Eat fruit and grow

        if (newHead.equals(food)) {

            score += 10;
            coins += 10;

            generateFood();

            if (score % 30 == 0) {
                generateObstacles();
            }

        } else {
            snake.removeLast();
        }

        saveData();
        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (screen.equals("GAME") &&
            !paused && !gameOver) {

            moveSnake();
        }
    }

    // PAUSE AND RESUME

    private void pauseGame() {

        paused = true;
        timer.stop();

        saveData();
        repaint();
    }

    private void resumeGame() {

        paused = false;

        timer.setDelay(getSpeed());
        timer.start();

        repaint();
        requestFocusInWindow();
    }

    // SHOP

    private void openShop(boolean fromGame) {

        returnToGame = fromGame;

        if (fromGame) {
            pauseGame();
        }

        screen = "SHOP";
        repaint();
    }

    private int getPrice(String name) {

        switch (name) {

            case "Blue":
                return 50;

            case "Purple":
                return 100;

            case "Yellow":
                return 150;

            case "Golden":
                return 200;

            case "Orange":
                return 75;

            case "Rainbow":
                return 200;

            default:
                return 50;
        }
    }

    private void buyOrSelectSnake(String name) {

        if (!unlockedSnakes.contains(name)) {

            int price = getPrice(name);

            if (coins >= price) {

                coins -= price;
                unlockedSnakes.add(name);
                selectedSnake = name;
            }

        } else {
            selectedSnake = name;
        }

        saveData();
        repaint();
    }

    private void buyOrSelectFruit(String name) {

        if (!unlockedFruits.contains(name)) {

            int price = getPrice(name);

            if (coins >= price) {

                coins -= price;
                unlockedFruits.add(name);
                selectedFruit = name;
            }

        } else {
            selectedFruit = name;
        }

        saveData();
        repaint();
    }

    // SNAKE COLOURS

    private Color getSnakeColor() {

        switch (selectedSnake) {

            case "Blue":
                return new Color(30, 144, 255);

            case "Purple":
                return new Color(160, 70, 220);

            case "Yellow":
                return Color.YELLOW;

            case "Golden":
                return new Color(255, 190, 0);

            default:
                return new Color(50, 205, 50);
        }
    }

    // FRUIT COLOURS

    private Color getFruitColor() {

        switch (selectedFruit) {

            case "Blue":
                return new Color(40, 130, 255);

            case "Orange":
                return Color.ORANGE;

            case "Purple":
                return new Color(180, 70, 230);

            case "Rainbow":
                return Color.MAGENTA;

            default:
                return Color.RED;
        }
    }

    // SAVE GAME

    private void saveData() {

        Properties p = new Properties();

        p.setProperty("coins", String.valueOf(coins));
        p.setProperty("highestScore", String.valueOf(highestScore));
        p.setProperty("difficulty", difficulty);

        p.setProperty("selectedSnake", selectedSnake);
        p.setProperty("selectedFruit", selectedFruit);

        p.setProperty(
            "unlockedSnakes",
            String.join(",", unlockedSnakes)
        );

        p.setProperty(
            "unlockedFruits",
            String.join(",", unlockedFruits)
        );

        boolean saveCurrentGame =
            hasSave && !gameOver && !snake.isEmpty();

        p.setProperty(
            "hasSave",
            String.valueOf(saveCurrentGame)
        );

        if (saveCurrentGame) {

            p.setProperty(
                "snakeCount",
                String.valueOf(snake.size())
            );

            for (int i = 0; i < snake.size(); i++) {

                p.setProperty(
                    "snakeX" + i,
                    String.valueOf(snake.get(i).x)
                );

                p.setProperty(
                    "snakeY" + i,
                    String.valueOf(snake.get(i).y)
                );
            }

            p.setProperty("score", String.valueOf(score));
            p.setProperty("direction", String.valueOf(direction));

            p.setProperty("foodX", String.valueOf(food.x));
            p.setProperty("foodY", String.valueOf(food.y));

            p.setProperty(
                "obstacleCount",
                String.valueOf(obstacles.size())
            );

            for (int i = 0; i < obstacles.size(); i++) {

                p.setProperty(
                    "obstacleX" + i,
                    String.valueOf(obstacles.get(i).x)
                );

                p.setProperty(
                    "obstacleY" + i,
                    String.valueOf(obstacles.get(i).y)
                );
            }
        }

        try (FileOutputStream out =
                 new FileOutputStream(SAVE_FILE)) {

            p.store(out, "Snake Game Save");

        } catch (IOException ex) {

            System.out.println(
                "Could not save game: " + ex.getMessage()
            );
        }
    }

    // LOAD GAME

    private void loadData() {

        File file = new File(SAVE_FILE);

        if (!file.exists()) {
            return;
        }

        Properties p = new Properties();

        try (FileInputStream in =
                 new FileInputStream(file)) {

            p.load(in);

            coins = Integer.parseInt(
                p.getProperty("coins", "0")
            );

            highestScore = Integer.parseInt(
                p.getProperty("highestScore", "0")
            );

            difficulty = p.getProperty(
                "difficulty", "Normal"
            );

            selectedSnake = p.getProperty(
                "selectedSnake", "Green"
            );

            selectedFruit = p.getProperty(
                "selectedFruit", "Red"
            );

            unlockedSnakes.clear();
            unlockedFruits.clear();

            unlockedSnakes.addAll(
                Arrays.asList(
                    p.getProperty(
                        "unlockedSnakes", "Green"
                    ).split(",")
                )
            );

            unlockedFruits.addAll(
                Arrays.asList(
                    p.getProperty(
                        "unlockedFruits", "Red"
                    ).split(",")
                )
            );

            hasSave = Boolean.parseBoolean(
                p.getProperty("hasSave", "false")
            );

            if (hasSave) {

                snake.clear();

                int count = Integer.parseInt(
                    p.getProperty("snakeCount", "1")
                );

                for (int i = 0; i < count; i++) {

                    int x = Integer.parseInt(
                        p.getProperty("snakeX" + i)
                    );

                    int y = Integer.parseInt(
                        p.getProperty("snakeY" + i)
                    );

                    snake.add(new Point(x, y));
                }

                score = Integer.parseInt(
                    p.getProperty("score", "0")
                );

                direction = Integer.parseInt(
                    p.getProperty(
                        "direction",
                        String.valueOf(KeyEvent.VK_RIGHT)
                    )
                );

                food = new Point(
                    Integer.parseInt(p.getProperty("foodX")),
                    Integer.parseInt(p.getProperty("foodY"))
                );

                obstacles.clear();

                int obstacleCount = Integer.parseInt(
                    p.getProperty("obstacleCount", "0")
                );

                for (int i = 0; i < obstacleCount; i++) {

                    int x = Integer.parseInt(
                        p.getProperty("obstacleX" + i)
                    );

                    int y = Integer.parseInt(
                        p.getProperty("obstacleY" + i)
                    );

                    obstacles.add(new Point(x, y));
                }
            }

        } catch (Exception ex) {

            System.out.println(
                "Could not load saved game: " + ex.getMessage()
            );

            hasSave = false;
        }
    }

    // CONTINUE GAME

    private void continueGame() {

        if (!hasSave) {
            return;
        }

        loadData();

        coinsAtGameStart = coins;

        screen = "GAME";
        gameOver = false;
        paused = false;

        timer.setDelay(getSpeed());
        timer.start();

        repaint();
        requestFocusInWindow();
    }

    // MOUSE CLICK HANDLING

    private void handleMouseClick(int x, int y) {

        if (screen.equals("MENU")) {

            if (y >= 210 && y <= 260) {

                if (x >= 100 && x <= 290) {
                    difficulty = "Normal";
                } else if (x >= 305 && x <= 495) {
                    difficulty = "Medium";
                } else if (x >= 510 && x <= 700) {
                    difficulty = "Difficult";
                }

                saveData();
                repaint();
            }

            if (inside(x, y, 300, 300, 200, 55)) {
                startNewGame();
            }

            if (inside(x, y, 300, 370, 200, 55) && hasSave) {
                continueGame();
            }

            if (inside(x, y, 300, 440, 200, 55)) {
                openShop(false);
            }

            if (inside(x, y, 300, 510, 200, 55)) {
                timer.stop();
                System.exit(0);
            }
        }

        else if (screen.equals("GAME")) {

            if (!paused) {

                if (inside(x, y, 585, 548, 100, 42)) {
                    pauseGame();
                }

                if (inside(x, y, 695, 548, 90, 42)) {

                    pauseGame();
                    screen = "MENU";
                    paused = false;
                    repaint();
                }

            } else {

                if (inside(x, y, 300, 280, 200, 50)) {
                    resumeGame();
                }

                if (inside(x, y, 300, 345, 200, 50)) {
                    openShop(true);
                }

                if (inside(x, y, 300, 410, 200, 50)) {

                    saveData();
                    timer.stop();

                    screen = "MENU";
                    paused = false;

                    repaint();
                }

                if (inside(x, y, 300, 475, 200, 50)) {

                    saveData();
                    timer.stop();

                    System.exit(0);
                }
            }
        }

        else if (screen.equals("SHOP")) {

            if (inside(x, y, 25, 25, 115, 42)) {

                if (returnToGame) {
                    screen = "GAME";
                    resumeGame();
                } else {
                    screen = "MENU";
                    repaint();
                }

                return;
            }

            for (int i = 0; i < snakeColors.length; i++) {

                int bx = 30 + i * 150;

                if (inside(x, y, bx, 150, 140, 160)) {
                    buyOrSelectSnake(snakeColors[i]);
                    return;
                }
            }

            for (int i = 0; i < fruitColors.length; i++) {

                int bx = 30 + i * 150;

                if (inside(x, y, bx, 370, 140, 160)) {
                    buyOrSelectFruit(fruitColors[i]);
                    return;
                }
            }
        }

        else if (screen.equals("GAME_OVER")) {

            if (inside(x, y, 300, 425, 200, 50)) {
                startNewGame();
            }

            if (inside(x, y, 300, 490, 200, 50)) {

                screen = "MENU";
                repaint();
            }
        }

        repaint();
    }

    private boolean inside(
        int x, int y, int bx, int by, int w, int h
    ) {

        return x >= bx && x <= bx + w &&
               y >= by && y <= by + h;
    }

    // BUTTON DESIGN

    private void drawButton(
        Graphics g,
        String text,
        int x, int y,
        int w, int h
    ) {

        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(new Color(40, 65, 75));
        g2.fillRoundRect(x, y, w, h, 18, 18);

        g2.setColor(new Color(80, 220, 170));
        g2.drawRoundRect(x, y, w, h, 18, 18);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 18));

        FontMetrics fm = g2.getFontMetrics();

        int tx = x + (w - fm.stringWidth(text)) / 2;
        int ty = y + (h + fm.getAscent()) / 2 - 5;

        g2.drawString(text, tx, ty);
    }

    private void drawDifficultyButton(
        Graphics g, String name,
        int x, int y, int w, int h
    ) {

        Graphics2D g2 = (Graphics2D) g;

        boolean selected = difficulty.equals(name);

        g2.setColor(
            selected
                ? new Color(30, 130, 100)
                : new Color(40, 65, 75)
        );

        g2.fillRoundRect(x, y, w, h, 18, 18);

        g2.setColor(
            selected
                ? Color.WHITE
                : new Color(80, 220, 170)
        );

        g2.drawRoundRect(x, y, w, h, 18, 18);

        g2.setFont(new Font("Arial", Font.BOLD, 17));

        FontMetrics fm = g2.getFontMetrics();

        g2.drawString(
            name.toUpperCase(),
            x + (w - fm.stringWidth(name.toUpperCase())) / 2,
            y + (h + fm.getAscent()) / 2 - 5
        );
    }

    // MAIN MENU

    private void drawMenu(Graphics g) {

        Graphics2D g2 = (Graphics2D) g;

        g2.setPaint(new GradientPaint(
            0, 0, new Color(10, 20, 35),
            WIDTH, HEIGHT, new Color(25, 65, 65)
        ));

        g2.fillRect(0, 0, WIDTH, HEIGHT);

        g2.setColor(new Color(70, 220, 150));
        g2.setFont(new Font("Arial", Font.BOLD, 58));

        g2.drawString("SNAKE GAME", 225, 110);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 19));

        g2.drawString(
            "EAT  •  GROW  •  SURVIVE",
            290, 150
        );

        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString("SELECT DIFFICULTY", 300, 190);

        drawDifficultyButton(g, "Normal", 100, 210, 190, 50);
        drawDifficultyButton(g, "Medium", 305, 210, 190, 50);
        drawDifficultyButton(g, "Difficult", 510, 210, 190, 50);

        g2.setColor(Color.YELLOW);
        g2.setFont(new Font("Arial", Font.BOLD, 17));

        g2.drawString("COINS: " + coins, 660, 40);

        drawButton(g, "PLAY GAME", 300, 300, 200, 55);

        drawButton(
            g,
            hasSave ? "CONTINUE GAME" : "NO SAVED GAME",
            300, 370, 200, 55
        );

        drawButton(g, "SNAKE SHOP", 300, 440, 200, 55);
        drawButton(g, "QUIT", 300, 510, 200, 55);
    }

    // GAME SCREEN

    private void drawGame(Graphics g) {

        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(new Color(18, 27, 32));
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        g2.setColor(new Color(30, 42, 43));
        g2.fillRect(0, 0, WIDTH, PLAY_HEIGHT);

        g2.setColor(Color.ORANGE);
        g2.drawRect(0, 0, WIDTH - 1, PLAY_HEIGHT - 1);

        // Stones

        for (Point o : obstacles) {

            int ox = o.x * BLOCK_SIZE;
            int oy = o.y * BLOCK_SIZE;

            g2.setColor(new Color(150, 100, 65));

            g2.fill3DRect(
                ox, oy,
                BLOCK_SIZE, BLOCK_SIZE,
                true
            );

            g2.setColor(new Color(80, 40, 10));

            g2.drawRect(
                ox, oy,
                BLOCK_SIZE, BLOCK_SIZE
            );
        }

        // Snake body

        Color snakeColor = getSnakeColor();

        for (int i = 1; i < snake.size(); i++) {

            Point p = snake.get(i);

            g2.setColor(snakeColor);

            g2.fillRoundRect(
                p.x * BLOCK_SIZE,
                p.y * BLOCK_SIZE,
                BLOCK_SIZE,
                BLOCK_SIZE,
                12, 12
            );
        }

        // Snake head

        Point head = snake.getFirst();

        int hx = head.x * BLOCK_SIZE;
        int hy = head.y * BLOCK_SIZE;

        g2.setColor(snakeColor);

        g2.fillRoundRect(
            hx, hy,
            BLOCK_SIZE, BLOCK_SIZE,
            12, 12
        );

        // Eyes

        g2.setColor(Color.WHITE);

        g2.fillOval(hx + 6, hy + 6, 8, 8);
        g2.fillOval(hx + 16, hy + 6, 8, 8);

        g2.setColor(Color.BLACK);

        g2.fillOval(hx + 9, hy + 8, 4, 4);
        g2.fillOval(hx + 19, hy + 8, 4, 4);

        // Fruit

        int fx = food.x * BLOCK_SIZE;
        int fy = food.y * BLOCK_SIZE;

        g2.setColor(getFruitColor());

        g2.fillOval(
            fx + 2, fy + 2,
            BLOCK_SIZE - 4, BLOCK_SIZE - 4
        );

        g2.setColor(Color.WHITE);
        g2.fillOval(fx + 7, fy + 7, 7, 7);

        g2.setColor(Color.GREEN);
        g2.fillRect(fx + 14, fy - 3, 4, 8);

        // Bottom control bar

        g2.setColor(new Color(10, 18, 27));

        g2.fillRect(
            0, PLAY_HEIGHT,
            WIDTH, HEIGHT - PLAY_HEIGHT
        );

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 17));

        g2.drawString("SCORE: " + score, 25, 575);
        g2.drawString("COINS: " + coins, 170, 575);
        g2.drawString("LEVEL: " + difficulty, 320, 575);

        drawButton(g, "PAUSE", 585, 548, 100, 42);
        drawButton(g, "QUIT", 695, 548, 90, 42);
    }

    // PAUSE SCREEN

    private void drawPause(Graphics g) {

        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(new Color(0, 0, 0, 220));
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        g2.setColor(new Color(70, 220, 150));
        g2.setFont(new Font("Arial", Font.BOLD, 55));

        g2.drawString("GAME PAUSED", 220, 170);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 20));

        g2.drawString(
            "Your progress has been saved",
            270, 215
        );

        drawButton(g, "RESUME", 300, 280, 200, 50);
        drawButton(g, "SNAKE SHOP", 300, 345, 200, 50);
        drawButton(g, "MAIN MENU", 300, 410, 200, 50);
        drawButton(g, "QUIT GAME", 300, 475, 200, 50);
    }

    // PROFESSIONAL SHOP

    private Color getShopSnakeColor(String name) {

        switch (name) {
            case "Blue":
                return new Color(55, 145, 255);

            case "Purple":
                return new Color(174, 100, 245);

            case "Yellow":
                return new Color(255, 215, 65);

            case "Golden":
                return new Color(235, 170, 35);

            default:
                return new Color(55, 205, 125);
        }
    }

    private Color getShopFruitColor(String name) {

        switch (name) {
            case "Blue":
                return new Color(55, 145, 255);

            case "Orange":
                return new Color(255, 150, 45);

            case "Purple":
                return new Color(180, 100, 235);

            case "Rainbow":
                return new Color(235, 85, 180);

            default:
                return new Color(245, 75, 80);
        }
    }

    private void drawShopCard(
        Graphics2D g2,
        int x, int y, int w, int h,
        String name,
        boolean snakeItem
    ) {

        boolean owned = snakeItem
            ? unlockedSnakes.contains(name)
            : unlockedFruits.contains(name);

        boolean selected = snakeItem
            ? selectedSnake.equals(name)
            : selectedFruit.equals(name);

        Color accent = snakeItem
            ? getShopSnakeColor(name)
            : getShopFruitColor(name);

        g2.setColor(new Color(24, 39, 57));
        g2.fillRoundRect(x, y, w, h, 18, 18);

        g2.setColor(
            selected
                ? new Color(75, 220, 165)
                : new Color(55, 75, 98)
        );

        g2.setStroke(new BasicStroke(selected ? 2.5f : 1.2f));
        g2.drawRoundRect(x, y, w, h, 18, 18);

        g2.setColor(new Color(15, 27, 42));
        g2.fillRoundRect(x + 9, y + 9, w - 18, 62, 13, 13);

        // Preview

        if (snakeItem) {

            g2.setColor(accent);

            g2.fillRoundRect(x + 39, y + 30, 25, 17, 8, 8);
            g2.fillRoundRect(x + 61, y + 30, 25, 17, 8, 8);
            g2.fillRoundRect(x + 83, y + 30, 25, 17, 8, 8);

            g2.setColor(Color.WHITE);
            g2.fillOval(x + 100, y + 33, 5, 5);
            g2.fillOval(x + 100, y + 41, 5, 5);

        } else {

            g2.setColor(accent);
            g2.fillOval(x + 55, y + 19, 34, 34);

            g2.setColor(new Color(255, 255, 255, 150));
            g2.fillOval(x + 62, y + 25, 8, 8);

            g2.setColor(new Color(85, 190, 105));
            g2.setStroke(new BasicStroke(3f));
            g2.drawLine(x + 73, y + 21, x + 80, y + 12);
        }

        // Name

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 15));

        FontMetrics fm = g2.getFontMetrics();

        g2.drawString(
            name,
            x + (w - fm.stringWidth(name)) / 2,
            y + 94
        );

        // Price or ownership

        g2.setFont(new Font("Arial", Font.PLAIN, 12));

        String detail = owned
            ? (selected ? "CURRENTLY EQUIPPED" : "OWNED")
            : getPrice(name) + " COINS";

        g2.setColor(
            selected
                ? new Color(100, 235, 180)
                : new Color(185, 200, 215)
        );

        fm = g2.getFontMetrics();

        g2.drawString(
            detail,
            x + (w - fm.stringWidth(detail)) / 2,
            y + 113
        );

        // Action button

        g2.setColor(
            selected
                ? new Color(35, 130, 100)
                : owned
                    ? new Color(45, 85, 125)
                    : new Color(35, 145, 125)
        );

        g2.fillRoundRect(x + 13, y + 122, w - 26, 27, 10, 10);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 11));

        String action = selected
            ? "EQUIPPED"
            : owned
                ? "EQUIP"
                : (coins >= getPrice(name)
                    ? "BUY NOW"
                    : "NOT ENOUGH COINS");

        fm = g2.getFontMetrics();

        g2.drawString(
            action,
            x + (w - fm.stringWidth(action)) / 2,
            y + 140
        );
    }

    private void drawShop(Graphics g) {

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setPaint(new GradientPaint(
            0, 0, new Color(9, 19, 34),
            WIDTH, HEIGHT, new Color(22, 48, 65)
        ));

        g2.fillRect(0, 0, WIDTH, HEIGHT);

        drawButton(g2, "BACK", 25, 25, 115, 42);

        g2.setColor(new Color(75, 230, 175));
        g2.setFont(new Font("Arial", Font.BOLD, 36));

        String title = "GAME STORE";
        FontMetrics fm = g2.getFontMetrics();

        g2.drawString(
            title,
            (WIDTH - fm.stringWidth(title)) / 2,
            62
        );

        g2.setColor(new Color(27, 48, 67));
        g2.fillRoundRect(610, 22, 165, 48, 15, 15);

        g2.setColor(new Color(255, 205, 75));
        g2.setFont(new Font("Arial", Font.BOLD, 17));

        g2.drawString("COINS  " + coins, 635, 52);

        g2.setColor(new Color(185, 205, 220));
        g2.setFont(new Font("Arial", Font.PLAIN, 13));

        String subtitle = "CUSTOMIZE YOUR SNAKE AND FRUIT";
        fm = g2.getFontMetrics();

        g2.drawString(
            subtitle,
            (WIDTH - fm.stringWidth(subtitle)) / 2,
            91
        );

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 19));

        g2.drawString("SNAKE SKINS", 31, 132);

        g2.setColor(new Color(75, 220, 170));
        g2.fillRoundRect(31, 140, 738, 3, 3, 3);

        for (int i = 0; i < snakeColors.length; i++) {

            drawShopCard(
                g2,
                30 + i * 150,
                150,
                140,
                160,
                snakeColors[i],
                true
            );
        }

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 19));

        g2.drawString("FRUIT SKINS", 31, 352);

        g2.setColor(new Color(75, 220, 170));
        g2.fillRoundRect(31, 360, 738, 3, 3, 3);

        for (int i = 0; i < fruitColors.length; i++) {

            drawShopCard(
                g2,
                30 + i * 150,
                370,
                140,
                160,
                fruitColors[i],
                false
            );
        }

        g2.setColor(new Color(150, 170, 190));
        g2.setFont(new Font("Arial", Font.PLAIN, 11));

        String hint = "Select a card to buy or equip a skin";
        fm = g2.getFontMetrics();

        g2.drawString(
            hint,
            (WIDTH - fm.stringWidth(hint)) / 2,
            565
        );

        g2.dispose();
    }

    // GAME OVER RESULTS PAGE

    private void drawGameOver(Graphics g) {

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );

        // Background

        g2.setPaint(new GradientPaint(
            0, 0, new Color(9, 18, 34),
            WIDTH, HEIGHT, new Color(25, 53, 70)
        ));

        g2.fillRect(0, 0, WIDTH, HEIGHT);

        // Main panel

        g2.setColor(new Color(25, 42, 60));
        g2.fillRoundRect(155, 35, 490, 530, 30, 30);

        g2.setColor(new Color(65, 95, 115));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(155, 35, 490, 530, 30, 30);

        // Game over heading

        g2.setColor(new Color(255, 105, 115));
        g2.setFont(new Font("Arial", Font.BOLD, 42));

        String heading = "GAME OVER!";
        FontMetrics fm = g2.getFontMetrics();

        g2.drawString(
            heading,
            (WIDTH - fm.stringWidth(heading)) / 2,
            105
        );

        g2.setColor(new Color(190, 205, 220));
        g2.setFont(new Font("Arial", Font.PLAIN, 16));

        String subtitle = "Here are your game results";
        fm = g2.getFontMetrics();

        g2.drawString(
            subtitle,
            (WIDTH - fm.stringWidth(subtitle)) / 2,
            137
        );

        // Score cards

        g2.setColor(new Color(35, 60, 82));
        g2.fillRoundRect(195, 170, 190, 120, 20, 20);
        g2.fillRoundRect(415, 170, 190, 120, 20, 20);

        g2.setColor(new Color(100, 195, 255));
        g2.setFont(new Font("Arial", Font.BOLD, 15));

        g2.drawString("YOUR SCORE", 245, 205);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 43));

        String currentScore = String.valueOf(score);
        fm = g2.getFontMetrics();

        g2.drawString(
            currentScore,
            290 - fm.stringWidth(currentScore) / 2,
            260
        );

        g2.setColor(new Color(255, 205, 85));
        g2.setFont(new Font("Arial", Font.BOLD, 15));

        g2.drawString("HIGHEST SCORE", 450, 205);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 43));

        String bestScore = String.valueOf(highestScore);
        fm = g2.getFontMetrics();

        g2.drawString(
            bestScore,
            510 - fm.stringWidth(bestScore) / 2,
            260
        );

        // New record message

        if (newRecord) {

            g2.setColor(new Color(255, 210, 85));
            g2.setFont(new Font("Arial", Font.BOLD, 22));

            String record = "NEW HIGH SCORE!";
            fm = g2.getFontMetrics();

            g2.drawString(
                record,
                (WIDTH - fm.stringWidth(record)) / 2,
                330
            );

        } else {

            g2.setColor(new Color(180, 205, 220));
            g2.setFont(new Font("Arial", Font.PLAIN, 15));

            String message = "Keep playing to beat your record!";
            fm = g2.getFontMetrics();

            g2.drawString(
                message,
                (WIDTH - fm.stringWidth(message)) / 2,
                330
            );
        }

        // Coins earned

        g2.setColor(new Color(35, 100, 85));
        g2.fillRoundRect(225, 355, 350, 48, 15, 15);

        g2.setColor(new Color(130, 245, 190));
        g2.setFont(new Font("Arial", Font.BOLD, 19));

        String earned = "COINS EARNED: +" + coinsEarned;
        fm = g2.getFontMetrics();

        g2.drawString(
            earned,
            (WIDTH - fm.stringWidth(earned)) / 2,
            385
        );

        // Buttons

        drawButton(g2, "PLAY AGAIN", 300, 425, 200, 50);
        drawButton(g2, "MAIN MENU", 300, 490, 200, 50);

        g2.dispose();
    }

    // PAINT COMPONENT

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        if (screen.equals("MENU")) {
            drawMenu(g);
        }

        else if (screen.equals("GAME")) {

            drawGame(g);

            if (paused) {
                drawPause(g);
            }
        }

        else if (screen.equals("SHOP")) {
            drawShop(g);
        }

        else if (screen.equals("GAME_OVER")) {
            drawGameOver(g);
        }
    }

    // MAIN METHOD

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            JFrame frame = new JFrame("Snake Game");

            sg game = new sg();

            frame.add(game);

            frame.pack();

            frame.setDefaultCloseOperation(
                JFrame.DO_NOTHING_ON_CLOSE
            );

            frame.addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosing(WindowEvent e) {

                    game.timer.stop();
                    game.saveData();

                    System.exit(0);
                }
            });

            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            game.requestFocusInWindow();
        });
    }
}
