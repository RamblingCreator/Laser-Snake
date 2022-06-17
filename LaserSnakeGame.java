// Ruby Leroux
// CSE 142 AP CS A
// Laser Snake
//
// This program runs a game similar to the game "snake", but with some additional mechanics, 
// like having to shoot enemies to get food and various powerups. 
// It also saves scores to a leaderboard

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URL; // Needed to load local image files
import java.net.URI; // Needed to load local image files

// Needed for the JPanel components
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Random;
import java.util.Collections;
//import java.util.*; // not used because it confuses the timer
import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.image.BufferedImage;
import javax.swing.event.*;
import javax.sound.sampled.*;

public class LaserSnakeGame extends JFrame {
int contentSize;
   
   public static void main(String[] args) {
      new LaserSnakeGame();
   }
   
   // Creates the window that the game will be displayed in
   public LaserSnakeGame()  {
      //this.setUndecorated(true);
      GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
      Rectangle bounds = env.getMaximumWindowBounds();
      contentSize = (((int)(bounds.getHeight())/120)*120);
      //contentSize = (((int)(900)/120)*120);
      contentSize = 12 * (contentSize/12)/10*10;
      
      this.setTitle("Laser Snake");
      this.setDefaultCloseOperation(EXIT_ON_CLOSE);
      this.getContentPane().setLayout(new BorderLayout());
      this.getContentPane().add(new GamePanel()); 
      this.setVisible(true);
      
      Insets inset = this.getInsets();
      this.setSize(new Dimension(contentSize+inset.left+inset.right, contentSize+inset.top+inset.bottom));
      
      //this.setLocation((int)bounds.getWidth()+700+30, 30);
      this.setLocationRelativeTo(null);
      this.setVisible(true);
   }
   
   // The panel that will be displayed in the LaserSnakeGame window
   public class GamePanel extends JPanel implements ActionListener {
      private Color PURPLE = new Color(175, 0, 255);
      //private Color PURPLE = new Color(140, 15, 220);
      private double dx;
      private double dy;
      String currentName = "";
      
      int direction = 1;
      int currentDirection = 1;
      int distance = 1;
      int smoothness = 10;
      int amount = 12;
      int size = contentSize/amount; //50 at 600(default)
      ArrayList<SnakeScore> leaderboard = new ArrayList<SnakeScore>();
      Timer time;
      boolean youWin = false;
      boolean isDead = false;
      int screen = 0; // title, game, death, win, scores, instructions
      boolean closePanel = false;
      File input;
      File fontInput;
      boolean repeatKey = false;
      int[] keyboardCursor = new int[]{0, 0};
      String[][] keyboard = new String[4][7];
      
      int[] position = new int[]{contentSize/2, contentSize/2};
      ArrayList<Integer[]> body = new ArrayList<Integer[]>();
      ArrayList<Integer[]> snakeTiles = new ArrayList<Integer[]>();
      ArrayList<SnakeLaser> lasers = new ArrayList<SnakeLaser>();
      ArrayList<Integer> directionInputs = new ArrayList<Integer>();
      int nextEnemy = 7;
      
      SnakeEnemy[] enemies;
      ArrayList<SnakeEnemy> enemiesList = new ArrayList<SnakeEnemy>();
      
      int length = 3;
      
      boolean key_shift;
      boolean shooting;
      int powerupCountdown = 0;
      int powerupRespawn = 240;
      int[] powerupPos;
      int powerupType = 0; // speed, invulnerability, portals
      boolean powerupActive = false;
      boolean powerupOnBoard = false;
      int[] portalExit = new int[2];
      
      BufferedImage[] headImages = new BufferedImage[4];
      BufferedImage[] laserImages = new BufferedImage[4];
      BufferedImage bugImage;
      BufferedImage[] powerupImages = new BufferedImage[4];
      BufferedImage[][] powerupHeadImages = new BufferedImage[2][4];
      
      BufferedImage titleScreenImage;
      BufferedImage gameScreenImage;
      BufferedImage deathScreenImage;
      BufferedImage winScreenImage;
      BufferedImage scoresScreenImage;
      BufferedImage instructionScreenImage;
      
      // This method is called at the start of the program, and it does many things.
      // It creates the panel, starts the timer, calls the method to load images, 
      // and creates the first enemies and powerup
            
      public GamePanel()  {
         this.setFocusable(true);
         addKeyListener(new GameInput());
         time = new Timer((int)(500/smoothness), this);
         time.start();
         
         addKeyListener(new GameInput());
         
         ArrayList<Integer[]> invalidTiles = new ArrayList<Integer[]>();
         for (int i = -2; i < 5; i++) {
            for (int j = -2; j < 5; j++) {
               invalidTiles.add(new Integer[]{amount/2+i, amount/2+j});
            }
         }
         enemies = new SnakeEnemy[]{new SnakeEnemy(invalidTiles), new SnakeEnemy(invalidTiles), new SnakeEnemy(invalidTiles)};
         newPowerup();
         loadGameImages();
         loadGameFiles();
      }
      
      public void loadGameFiles () {
         input = new File ("leaderboard.txt");
         fontInput = new File ("GoodTimesFont.otf");
      }
      
