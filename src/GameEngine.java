import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class GameEngine extends JPanel implements Runnable, KeyListener {

    final int tileSize = 32; 

    final int screenWidth = 800;
    final int screenHeight = 576;
    final int collisionScale = 2;

    int maxWorldCol;
    int maxWorldRow;
    int worldWidth;
    int worldHeight;

    final int STATE_TITLE = 0;
    final int STATE_STARTER_SELECT = 1;
    final int STATE_ROAMING = 2;
    final int STATE_TRANSITION = 3;
    final int STATE_BATTLE = 4;

    int gameState = STATE_TITLE;

    Thread gameThread;
    Set<Integer> activeKeys = new HashSet<>();

    Image starterMapImage;
    Image titleScreenImage;
    Image pressEnterImage;
    BufferedImage battleBg;
    BufferedImage battleBase;

    int titleCounter = 0;
    boolean showStartText = true;
    int transitionCounter = 0;
    boolean flashWhite = true;

    int battleAnimTimer = 0;
    int playerShakeX = 0;
    int enemyShakeX = 0;
    boolean isPlayerTurn = true;
    boolean isBattleAnimating = false;
    int skillCursor = 0;
    String lastEffectiveness = "";
    boolean wasCritical = false;
    int damagePopup = 0;
    int damagePopupY = 0;
    boolean showDamageOnEnemy = true;

    Player player;
    Rooster playerCock;
    Rooster enemyCock;
    String battleMessage = "";
    String battleSubMessage = "";
    int starterIndex = 0;
    
    MusicManager musicManager;

    // MAP DATA (26 x 29) - Matches MapV2.png at 416x464 pixels with 16px tiles
    // 0 = Water (impassable), 1 = Walls/Buildings/Objects (impassable), 2 = Grass (walkable), 3 = Path (walkable, no encounters)
    int[][] newMapData = {
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}, // Row 0: Top fence
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}, // Row 1: Top fence
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}, // Row 2: Grass + water
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}, // Row 3: Grass
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}, // Row 4: Building roof
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}, // Row 5: Building roof
            {1,1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1}, // Row 6: Building
            {1,1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1}, // Row 7: Building
            {1,1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1}, // Row 8: Building
            {1,1,2,2,2,2,2,1,1,1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,1}, // Row 9: Building
            {1,1,2,2,2,2,2,1,1,1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,1}, // Row 10: Building base
            {1,1,2,2,2,2,2,1,1,1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,1}, // Row 11: Building base
            {1,1,2,2,2,2,2,1,1,1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,1}, // Row 12: Path
            {1,1,2,2,2,2,2,1,1,1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,1}, // Row 13: Path
            {1,1,2,2,2,2,2,1,1,1,1,1,1,1,1,1,1,2,2,3,2,2,2,2,2,1}, // Row 14: Bush + path
            {1,1,2,2,2,2,2,1,1,3,3,3,3,3,3,1,1,3,3,3,2,2,2,2,2,1}, // Row 15: Bush + path
            {1,1,2,2,2,2,2,1,1,2,2,2,2,2,2,2,3,3,3,3,2,2,2,2,2,1}, // Row 16: Bush + grass
            {1,1,2,2,2,2,2,1,1,2,2,2,2,2,2,2,3,3,2,2,2,2,2,2,2,1}, // Row 17: Bush + well
            {1,1,2,2,2,2,2,1,1,2,2,2,2,2,2,2,3,3,2,2,2,2,2,2,2,1}, // Row 18: Bush + well
            {1,1,2,2,2,2,2,1,1,2,2,2,2,2,2,2,3,1,1,2,2,2,2,2,2,1}, // Row 19: Bush + well
            {1,1,2,2,2,2,2,1,1,2,2,2,2,2,2,2,2,1,1,2,2,2,2,2,2,1}, // Row 20: Bush + path
            {1,1,2,2,2,2,2,1,1,2,2,2,2,2,2,2,2,1,1,2,2,2,2,2,2,1}, // Row 21: Bush + path
            {1,1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1,1,2,2,2,2,2,2,1}, // Row 22: Grass
            {1,1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1}, // Row 23: Grass
            {1,1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1}, // Row 24: Grass
            {1,1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1}, // Row 25: Grass
            {1,1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1}, // Row 26: Bottom fence
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,2,2,2,2,2,1}, // Row 27: Bottom fence
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}  // Row 28: Border
    };

    public GameEngine() {
        if (newMapData != null && newMapData.length > 0 && newMapData[0] != null) {
            maxWorldRow = newMapData.length;
            maxWorldCol = newMapData[0].length;
        } else {
            maxWorldRow = 24;
            maxWorldCol = 25;
        }
        worldWidth = tileSize * maxWorldCol;
        worldHeight = tileSize * maxWorldRow;
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        loadImages();
    }

    public void setupGame() {
        this.addKeyListener(this);
        this.setFocusable(true);

        SwingUtilities.invokeLater(this::requestFocusInWindow);

        player = new Player(tileSize * 10, tileSize * 20);
        musicManager = new MusicManager();
        musicManager.playMusic(GameConstants.MUSIC_TITLE, true);
        gameState = STATE_TITLE;
    }

    private void loadImages() {
        try {
            starterMapImage = loadImageSafe("/res/MapV2.png");
            titleScreenImage = loadImageSafe("/res/Title.png");
            pressEnterImage = loadImageWithTransparency("/res/start.png", true);

            battleBg = toBuffered(loadImageSafe(GameConstants.PATH_BG_CITY));
            if (battleBg == null) System.out.println("❌ ERROR: Failed to load " + GameConstants.PATH_BG_CITY);

            battleBase = toBuffered(loadImageSafe(GameConstants.PATH_BASE));
            if (battleBase == null) System.out.println("❌ ERROR: Failed to load " + GameConstants.PATH_BASE);

        } catch (Exception e) {
            System.err.println("Error loading images: " + e.getMessage());
        }
    }

    private BufferedImage toBuffered(Image img) {
        if (img == null) return null;
        if (img instanceof BufferedImage) return (BufferedImage) img;
        BufferedImage buf = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = buf.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return buf;
    }

    private Image loadImageSafe(String path) {
        URL url = getClass().getResource(path);
        if (url != null) {
            return new ImageIcon(url).getImage();
        } else {
            System.out.println("⚠️ ERROR: Could not find " + path);
            return null;
        }
    }

    
    private Image loadImageWithTransparency(String path, boolean removeBlack) {
        try {
            URL url = getClass().getResource(path);
            if (url == null) return null;

            BufferedImage raw = ImageIO.read(url);
            BufferedImage transparentImg = new BufferedImage(raw.getWidth(), raw.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = transparentImg.createGraphics();
            g2.drawImage(raw, 0, 0, null);
            g2.dispose();

            for (int row = 0; row < transparentImg.getHeight(); row++) {
                for (int col = 0; col < transparentImg.getWidth(); col++) {
                    int rgba = transparentImg.getRGB(col, row);
                    int alpha = (rgba >> 24) & 0xFF;
                    int r = (rgba >> 16) & 0xFF;
                    int g = (rgba >> 8) & 0xFF;
                    int b = rgba & 0xFF;

                    if (removeBlack) {
                        if (alpha > 0 && r < 30 && g < 30 && b < 30) {
                            transparentImg.setRGB(col, row, 0x00000000);
                        }
                    }
                }
            }
            return transparentImg;
        } catch (Exception e) {
            System.err.println("loadImageWithTransparency error: " + e.getMessage());
            return null;
        }
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1_000_000_000.0 / 60.0;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    public void update() {
        switch (gameState) {
            case STATE_TITLE -> {
                titleCounter++;
                if (titleCounter > 40) {
                    showStartText = !showStartText;
                    titleCounter = 0;
                }
            }
            case STATE_ROAMING -> {
                player.update(activeKeys, newMapData, tileSize);

                int playerCenterX = player.x + (tileSize / 2);
                int playerCenterY = player.y + (tileSize / 2);

                int col = playerCenterX / tileSize;
                int row = playerCenterY / tileSize;

                if (col >= 0 && col < maxWorldCol && row >= 0 && row < maxWorldRow) {
                    int tileType = newMapData[row][col];
                    if (tileType == 2 && player.isMoving && new Random().nextInt(100) < 1) {
                        startTransition();
                    }
                }
            }
            case STATE_TRANSITION -> {
                transitionCounter++;
                if (transitionCounter % 5 == 0) flashWhite = !flashWhite;
                if (transitionCounter > 60) startBattle();
            }
        }
    }

    private void startTransition() {
        gameState = STATE_TRANSITION;
        transitionCounter = 0;
        activeKeys.clear();
    }

    private void startBattle() {
        gameState = STATE_BATTLE;
        enemyCock = RoosterFactory.createRandomRooster("Wild Chicken");
        battleMessage = "Wild " + enemyCock.getName() + " appeared!";
        battleSubMessage = "";
        isPlayerTurn = true;
        isBattleAnimating = false;
        skillCursor = 0;
        lastEffectiveness = "";
        wasCritical = false;
        damagePopup = 0;
        if (musicManager != null) musicManager.playMusic(GameConstants.MUSIC_BATTLE, true);
        if (musicManager != null) musicManager.playSound(GameConstants.SOUND_ROOSTER);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        switch (gameState) {
            case STATE_TITLE -> drawTitleScreen(g2);
            case STATE_STARTER_SELECT -> drawStarterSelect(g2);
            case STATE_ROAMING -> drawRoaming(g2);
            case STATE_TRANSITION -> {
                drawRoaming(g2);
                drawTransition(g2);
            }
            case STATE_BATTLE -> drawBattle(g2);
        }
        g2.dispose();
    }

    private void drawTitleScreen(Graphics2D g2) {
        if (titleScreenImage != null) {
            g2.drawImage(titleScreenImage, 0, 0, screenWidth, screenHeight, null);
        } else {
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, screenWidth, screenHeight);
        }
        if (showStartText && pressEnterImage != null) {
            int x = screenWidth / 2 - (pressEnterImage.getWidth(null) / 2);
            int y = screenHeight - 100;
            g2.drawImage(pressEnterImage, x, y, null);
        }
    }

    private void drawStarterSelect(Graphics2D g2) {
        GradientPaint gp = new GradientPaint(0, 0, new Color(20, 20, 60), 0, screenHeight, new Color(0, 0, 0));
        g2.setPaint(gp);
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 30));
        String text = "CHOOSE YOUR STARTER";
        int x = getCenteredX(g2, text);
        g2.drawString(text, x, 100);

        drawStarterOption(g2, "MANOK NA PULA", 0, 200);
        drawStarterOption(g2, "MANOK NA ITIM", 1, 280);
        drawStarterOption(g2, "MANOK NA BATO", 2, 360);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 15));
        g2.setColor(Color.GRAY);
        g2.drawString("[W/S] to Move   [ENTER] to Select   [M] Mute/Unmute", 200, 500);
    }

    private void drawStarterOption(Graphics2D g2, String name, int index, int y) {
        if (index == starterIndex) {
            g2.setColor(Color.YELLOW);
            g2.setFont(new Font("Arial", Font.BOLD, 26));
            g2.drawString("> " + name + " <", 250, y);
        } else {
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.PLAIN, 22));
            g2.drawString(name, 280, y);
        }
    }

    private void drawRoaming(Graphics2D g2) {
        int cameraX = player.x - screenWidth / 2 + tileSize / 2;
        int cameraY = player.y - screenHeight / 2 + tileSize / 2;

        if (cameraX < 0) cameraX = 0;
        if (cameraY < 0) cameraY = 0;
        if (worldWidth > screenWidth && cameraX > worldWidth - screenWidth) cameraX = worldWidth - screenWidth;
        if (worldHeight > screenHeight && cameraY > worldHeight - screenHeight) cameraY = worldHeight - screenHeight;

        g2.translate(-cameraX, -cameraY);
        if (starterMapImage != null) {
            g2.drawImage(starterMapImage, 0, 0, worldWidth, worldHeight, null);
        }
        player.draw(g2, tileSize);
        g2.translate(cameraX, cameraY);
    }

    private void drawTransition(Graphics2D g2) {
        if (flashWhite) g2.setColor(Color.WHITE);
        else g2.setColor(Color.BLACK);

        for (int i = 0; i < screenHeight; i += 40) {
            g2.fillRect(0, i, screenWidth, 20);
        }
    }

    private void drawBattle(Graphics2D g2) {
        if (playerCock == null || enemyCock == null) {
            g2.setColor(Color.GRAY);
            g2.fillRect(0, 0, screenWidth, screenHeight);
            g2.setColor(Color.WHITE);
            g2.drawString("Loading battle...", 50, 50);
            return;
        }

        if (battleAnimTimer > 0) {
            battleAnimTimer--;
            if (showDamageOnEnemy) {
                enemyShakeX = (battleAnimTimer % 4 < 2) ? -5 : 5;
            } else {
                playerShakeX = (battleAnimTimer % 4 < 2) ? -5 : 5;
            }
            if (damagePopupY > 0) damagePopupY -= 2;
        } else {
            enemyShakeX = 0;
            playerShakeX = 0;
        }

        if (battleBg != null) {
            g2.drawImage(battleBg, 0, 0, screenWidth, screenHeight, null);
        } else {
            GradientPaint skyGradient = new GradientPaint(0, 0, new Color(135, 206, 235), 0, screenHeight, new Color(34, 139, 34));
            g2.setPaint(skyGradient);
            g2.fillRect(0, 0, screenWidth, screenHeight);
        }

        int enemyBaseX = 480, enemyBaseY = 140;
        int playerBaseX = 80, playerBaseY = 290;

        if (battleBase != null) {
            g2.drawImage(battleBase, enemyBaseX - 30, enemyBaseY, 250, 100, null);
            g2.drawImage(battleBase, playerBaseX - 30, playerBaseY, 250, 100, null);
        }

        if (enemyCock.getFrontSprite() != null) {
            g2.drawImage(enemyCock.getFrontSprite(), enemyBaseX + 20 + enemyShakeX, enemyBaseY - 80, 160, 160, null);
        } else {
            g2.setColor(Color.RED);
            g2.fillOval(enemyBaseX + 50 + enemyShakeX, enemyBaseY - 50, 80, 80);
        }

        if (playerCock.getBackSprite() != null) {
            g2.drawImage(playerCock.getBackSprite(), playerBaseX + 20 + playerShakeX, playerBaseY - 80, 160, 160, null);
        } else {
            g2.setColor(Color.BLUE);
            g2.fillOval(playerBaseX + 50 + playerShakeX, playerBaseY - 50, 80, 80);
        }

        if (battleAnimTimer > 0 && damagePopup > 0) {
            g2.setFont(new Font("Arial", Font.BOLD, 28));
            int popupX = showDamageOnEnemy ? enemyBaseX + 80 : playerBaseX + 80;
            int popupY = (showDamageOnEnemy ? enemyBaseY - 40 : playerBaseY - 40) - (20 - damagePopupY);
            
            g2.setColor(Color.BLACK);
            g2.drawString("-" + damagePopup, popupX + 2, popupY + 2);
            
            if (wasCritical) {
                g2.setColor(Color.ORANGE);
            } else {
                g2.setColor(Color.WHITE);
            }
            g2.drawString("-" + damagePopup, popupX, popupY);
        }

        drawHPBar(g2, enemyCock, 30, 30, true);
        drawHPBar(g2, playerCock, screenWidth - 270, 320, false);

        int boxX = 0, boxY = 420, boxW = screenWidth, boxH = 160;
        
        GradientPaint boxGradient = new GradientPaint(0, boxY, new Color(20, 20, 40, 240), 0, boxY + boxH, new Color(10, 10, 30, 240));
        g2.setPaint(boxGradient);
        g2.fillRect(boxX, boxY, boxW, boxH);
        g2.setColor(new Color(255, 215, 0)); 
        g2.setStroke(new BasicStroke(3));
        g2.drawRect(boxX + 4, boxY + 4, boxW - 8, boxH - 8);
        g2.setColor(new Color(139, 69, 19)); 
        g2.drawRect(boxX + 8, boxY + 8, boxW - 16, boxH - 16);

        g2.setFont(new Font("Monospaced", Font.BOLD, 20));
        g2.setColor(Color.WHITE);
        g2.drawString(battleMessage, boxX + 25, boxY + 38);
        
        if (!battleSubMessage.isEmpty()) {
            if (battleSubMessage.contains("super effective")) {
                g2.setColor(new Color(0, 255, 100));
            } else if (battleSubMessage.contains("not very effective")) {
                g2.setColor(new Color(255, 150, 100));
            } else if (battleSubMessage.contains("CRITICAL")) {
                g2.setColor(Color.ORANGE);
            } else {
                g2.setColor(Color.LIGHT_GRAY);
            }
            g2.setFont(new Font("Monospaced", Font.ITALIC, 16));
            g2.drawString(battleSubMessage, boxX + 25, boxY + 58);
        }

        if (isPlayerTurn && !isBattleAnimating) {
            List<Skill> skills = playerCock.getSkills();
            int skillStartY = boxY + 85;
            
            for (int i = 0; i < skills.size(); i++) {
                Skill s = skills.get(i);
                int skillX = (i < 2) ? boxX + 25 : boxX + 400;
                int skillY = skillStartY + (i % 2) * 35;
                
                if (i == skillCursor) {
                    g2.setColor(new Color(255, 255, 100, 80));
                    g2.fillRoundRect(skillX - 5, skillY - 20, 350, 30, 10, 10);
                    g2.setColor(Color.YELLOW);
                    g2.setFont(new Font("Monospaced", Font.BOLD, 18));
                    g2.drawString("▶ ", skillX, skillY);
                    skillX += 20;
                } else {
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Monospaced", Font.PLAIN, 16));
                }
                
                g2.drawString("[" + (i + 1) + "] " + s.getName(), skillX, skillY);

                g2.setFont(new Font("Arial", Font.BOLD, 11));
                Color typeColor = getTypeColor(s.getType());
                g2.setColor(typeColor);
                g2.fillRoundRect(skillX + 180, skillY - 14, 55, 18, 8, 8);
                g2.setColor(Color.WHITE);
                g2.drawString(s.getType().toUpperCase(), skillX + 188, skillY);
                
                g2.setFont(new Font("Arial", Font.PLAIN, 12));
                g2.setColor(Color.GRAY);
                g2.drawString("PWR:" + s.getDamage(), skillX + 245, skillY);
            }
            
            g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
            g2.setColor(Color.GRAY);
            g2.drawString("[W/S] Select   [ENTER/1-3] Attack   [M] Mute", boxX + 25, boxY + boxH - 15);
        } else if (isBattleAnimating) {
            g2.setFont(new Font("Monospaced", Font.ITALIC, 16));
            g2.setColor(Color.GRAY);
            g2.drawString("...", boxX + 25, boxY + 90);
        }
    }

    private Color getTypeColor(String type) {
        return switch (type.toLowerCase()) {
            case "fire" -> new Color(255, 68, 34);
            case "water" -> new Color(51, 153, 255);
            case "grass" -> new Color(85, 170, 85);
            case "electric" -> new Color(255, 204, 51);
            case "rock" -> new Color(153, 136, 102);
            case "dark" -> new Color(85, 68, 68);
            case "normal" -> new Color(153, 153, 153);
            default -> new Color(128, 128, 128);
        };
    }

    private void drawHPBar(Graphics2D g2, Rooster r, int x, int y, boolean isEnemy) {
        g2.setColor(new Color(40, 40, 60, 220));
        g2.fillRoundRect(x, y, 240, 80, 15, 15);
        g2.setColor(new Color(255, 215, 0, 150));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(x, y, 240, 80, 15, 15);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.drawString(r.getName(), x + 12, y + 22);
        
        Color typeColor = getTypeColor(r.getType());
        g2.setColor(typeColor);
        g2.fillRoundRect(x + 150, y + 8, 60, 18, 8, 8);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.drawString(r.getType().toUpperCase(), x + 158, y + 21);

        g2.setColor(new Color(60, 60, 60));
        g2.fillRoundRect(x + 12, y + 35, 216, 16, 8, 8);
        
        g2.setColor(new Color(80, 80, 80));
        g2.drawRoundRect(x + 12, y + 35, 216, 16, 8, 8);

        double hpPercent = (double) r.getHp() / r.getMaxHp();
        hpPercent = Math.max(0, Math.min(1.0, hpPercent));
        
        Color hpColor;
        if (hpPercent > 0.5) hpColor = new Color(76, 187, 23);
        else if (hpPercent > 0.25) hpColor = new Color(255, 193, 7);
        else hpColor = new Color(220, 53, 69);
        
        int barWidth = (int) (212 * hpPercent);
        if (barWidth > 0) {
            GradientPaint hpGradient = new GradientPaint(x + 14, y + 37, hpColor.brighter(), x + 14, y + 49, hpColor.darker());
            g2.setPaint(hpGradient);
            g2.fillRoundRect(x + 14, y + 37, barWidth, 12, 6, 6);
        }

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 13));
        String hpText = r.getHp() + " / " + r.getMaxHp();
        g2.drawString(hpText, x + 12, y + 68);
        
        g2.setColor(Color.LIGHT_GRAY);
        g2.setFont(new Font("Arial", Font.PLAIN, 11));
        g2.drawString("HP", x + 200, y + 68);
    }

    private int getCenteredX(Graphics2D g2, String text) {
        int length = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        return screenWidth / 2 - length / 2;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        switch (gameState) {
            case STATE_TITLE -> {
                if (code == KeyEvent.VK_ENTER) {
                    if (musicManager != null) musicManager.playSound(GameConstants.SOUND_MENU_ACCEPT);
                    gameState = STATE_STARTER_SELECT;
                    if (musicManager != null) musicManager.playMusic(GameConstants.MUSIC_ADVENTURE, true);
                }
                if (code == KeyEvent.VK_M && musicManager != null) musicManager.toggleMute();
            }
            case STATE_STARTER_SELECT -> {
                if (code == KeyEvent.VK_W) {
                    if (musicManager != null) musicManager.playSound(GameConstants.SOUND_MENU_MOVE);
                    starterIndex--;
                    if (starterIndex < 0) starterIndex = 2;
                }
                if (code == KeyEvent.VK_S) {
                    if (musicManager != null) musicManager.playSound(GameConstants.SOUND_MENU_MOVE);
                    starterIndex++;
                    if (starterIndex > 2) starterIndex = 0;
                }
                if (code == KeyEvent.VK_ENTER) {
                    if (musicManager != null) {
                        musicManager.playSound(GameConstants.SOUND_MENU_ACCEPT);
                        musicManager.playSound(GameConstants.SOUND_ROOSTER);
                    }
                    playerCock = RoosterFactory.createRooster(starterIndex + 1, "My Manok");
                    gameState = STATE_ROAMING;
                }
                if (code == KeyEvent.VK_M && musicManager != null) musicManager.toggleMute();
            }
            case STATE_ROAMING -> {
                activeKeys.add(code);
                if (code == KeyEvent.VK_M && musicManager != null) musicManager.toggleMute();
            }
            case STATE_BATTLE -> {
                if (!isBattleAnimating && isPlayerTurn) {
                    int maxSkills = playerCock != null ? playerCock.getSkills().size() : 0;
                    if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
                        if (musicManager != null) musicManager.playSound(GameConstants.SOUND_MENU_MOVE);
                        skillCursor--;
                        if (skillCursor < 0) skillCursor = maxSkills - 1;
                    }
                    if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
                        if (musicManager != null) musicManager.playSound(GameConstants.SOUND_MENU_MOVE);
                        skillCursor++;
                        if (skillCursor >= maxSkills) skillCursor = 0;
                    }
                    if (code == KeyEvent.VK_ENTER) executeTurn(skillCursor);
                    if (code == KeyEvent.VK_1) executeTurn(0);
                    if (code == KeyEvent.VK_2) executeTurn(1);
                    if (code == KeyEvent.VK_3) executeTurn(2);
                }
                if (code == KeyEvent.VK_M && musicManager != null) musicManager.toggleMute();
            }
        }
    }

    private void executeTurn(int skillIndex) {
        if (playerCock == null || enemyCock == null) return;
        if (skillIndex >= playerCock.getSkills().size()) return;
        if (isBattleAnimating) return;

        isBattleAnimating = true;
        isPlayerTurn = false;
        
        Skill skill = playerCock.getSkills().get(skillIndex);

        if (musicManager != null && skill.getSoundEffect() != null) {
            musicManager.playSound(skill.getSoundEffect());
        }

        
        double typeMultiplier = TypeEffectiveness.getMultiplier(skill.getType(), enemyCock.getType());
        boolean isCritical = Math.random() < GameConstants.CRIT_CHANCE;
        
        int dmg = playerCock.attack(enemyCock, skill);
        wasCritical = isCritical;
        
        if (typeMultiplier > 1.0) {
            battleSubMessage = "It's super effective!";
        } else if (typeMultiplier < 1.0) {
            battleSubMessage = "It's not very effective...";
        } else if (isCritical) {
            battleSubMessage = "CRITICAL HIT!";
        } else {
            battleSubMessage = "";
        }
        
        battleAnimTimer = 30;
        damagePopup = dmg;
        damagePopupY = 20;
        showDamageOnEnemy = true;
        
        if (musicManager != null) musicManager.playSound(GameConstants.SOUND_HIT);
        battleMessage = playerCock.getName() + " used " + skill.getName() + "!";
        repaint();

        if (enemyCock.isFainted()) {
            Timer winTimer = new Timer(800, evt -> {
                battleMessage = enemyCock.getName() + " fainted!";
                battleSubMessage = "You won the battle!";
                if (musicManager != null) musicManager.playSound(GameConstants.SOUND_VICTORY);
                endBattle(true);
            });
            winTimer.setRepeats(false);
            winTimer.start();
            return;
        }

        Timer enemyTimer = new Timer(1200, evt -> {
            List<Skill> eSkills = enemyCock.getSkills();
            Skill eSkill = eSkills.get(new Random().nextInt(eSkills.size()));

    
            if (musicManager != null && eSkill.getSoundEffect() != null) {
                musicManager.playSound(eSkill.getSoundEffect());
            }
            
            double eTypeMultiplier = TypeEffectiveness.getMultiplier(eSkill.getType(), playerCock.getType());
            boolean eCrit = Math.random() < GameConstants.CRIT_CHANCE;
            
            int eDmg = enemyCock.attack(playerCock, eSkill);
            wasCritical = eCrit;
            
            if (eTypeMultiplier > 1.0) {
                battleSubMessage = "It's super effective!";
            } else if (eTypeMultiplier < 1.0) {
                battleSubMessage = "It's not very effective...";
            } else if (eCrit) {
                battleSubMessage = "CRITICAL HIT!";
            } else {
                battleSubMessage = "";
            }
            
            battleAnimTimer = 30;
            damagePopup = eDmg;
            damagePopupY = 20;
            showDamageOnEnemy = false;
            
            if (musicManager != null) musicManager.playSound(GameConstants.SOUND_IMPACT);
            battleMessage = enemyCock.getName() + " used " + eSkill.getName() + "!";
            repaint();

            if (playerCock.isFainted()) {
                Timer loseTimer = new Timer(800, evt2 -> {
                    battleMessage = playerCock.getName() + " fainted!";
                    battleSubMessage = "Rushing home...";
                    if (musicManager != null) musicManager.playSound(GameConstants.SOUND_DEFEAT);
                    endBattle(false);
                });
                loseTimer.setRepeats(false);
                loseTimer.start();
            } else {
                Timer resetTimer = new Timer(800, evt2 -> {
                    isPlayerTurn = true;
                    isBattleAnimating = false;
                    battleSubMessage = "";
                    battleMessage = "What will " + playerCock.getName() + " do?";
                });
                resetTimer.setRepeats(false);
                resetTimer.start();
            }
        });
        enemyTimer.setRepeats(false);
        enemyTimer.start();
    }

    private void endBattle(boolean win) {
        isBattleAnimating = true;
        Timer t = new Timer(2500, evt -> {
            if (!win) {
                player.x = tileSize * 3;
                player.y = tileSize * 20;
                if (playerCock != null) playerCock.healFull();
            }
            isBattleAnimating = false;
            isPlayerTurn = true;
            battleSubMessage = "";
            gameState = STATE_ROAMING;
            if (musicManager != null) musicManager.playMusic(GameConstants.MUSIC_ADVENTURE, true);
        });
        t.setRepeats(false);
        t.start();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        activeKeys.remove(e.getKeyCode());
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}
