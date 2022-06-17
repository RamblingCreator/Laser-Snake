import java.util.*;
import java.io.*;
import java.awt.*;

// This class represents the lasers in the game Laser Snake.
// It stores the position and direction of the laser.
public class SnakeLaser {
   int x;
   int y;
   int laserDirection;
   
   // Constructs a new SnakeLaser in front of the snake's head
   // Parameters:
   //    int[] snakePosition - The position of the snake's head
   //    int snakeDirection - The direction the snake is facing.
   //    int size - The size of one tile
   public SnakeLaser (int[] snakePosition, int snakeDirection, int size) {
      x = snakePosition[0];
      y = snakePosition[1];
      laserDirection = snakeDirection;
      
      int[][] newPositions = new int[][]{{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
      
      //int offsetDistance = size*3/5;
      int offsetDistance = size/4;
      x += newPositions[laserDirection][0]*offsetDistance;
      y += newPositions[laserDirection][1]*offsetDistance;
   }
   // Tests if the laser is in a valid position
   // Parameters:
   //    int boardSize - The size of the game's window
   //    ArrayList<Integer[]> body - The positions of the snake body segments
   //    SnakeEnemy[] enemies - The enemies on the board
   //    int size - The size of one tile
   public boolean isValid (int boardSize, ArrayList<Integer[]> body, SnakeEnemy[] enemies, int size) {
      boolean isValid = true;
      if (x < 0 || x >= boardSize || y < 0 || y >= boardSize) {
         isValid = false;
      } else if (containsIntegerArray(body, new int[]{x, y})) {
         //isValid = false;
      }
      
      for (int i = 0; i < enemies.length; i++) {
         int[] enemyPos = new int[]{enemies[i].tilePosition[0]*size, enemies[i].tilePosition[1]*size};
         
         if (y >= enemyPos[1]+size/4 && y < enemyPos[1]+size/4+size/2 && x == enemyPos[0]) {
            if (enemies[i].health > 0) {
               isValid = false;
            }
         }
         if (x >= enemyPos[0]+size/4 && x < enemyPos[0]+size/4+size/2 && y == enemyPos[1]) {
            if (enemies[i].health > 0) {
               isValid = false;
            }
         }
      }
      return (isValid);
   }
   
   // Moves the laser in it's current direction
   // Parameters:
   //    int smoothness - the amount of segments in one tile
   //    int boardSize - The size of the game's window
   //    ArrayList<Integer[]> body - The positions of the snake body segments
   //    SnakeEnemy[] enemies - The enemies on the board
   //    int size - The size of one tile
   public void moveLaser(int smoothness, int boardSize, ArrayList<Integer[]> body, SnakeEnemy[] enemies, int size){
      int[][] newPositions = new int[][]{{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
      smoothness -= smoothness/2;
      
      x += newPositions[laserDirection][0]*size/smoothness;
      y += newPositions[laserDirection][1]*size/smoothness;
      
      if (x < 0 || x >= boardSize || y < 0 || y >= boardSize) {
         laserDirection = -1;
      } else if (containsIntegerArray(body, new int[]{x, y})) {
         //laserDirection = -1;
      }
      
      int testX = x;
      int testY = y;
      if (laserDirection == 2) {
         testY+=size;
      } else if (laserDirection == 1) {
         testX+=size;
      }
      if (laserDirection == 0 || laserDirection == 2) {
         testX += size/2;
      } else if (laserDirection == 1 || laserDirection == 3) {
         testY += size/2;
      }
         
      for (int i = 0; i < enemies.length; i++) {
         int[] enemyPos = new int[]{enemies[i].tilePosition[0]*size, enemies[i].tilePosition[1]*size};
         if (testX >= enemyPos[0]+size/4 && testX < enemyPos[0]+size/4+size/2 && testY >= enemyPos[1]+size/4 && testY < enemyPos[1]+size/4+size/2) {
            if (enemies[i].health > 0) {
               enemies[i].health--;
               laserDirection = -1;
            }
         }
      }
   }
   
   // Checks if an ArrayList contains the given integer array
   // Parameters:
   //    ArrayList<Integer[]> list - the list being checked
   //    int[] thing - the thing being checked for
   
   public boolean containsIntegerArray (ArrayList<Integer[]> list, int[] thing) {
      boolean contains = false;
      for (int i = 0; i < list.size(); i++) {
         if (list.get(i)[0] == thing[0] && list.get(i)[1] == thing[1]) {
            contains = true;
         }
      }
      return(contains);
   }
}