      // This method loads all the images into their designated variables.
      public void loadGameImages () {// title, game, death, win, scores, instructions
         titleScreenImage = scaleImage(loadImage("LaserSnakeScreens_Title.png"), contentSize, contentSize);
         gameScreenImage = scaleImage(loadImage("LaserSnakeScreens_Grid.png"), contentSize, contentSize);
         deathScreenImage = scaleImage(loadImage("LaserSnakeScreens_Death.png"), contentSize, contentSize);
         winScreenImage = scaleImage(loadImage("LaserSnakeScreens_Win.png"), contentSize, contentSize);
         //deathScreenImage = scaleImage(loadImage("Laser Snake Screens_DeathKeys.png"), contentSize, contentSize);
         //winScreenImage = scaleImage(loadImage("Laser Snake Screens_WinKeys.png"), contentSize, contentSize);
         scoresScreenImage = scaleImage(loadImage("LaserSnakeScreens_Scores.png"), contentSize, contentSize);
         instructionScreenImage = scaleImage(loadImage("LaserSnakeScreens_Instructions.png"), contentSize, contentSize);
         
         for (int i = 0; i < headImages.length; i++) {
            //headImages[i] = scaleImage(loadImage("LaserSnakeAssets_Head " + i + ".png"), contentSize/12-1, contentSize/12-1);
            headImages[i] = scaleImage(loadImage("SnakeEyes_" + i + ".png"), contentSize/12-1, contentSize/12-1);
         }
         for (int i = 0; i < laserImages.length; i++) {
            laserImages[i] = scaleImage(loadImage("LaserSnakeAssets_Laser " + i + ".png"), contentSize/12, contentSize/12);
         }
         
         for (int i = 0; i < powerupHeadImages[0].length; i++) {
            powerupHeadImages[0][i] = scaleImage(loadImage("SnakeEyes_Spd" + i + ".png"), contentSize/12-1, contentSize/12-1);
            powerupHeadImages[1][i] = scaleImage(loadImage("SnakeEyes_Inv" + i + ".png"), contentSize/12-1, contentSize/12-1);
         }
         
         bugImage = scaleImage(loadImage("LaserSnakeAssets_Bug.png"), contentSize/12, contentSize/12);
         powerupImages[0] = scaleImage(loadImage("LaserSnakeAssets_Powerup1.png"), contentSize/12, contentSize/12);
         powerupImages[1] = scaleImage(loadImage("LaserSnakeAssets_Powerup2.png"), contentSize/12, contentSize/12);
         powerupImages[2] = scaleImage(loadImage("LaserSnakeAssets_Portal 1.png"), contentSize/12, contentSize/12);
         powerupImages[3] = scaleImage(loadImage("LaserSnakeAssets_Portal 2.png"), contentSize/12, contentSize/12);
      }
      
      // This method takes an image from a file and creates a BufferedImage
      // It returns the bufferedImage of the file
      public BufferedImage loadImage (String filename) {
         URL urlForImage = getClass().getResource(filename);
         ImageIcon thisImageIcon = new ImageIcon(urlForImage);
         Image thisImage = thisImageIcon.getImage();
         BufferedImage buffered = new BufferedImage(thisImage.getWidth(null), thisImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
         Graphics2D bGr = buffered.createGraphics();
         bGr.drawImage(thisImage, 0, 0, null);
         return(buffered);
      }
      // Scales a BufferedImage to the given width and height, and returns it.
      // Parameters:
      //    BufferedImage origImage - the image to be scaled
      //    int newWidth - the width that the image will be set to
      //    int newHeight - the height that the image will be set to
      public BufferedImage scaleImage (BufferedImage origImage, int newWidth, int newHeight) {
         BufferedImage scaledImage = null;
         if (origImage != null) {
            scaledImage = new BufferedImage(newWidth, newHeight, origImage.getType());
            Graphics2D graphics2D = scaledImage.createGraphics();
            graphics2D.drawImage(origImage, 0, 0, newWidth, newHeight, null);
            graphics2D.dispose();
         }
         return (scaledImage);
      }
      
      // Finds all tiles on the playing board that are occupied
      // Returns an ArrayList of coordinates
      public ArrayList<Integer[]> getInvalidTiles () {
         getTiles();
         ArrayList<Integer[]> invalidTiles = new ArrayList<Integer[]>();
         
         int[] headTile = new int[]{position[0]/size, position[1]/size};
         Integer[][] snakeBuffer = new Integer[][]{{headTile[0]-1, headTile[1]-1}, {headTile[0], headTile[1]-1}, {headTile[0]+1, headTile[1]-1}, 
               {headTile[0]+1, headTile[1]}, {headTile[0]+1, headTile[1]+1}, {headTile[0], headTile[1]+1}, {headTile[0]-1, headTile[1]+1}, {headTile[0]-1, headTile[1]}};
         
         for (int i = 0; i < snakeBuffer.length; i++) {
            if (!invalidTiles.contains(snakeBuffer[i])) {
               invalidTiles.add(snakeBuffer[i]);
            }
         }
         for (int i = 0; i < snakeTiles.size(); i++) {
            if (!invalidTiles.contains(snakeTiles.get(i))) {
               invalidTiles.add(snakeTiles.get(i));
            }
         }
         for (int i = 0; i < enemies.length; i++) {
            Integer[] thisEnemy = new Integer[]{enemies[i].tilePosition[0], enemies[i].tilePosition[1]};
            if (!invalidTiles.contains(new Integer[]{thisEnemy[0], thisEnemy[1]})) {
               invalidTiles.add(new Integer[]{thisEnemy[0], thisEnemy[1]});
            }
         }
         if (powerupOnBoard) {
            Integer[] thisPowerup = new Integer[]{powerupPos[0], powerupPos[1]};
            if (!invalidTiles.contains(new Integer[]{thisPowerup[0], thisPowerup[1]})) {
                  invalidTiles.add(new Integer[]{thisPowerup[0], thisPowerup[1]});
               }
            thisPowerup = new Integer[]{portalExit[0], portalExit[1]};
            if (powerupType == 2) {
               if (!invalidTiles.contains(new Integer[]{thisPowerup[0], thisPowerup[1]})) {
                  invalidTiles.add(new Integer[]{thisPowerup[0], thisPowerup[1]});
               }
            }
         }
         return(invalidTiles);
      }
      
      // Creates a powerup at a random unoccupied position, and determines the type of powerup
      public void newPowerup () {
         Random r = new Random();
         powerupType = r.nextInt(2);
         
         powerupPos = new int[]{-1, -1};
         portalExit = new int[]{-1, -1};
         ArrayList<Integer[]> invalidTiles = getInvalidTiles();
         boolean overlap = true;
         while (overlap) {
            overlap = false;
            powerupPos = new int[]{r.nextInt(12), r.nextInt(12)};
            for (int i = 0; i < invalidTiles.size(); i++) {
               if (invalidTiles.get(i)[0] == powerupPos[0] && invalidTiles.get(i)[1] == powerupPos[1]) {
                  overlap = true;
               }
            }
         }
         
         if (powerupType == 2) {
            overlap = true;
            while (overlap) {
               overlap = false;
               portalExit = new int[]{r.nextInt(12), r.nextInt(12)};
               for (int i = 0; i < invalidTiles.size(); i++) {
                  if (invalidTiles.get(i)[0] == portalExit[0] && invalidTiles.get(i)[1] == portalExit[1]) {
                     overlap = true;
                  }
               }
            }
         }
      }
      
      // Determines if the position of the snake's head is touching something
      // Parameters:
      //    int[] thingPos: the coordinates of the thing being tested
      //    int thingSize: the size of the thing being tested
      public boolean headTouching (int[] thingPos, int thingSize) {
         boolean isTouching = false;
         int offset = 0;
         int smoothGrid = size/smoothness;
         int bodySize = size*4/5;
         thingSize = (thingSize/smoothGrid)*smoothGrid;
         
         if ((thingSize/2)%smoothGrid != 0) {
            thingSize -= smoothGrid;
         }
         if (size != thingSize) {
            offset = (size-thingSize)/2;
         }
         if (currentDirection == 0) {
            if (position[0] == thingPos[0] && position[1] == thingPos[1]+thingSize+offset-smoothGrid) {
               isTouching = true;
            }
         } else if (currentDirection == 1) {
            if (position[0]+bodySize == thingPos[0]+offset && position[1] == thingPos[1]) {
               isTouching = true;
            }
         } else if (currentDirection == 2) {
            if (position[0] == thingPos[0] && position[1]+bodySize == thingPos[1]+offset) {
               isTouching = true;
            }
         } else if (currentDirection == 3) {
            if (position[0] == thingPos[0]+thingSize+offset-smoothGrid && position[1] == thingPos[1]) {
               isTouching = true;
            }
         }
         return (isTouching);
      }
      
/*    This method runs every time a certain amount of time has passed.
      If the program is currently running the game, it does a bunch of things:
         - counts down the timers for the powerup
         - checks if a new enemy or laser should spawn
         - moves the snake and the lasers
         - checks if the snake's head is touching anything or if the game is over
      Parameters:
         ActionEvent event - the event that fires the method ???
      */ 
      public void actionPerformed(ActionEvent event) {
         Random r = new Random();
         if (closePanel) {
            System.exit(0);
         }
         if (screen == 1) {
            if (length == nextEnemy) {
               nextEnemy+= 4+(length/5);
               SnakeEnemy newEnemy = new SnakeEnemy(getInvalidTiles());
               enemies = Arrays.copyOf(enemies, enemies.length+1);
               enemies[enemies.length-1] = newEnemy;
            }
            
            if (powerupRespawn > 1) {
               powerupRespawn--;
               powerupOnBoard = false;
            } else if (powerupRespawn == 1) {
               newPowerup();
               powerupRespawn--;
               powerupOnBoard = true;
            }
            if (powerupCountdown > 1) {
               if (powerupType == 0) {
                  powerupActive = true;
                  powerupOnBoard = false;
               }
               powerupCountdown--;
            } else if (powerupCountdown == 1) {
               powerupActive = false;
               powerupOnBoard = false;
               powerupRespawn = 120 + r.nextInt(8)*15;
               powerupCountdown--;
            }
            
            
            if (shooting) {
               SnakeLaser newLaser = new SnakeLaser(position, currentDirection, size);
               if (newLaser.isValid(amount*size, body, enemies, size)) {
                  lasers.add(newLaser);
               }
               shooting = false;
            }
            moveLasers();
            
            moveSnake();
            
            if (powerupOnBoard) {
               
               int[] testPosition = new int[]{position[0], position[1]};
               if (currentDirection == 2) {
                  testPosition[1]+=size;
               } else if (currentDirection == 1) {
                  testPosition[0]+=size;
               }
               if (powerupType == 2 && powerupCountdown == 0) {
                  int[] teleportLocation = new int[2];
                  if (headTouching(new int[]{powerupPos[0]*size, powerupPos[1]*size}, 0)) {
                     teleportLocation[0] = portalExit[0]*size;
                     teleportLocation[1] = portalExit[1]*size;
                  } else if (headTouching(new int[]{portalExit[0]*size, portalExit[1]*size}, 0)) {
                     teleportLocation[0] = powerupPos[0]*size;
                     teleportLocation[1] = powerupPos[1]*size;
                  }
                  if (headTouching(new int[]{powerupPos[0]*size, powerupPos[1]*size}, 0) || headTouching(new int[]{portalExit[0]*size, portalExit[1]*size}, 0)) {
                     position[0] = teleportLocation[0];
                     position[1] = teleportLocation[1];
                     currentDirection = direction;
                     distance = 1;
                     powerupCountdown = (length+2)*smoothness;
                  }
               } else if (headTouching(new int[]{powerupPos[0]*size, powerupPos[1]*size}, 0)) {
                  if (powerupType == 0) {
                     powerupCountdown = 150;
                  }
                  powerupOnBoard = false;
                  powerupActive = true;
               } 
            }
            if (powerupActive && powerupType == 0) { // speed boost active
               time.setDelay(300/smoothness);
            } else {
               time.setDelay(350/smoothness);
            }
            isDead = isDead();
            if (isDead) {
               if (length > 3) {
                  screen = 2;
               } else {
                  resetVars();
                  getLeaderboard();
                  screen = 3;
               }
            }
            if (length-1 >= 99) {
               youWin = true;
               screen = 2;
            }
         }
         repaint();
      }
      
      
      // fills the "keyboard" array with a sequence of strings to act as an onscreen keyboard
      public void getKeyboard () {
         keyboard = new String[4][8];
         for (int i = 0; i < keyboard.length; i++) {
            for (int j = 0; j < keyboard[0].length; j++) {
               keyboard[i][j] = Character.toString((char)(i*keyboard[0].length+j+65));
            }
         }
         String[] otherKeys = new String[]{"->", "_", "<-", "*", "?", "!"};
         for (int i = 1; i <= otherKeys.length; i++) {
            keyboard[keyboard.length-1][keyboard[0].length-i] = otherKeys[i-1];
         }
      }
      
      // converts a position on a board of size 600 to fit the contentSize variable.
      // returns the relative position on the board
      // Parameters:
      //    double boardSize - the position on a 600x600 board
      public int realSize (double boardSize) {
         int outSize = (int)(contentSize/(600.0/boardSize));
         return(outSize);
      }
      
      // Moves all of the lasers and checks if they are still valid
      public void moveLasers(){
         ArrayList<SnakeLaser> newLasers = new ArrayList<SnakeLaser>();
         SnakeLaser addLaser;
         for (int i = 0; i < lasers.size(); i++) {
            addLaser = lasers.get(i);
            if (powerupType == 2 && powerupCountdown == 0 && powerupOnBoard) {
               if (addLaser.x == powerupPos[0]*size && addLaser.y == powerupPos[1]*size) {
                  addLaser.x = portalExit[0]*size;
                  addLaser.y = portalExit[1]*size;
               } else if (addLaser.x == portalExit[0]*size && addLaser.y == portalExit[1]*size) {
                  addLaser.x = powerupPos[0]*size;
                  addLaser.y = powerupPos[1]*size;
               }
            }
            addLaser.moveLaser(smoothness, size*amount, body, enemies, size);
            if (addLaser.isValid(amount*size, body, enemies, size) && addLaser.laserDirection != -1) {
               newLasers.add(addLaser);
            }

         }
         lasers.clear();
         for (int i = 0; i < newLasers.size(); i++) {
            lasers.add(newLasers.get(i));
         }
      }
      
      // this method writes the player's name and score, as well as 
      // the rest of the leaderboard to the "leaderboard.txt" document
      public void saveScore() {
         getLeaderboard();
         if (currentName.length() >= 1) {
            leaderboard.add(new SnakeScore(" " + currentName, length-3));
            try {
               PrintStream output = new PrintStream(input);
               for (int i = 0; i < leaderboard.size(); i++) {
                  output.println(leaderboard.get(i).score + "" + leaderboard.get(i).name);
               }
            } catch (FileNotFoundException e) {}
         }
         getLeaderboard();
      }
      
      // gets the other player's saved scores and names from the 
      // "leaderboard.txt" file, saves them in an array, and sorts them.
      public void getLeaderboard() {
         leaderboard.clear();
         try {
            Scanner readSave = new Scanner(input);
            while (readSave.hasNextLine()) {
               String thisLine = readSave.nextLine();
               Scanner readLine = new Scanner (thisLine);
               int thisScore = readLine.nextInt();
               String thisName = readLine.nextLine();
               leaderboard.add(new SnakeScore(thisName, thisScore));
            }
         } catch (FileNotFoundException e) {}
         //Collections.sort(leaderboard, new SortByScore());
         Collections.sort(leaderboard);
      }
      
      // Generates an ArrayList of all the tiles that the snake is on.
      // Returns this ArrayList.
      public ArrayList<Integer[]> getTiles() {
         snakeTiles.clear();
         Integer[] thisTile = new Integer[2];
         for (int i = 0; i < body.size(); i++) {
            thisTile = new Integer[]{body.get(i)[0]/size, body.get(i)[1]/size};
            boolean bodyContains = false;
            for (int j = 0; j < snakeTiles.size(); j++) {
               if (snakeTiles.get(j)[0] == thisTile[0] && snakeTiles.get(j)[1] == thisTile[1]) {
                  bodyContains = true;
               }
            }
            if (!bodyContains) {
               snakeTiles.add(thisTile);
            }
         }
         return(snakeTiles);
      }
      
      // moves the snake's position in the current direction, turns the snake if it is
      // aligned with the grid, and makes sure that it's body is the correct length.
      public void moveSnake() {
         if (distance <= smoothness) {
            int[][] directions = new int[][]{{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
            position[0]+= (int)(directions[currentDirection][0]*size/smoothness);
            position[1]+= (int)(directions[currentDirection][1]*size/smoothness);
         }
         if (distance > smoothness) {
            distance = 0;
            currentDirection = direction;
         }
         distance++;
         if (body.size() > 0) {
            if (body.get(0)[0] == position[0] && body.get(0)[1] == position[1]) {
               
            } else {
               body.add(0, new Integer[]{position[0], position[1]});
            }
         } else {
            body.add(0, new Integer[]{position[0], position[1]});
         }
         if (body.size() > length*smoothness) {
            body.remove(body.size()-1);
         }
      }
      
      // Draws all the graphics of the game.
      // Parameters: 
      //   Graphics g - references the Graphics object for the panel.
      public void paintComponent(Graphics g)  {
         super.paintComponent(g);
         setBackground(Color.white);
         
         if (screen == 0) {
            g.drawImage(titleScreenImage, 0, 0, this);
         } else if (screen == 4) {
            g.drawImage(instructionScreenImage, 0, 0, this);
         } else if (screen == 3) {
            g.drawImage(scoresScreenImage, 0, 0, this);
            try {
               Font goodTimesFont = Font.createFont(Font.TRUETYPE_FONT, fontInput).deriveFont(12f);
               GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
               ge.registerFont(goodTimesFont);
               g.setFont(new Font("GoodTimesRg-Regular", Font.BOLD, (int)(contentSize/15))); //size == height
            } catch (FontFormatException | IOException e) {}
            g.setColor(Color.LIGHT_GRAY);
            int leaderboardLength = 5;
            
            if (leaderboard.size() < 5) {
               leaderboardLength = leaderboard.size();
            }
            for (int i = 0; i < leaderboardLength; i++) {
               String thisName = leaderboard.get(leaderboard.size()-1-i).name;
               int thisScore = leaderboard.get(leaderboard.size()-1-i).score;
               String thisPrint = thisScore + ": " + thisName;
               int stringWidth = g.getFontMetrics().stringWidth(String.valueOf(thisPrint));
               int stringHeight = g.getFontMetrics().getHeight();
               int xOffset = (int)(contentSize/1.71428571429-stringWidth)/2;
               g.drawString(thisPrint, contentSize/4+xOffset, contentSize/4+contentSize/50+(int)(contentSize/7.4)*i+stringHeight);
            }
         } else if (screen == 2) {
            drawGameOver(g);
         } else if (screen == 1) { // draws the game
            g.drawImage(gameScreenImage, 0, 0, this);
            drawPowerups(g);
            for (int i = 0; i < enemies.length; i++) {
               if (enemies[i].health <= 0) {
                  g.setColor(Color.RED);
                  g.fillOval(enemies[i].tilePosition[0]*size+size/4, enemies[i].tilePosition[1]*size+size/4, size/2, size/2); // **(
               } else {
                  g.drawImage(bugImage, enemies[i].tilePosition[0]*size, enemies[i].tilePosition[1]*size, this);
               }
            }
            for (int i = 0; i < lasers.size(); i++) {
               g.drawImage(laserImages[lasers.get(i).laserDirection], lasers.get(i).x, lasers.get(i).y, this);
            }
            drawSnake(g);
         }
      }
      
      // determines if a powerup is on the board, then draws the correct one.
      // Parameters: 
      //   Graphics g - references the Graphics object for the panel.
      public void drawPowerups (Graphics g) {
         if (powerupOnBoard) {
            if (powerupType == 0) {
               g.drawImage(powerupImages[0], powerupPos[0]*size+1, powerupPos[1]*size+1, this);
            } else if (powerupType == 1) {
               g.drawImage(powerupImages[1], powerupPos[0]*size+1, powerupPos[1]*size+1, this);
            } else if (powerupType == 2) {
               g.drawImage(powerupImages[2], powerupPos[0]*size+1, powerupPos[1]*size+1, this);
               g.drawImage(powerupImages[3], portalExit[0]*size+1, portalExit[1]*size+1, this);
            }
         }
      }
      
      // Draws the snake onto the board.
      // Parameters: 
      //   Graphics g - references the Graphics object for the panel.
      public void drawSnake (Graphics g) {
         
         Color snakeColor = new Color(5, 220, 5);
         Color darkSnakeColor = new Color(5, 195, 5);
         
         int bodySize = size*4/5;
         int snakeOffset = 0;
         
         if (powerupActive) {
            if (powerupType == 0) {
               g.setColor(Color.YELLOW);
            } else if (powerupType == 1) {
               g.setColor(PURPLE);
            }
         } else {
            g.setColor(darkSnakeColor);
         }
         for (int i = 0; i < body.size(); i++) {
            g.fillOval(body.get(i)[0]+size/10+snakeOffset, body.get(i)[1]+size/10+snakeOffset, bodySize, bodySize);
         }
         
         g.setColor(snakeColor);
         int insideSize = bodySize*9/11;
         insideSize = insideSize/2*2;
         int inset = Math.abs((bodySize-insideSize)/2);
         for (int i = 0; i < body.size(); i++) {
            g.fillOval(body.get(i)[0]+size/10+inset+snakeOffset, body.get(i)[1]+size/10+inset+snakeOffset, insideSize, insideSize);
         }
         
         if (powerupActive) {
            g.drawImage(powerupHeadImages[powerupType][currentDirection], position[0]+1+snakeOffset, position[1]+1+snakeOffset, this);
         } else {
            g.drawImage(headImages[currentDirection], position[0]+1+snakeOffset, position[1]+1+snakeOffset, this);
         }
      }
      
      // Draws the game over screen, with the player's score and their current name.
      // Parameters: 
      //   Graphics g - references the Graphics object for the panel.
      public void drawGameOver(Graphics g) {
         if (youWin) {
            g.drawImage(winScreenImage, 0, 0, this);
         } else {
            g.drawImage(deathScreenImage, 0, 0, this);
         }
         try {
            Font goodTimesFont = Font.createFont(Font.TRUETYPE_FONT, fontInput).deriveFont(12f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(goodTimesFont);
            g.setFont(new Font("GoodTimesRg-Regular", Font.BOLD, 70)); //size == height
         } catch (FontFormatException | IOException e) {}
         
         g.setFont(new Font("GoodTimesRg-Regular", Font.BOLD, realSize(70))); //size == height
         g.setColor (Color.WHITE);
         
         int stringWidth = g.getFontMetrics().stringWidth(String.valueOf(length));
         int stringHeight = g.getFontMetrics().getHeight();
         int xOffset = (realSize(95)-stringWidth)/2;
         int yOffset = (int)((realSize(55)- stringHeight)/2);
         
         for (int i = 0; i < currentName.length(); i++) {
            stringWidth = g.getFontMetrics().stringWidth(String.valueOf(currentName.substring(i, i+1)));
            xOffset = (int)((realSize(90)-stringWidth)/2);
            g.drawString(currentName.substring(i, i+1), realSize(45) +xOffset+ (realSize(105)*i), realSize(420)+realSize(70)-4);
         }
         
         stringWidth = g.getFontMetrics().stringWidth(String.valueOf(length-3));
         xOffset = (realSize(140)-stringWidth)/2;
         yOffset = (int)((realSize(80)- stringHeight)/2);
         g.drawString(String.valueOf(length-3), realSize(230)+xOffset, realSize(310)-g.getFontMetrics().getDescent()/2-12);
      }
      
      // Draws a keyboard and the cursor on the screen 
      // Parameters: 
      //   Graphics g - references the Graphics object for the panel.
      public void drawKeyboard(Graphics g) {
         g.setFont(new Font(g.getFont().getFontName(), Font.BOLD, (int)(contentSize/14))); //size == height
         int stringWidth = g.getFontMetrics().stringWidth(String.valueOf(length));
         int stringHeight = g.getFontMetrics().getHeight();
         int xOffset = (realSize(95)-stringWidth)/2;
         int yOffset = (int)((realSize(55)- stringHeight)/2);
         g.setColor (Color.WHITE);
         g.setColor(Color.LIGHT_GRAY);
         int[] startPos = new int[]{realSize(26.5), realSize(314.5)};
         for (int i = 0; i < keyboard.length; i++) {
            for (int j = 0; j < keyboard[0].length; j++) {
               int spacing = realSize(69);
               if (keyboard[i][j].equals("<-")) {
                  drawLeftArrow(g, startPos[0]+spacing*j, startPos[1]+spacing*i+realSize(64)/2);
               } if (keyboard[i][j].equals("_")) {
                  drawSpace(g, startPos[0]+spacing*j, startPos[1]+spacing*i+realSize(64)/2);
               } if (keyboard[i][j].equals("->")) {
                  drawRightArrow(g, startPos[0]+spacing*j, startPos[1]+spacing*i+realSize(64)/2);
               } else if (!(i == keyboard.length-1 && j > 4)) {
                  stringWidth = g.getFontMetrics().stringWidth(String.valueOf(keyboard[i][j]));
                  xOffset = ((realSize(64)-stringWidth)/2);
                  yOffset = realSize(64)-(realSize(64)-stringHeight)/2-contentSize/50;
                  g.drawString(keyboard[i][j], startPos[0]+spacing*j+xOffset, startPos[1]+spacing*i+yOffset);
               }
            }
         }
         g.drawString(String.valueOf(length-3), realSize(442.5)+xOffset, realSize(102.5) + realSize(55)-yOffset-g.getFontMetrics().getDescent()); // + realSize(80)
         int baseSize = realSize(64);
         for (int i = 0; i < 2; i++) {
            g.drawRect(startPos[0]+realSize(69)*keyboardCursor[0]+i, startPos[1]+realSize(69)*keyboardCursor[1]+i, baseSize-i*2, baseSize-i*2);
         }
         for (int i = 0; i < currentName.length(); i++) {
            stringWidth = g.getFontMetrics().stringWidth(String.valueOf(currentName.substring(i, i+1)));
            xOffset = (int)((contentSize/7.5-stringWidth)/2);
            g.drawString(currentName.substring(i, i+1), (int)(contentSize/8.57142857143 +xOffset+ contentSize/6.31578947368*i), (int)(contentSize/2.57510729614)+(int)(contentSize/8.57142857143)-4);
         }
      }
      // Draws the space bar icon
      // Parameters: 
      //   Graphics g - references the Graphics object for the panel.
      public void drawSpace (Graphics g, int x, int y){
         g.fillRect(x+realSize((64-40)/2), y, realSize(6), realSize(64)/6);
         g.fillRect(x+realSize((64-40)/2)+realSize(40)-realSize(6), y, realSize(6), realSize(64)/6);
         int centerY = y+realSize(64)/6;
         int endX = x+realSize((64-40)/2)+realSize(40);
         g.fillRect(x+realSize((64-40)/2), centerY, realSize(40), realSize(6));//realSize(37.5)
      }
      // Draws a left arrow icon
      // Parameters: 
      //   Graphics g - references the Graphics object for the panel.
      public void drawLeftArrow(Graphics g, int x, int y){
         int centerY = y+realSize(6)/2;
         int endX = x+realSize((64-40)/2)+realSize(40);
         g.fillRect(x+realSize((64-40)/2)+realSize(10), centerY, realSize(40)-realSize(10), realSize(6));//realSize(37.5)
         int[] xPoints = new int[]{x+realSize(15)+realSize((64-40)/2), x+realSize(15)+realSize((64-40)/2), x+realSize((64-40)/2)};
         centerY += realSize(6)/2;
         int[] yPoints = new int[]{centerY-realSize(10), centerY+realSize(10), centerY};
         g.fillPolygon(xPoints, yPoints, 3);
      }
      
      // Draws a right arrow icon
      // Parameters: 
      //   Graphics g - references the Graphics object for the panel.
      public void drawRightArrow(Graphics g, int x, int y){
         int centerY = y+realSize(6)/2;
         int endX = x+realSize((64-40)/2)+realSize(40);
         g.fillRect(x+realSize((64-40)/2), centerY, realSize(40)-realSize(10), realSize(6));//realSize(37.5)
         int[] xPoints = new int[]{endX-realSize(15), endX-realSize(15), endX};
         centerY += realSize(6)/2;
         int[] yPoints = new int[]{centerY-realSize(10), centerY+realSize(10), centerY};
         g.fillPolygon(xPoints, yPoints, 3);
      }
      
      // Draws the information from various variables onto the screen
      // Only used while testing the game.
      // Parameters: 
      //   Graphics g - references the Graphics object for the panel.
      public void drawInfo (Graphics g) {
         g.setColor(Color.WHITE);
         g.setFont(new Font(g.getFont().getFontName(), Font.BOLD, (int)(contentSize/24)));
         int stringHeight = g.getFontMetrics().getHeight();
         g.drawString(String.valueOf("cDirection: " + currentDirection), 25, 25);
         g.drawString(String.valueOf("inDirection: " + direction), 25, 25+stringHeight);
         g.drawString(String.valueOf("distance: " + distance), 25, 25+stringHeight*2);
         g.drawString(String.valueOf("dirInputs: " + directionInputs.toString()), 25, 25+stringHeight*3);
         g.drawString(String.valueOf("countdown: " + powerupCountdown), 25, 25+stringHeight*4);
         
         ArrayList<Integer[]> fullTiles = getInvalidTiles();
         g.setColor(Color.MAGENTA);
         for (int i = 0; i < fullTiles.size(); i++) {
            g.fillRect(fullTiles.get(i)[0]*size+size/6, fullTiles.get(i)[1]*size+size/6, size*2/3, size*2/3);
         }
      }
      
      // Checks if the snake is off the board, hitting an alive enemy, or hitting itself
      // If the player is invulnerable they won't die, if they are touching food they will grow.
      // Returns true if the snake is dead
      public boolean isDead () {
         ArrayList<Integer[]> snakeBody = new ArrayList<Integer[]>();
         for (int i = 0; i < body.size(); i++) {
            snakeBody.add(body.get(i));
         }
         snakeBody.remove(0);
         
         int[] testPosition = new int[]{position[0], position[1]};
         if (currentDirection == 2) {
            testPosition[1]+=size;
         } else if (currentDirection == 1) {
            testPosition[0]+=size;
         }
         
         boolean thisIsDead = false;
         if (testPosition[0] < 0-size/smoothness || testPosition[0] > size*amount+size/smoothness || testPosition[1] < 0-size/smoothness || testPosition[1] > size*amount+size/smoothness) {
            thisIsDead = true;
         }
         for (int i = 0; i < snakeBody.size(); i++) {
            boolean touchingPortal = false;
            //if (snakeBody.get(i)[0] == position[0] && snakeBody.get(i)[1] == position[1]) {
            if (headTouching(new int[]{snakeBody.get(i)[0], snakeBody.get(i)[1]}, size*3/5)) { 
               if (powerupActive && powerupType == 2) {
                  if (headTouching(new int[]{powerupPos[0]*size, powerupPos[1]*size}, (size*4/5)*-1) || headTouching(new int[]{portalExit[0]*size, portalExit[1]*size}, (size*4/5)*-1)){
                     touchingPortal = true;
                  }
               }
               if ((powerupActive && powerupType == 1) || touchingPortal) {
                  // nothing happens
               } else {
                  thisIsDead = true;
               }
            }
         }
         
         for (int i = 0; i < enemies.length; i++) {
            int[] thisEnemyPos = new int[]{enemies[i].tilePosition[0]*size, enemies[i].tilePosition[1]*size};
            if (headTouching(thisEnemyPos, size/2)) {
               if (powerupActive && powerupType == 1) { //invulnerability
                  length++;
                  if (enemies[i].health <= 0) {
                  } else {
                     powerupCountdown = 1;
                  }
                  enemies[i] = new SnakeEnemy(getInvalidTiles());
               } else {
                  if (enemies[i].health <= 0) {
                     length++;
                     if (powerupType == 2 && powerupCountdown > 1) {
                        powerupCountdown += smoothness;
                     }
                  } else {
                     thisIsDead = true;
                  }
                  enemies[i] = new SnakeEnemy(getInvalidTiles());
               }
            }
         }
         return(thisIsDead);
      }
      
      // This method resets all of the game variables to their "default" state.
      public void resetVars () {
         position = new int[]{contentSize/2, contentSize/2};
         length = 3;
         currentName = "";
         currentDirection = 1;
         direction = 1;
         distance = 1;
         body = new ArrayList<Integer[]>();
         snakeTiles = new ArrayList<Integer[]>();
         lasers = new ArrayList<SnakeLaser>();
         directionInputs = new ArrayList<Integer>();
         enemies = new SnakeEnemy[]{new SnakeEnemy(), new SnakeEnemy(), new SnakeEnemy()};
         screen = 3;
         youWin = false;
         isDead = false;
         powerupCountdown = 0;
         powerupRespawn = 240;
         powerupActive = false;
         powerupOnBoard = false;
         keyboardCursor = new int[]{0, 0};
         nextEnemy = 7;
         
         repeatKey = true;
      }
      
      // Private class to intantiate a KeyListener object
      // Called from the GamePanel contructor
      private class GameInput implements KeyListener {
         
         public void keyTyped(KeyEvent e) {}
      
         public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == e.VK_SHIFT) {
               key_shift = false;
            }
            if (e.getKeyCode() == e.VK_SPACE) {
               //key_space = false;
            }
         }
         
         public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == 27) {
               //closePanel = true; // only use while testing
            } else if (e.getKeyCode() == e.VK_SHIFT) {
               key_shift = true;
            } else if (screen == 2) {
               if (repeatKey == false) {
                  keyboardTyping(e);
                  repeatKey = true;
               } else if (repeatKey == true) {
                  repeatKey = false;
               }
            } else if (screen == 0) {
               if (e.getKeyCode() == 10 || e.getKeyCode() == 32) {
                  if (repeatKey == false) {
                     screen = 4;
                     repeatKey = true;
                  } else if (repeatKey == true) {
                     repeatKey = false;
                  }
               }
            } else if (screen == 3) {
               if (e.getKeyCode() == 10 || e.getKeyCode() == 32) {
                  if (repeatKey == false) {
                     screen = 0;
                     repeatKey = true;
                  } else if (repeatKey == true) {
                     repeatKey = false;
                  }
               }
            } else if (screen == 4) {
               if (e.getKeyCode() == 10 || e.getKeyCode() == 32) {
                  if (repeatKey == false) {  
                     screen = 1;
                     repeatKey = true;
                  } else if (repeatKey == true) {
                     repeatKey = false;
                  }
               }
            } else {
               if (e.getKeyCode() == e.VK_DOWN) {
                  if (currentDirection != 0) {
                     direction = 2;
                  }
               } else if (e.getKeyCode() == e.VK_UP) {
                  if (currentDirection != 2) {
                     direction = 0;
                  }
               } else if (e.getKeyCode() == e.VK_RIGHT) {
                  if (currentDirection != 3) {
                     direction = 1;
                  }
               } else if (e.getKeyCode() == e.VK_LEFT) {
                  if (currentDirection != 1) {
                     direction = 3;
                  }
               } else if (e.getKeyCode() == e.VK_SPACE) {
                  shooting = true;
               }
               /*if (e.getKeyCode() == e.VK_UP || e.getKeyCode() == e.VK_RIGHT || e.getKeyCode() == e.VK_DOWN || e.getKeyCode() == e.VK_LEFT) {
                  System.out.println(directionInputs.toString());
                  if (directionInputs.size() > 0) {
                     if (directionInputs.get(directionInputs.size()-1) != direction) {
                        directionInputs.add(direction);
                     }
                  } else {
                     directionInputs.add(direction);
                  }
               }*/
            }
         }
      }
      
      // Uses the arrow keys to move a cursor and select characters to type
      // Parameters:
      //    KeyEvent e - the key that was pressed
      public void arrowTyping (KeyEvent e) {
         if (e.getKeyCode() == e.VK_UP) {
            if (keyboardCursor[1] > 0) {
               keyboardCursor[1]--;
            }
         } else if (e.getKeyCode() == e.VK_RIGHT) {
            if (keyboardCursor[0] < keyboard[0].length-1) {
               keyboardCursor[0]++;
            }
         } else if (e.getKeyCode() == e.VK_DOWN) {
            if (keyboardCursor[1] < keyboard.length-1) {
               keyboardCursor[1]++;
            }
         } else if (e.getKeyCode() == e.VK_LEFT) {
            if (keyboardCursor[0] > 0) {
               keyboardCursor[0]--;
            }
         } else if (e.getKeyCode() == e.VK_SPACE || e.getKeyCode() == e.VK_ENTER) {
            if (keyboard[keyboardCursor[1]][keyboardCursor[0]].equals("<-")) { 
               if (currentName.length() > 0) {
                  currentName = currentName.substring(0, currentName.length()-1);
               }
            } else if (keyboard[keyboardCursor[1]][keyboardCursor[0]].equals("_")) {
               if (currentName.length() < 5) {
                  currentName = currentName + " ";
               }
            } else if (keyboard[keyboardCursor[1]][keyboardCursor[0]].equals("->")) {
               saveScore();
               getLeaderboard();
               resetVars();
            } else if (currentName.length() < 5) {
               currentName = currentName + keyboard[keyboardCursor[1]][keyboardCursor[0]];
            }
         }
      }
      
      // Uses keyboard input to type the player's name
      // Parameters:
      //    KeyEvent e - the key that was pressed
      public void keyboardTyping (KeyEvent e) { 
         if ((e.getKeyCode() >= 44 && e.getKeyCode()<=96) || e.getKeyCode() == 32) {
            if (repeatKey == false) {
               if (currentName.length() < 5) {
                  currentName = currentName + (char)e.getKeyCode();
               }
               repeatKey = true;
            } else if (repeatKey == true) {
               repeatKey = false;
            }
         } else if (e.getKeyCode() == 8) { 
            if (currentName.length() > 0) {
               currentName = currentName.substring(0, currentName.length()-1);
            }
         } else if (e.getKeyCode() == 10) {
            saveScore();
            resetVars();
         }
      }
   }
}