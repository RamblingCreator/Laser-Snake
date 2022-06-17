import java.awt.Graphics;
import java.awt.Color;
import java.awt.*;
import java.util.*;
import java.io.*;

// This class represents the bug enemies in laser snake
// It stores the enemy's position and it's health.
public class SnakeEnemy {
   int[] tilePosition;
   int health;
   
   // Constructs a new SnakeEnemy at a random position.
   public SnakeEnemy() {
      Random r = new Random();
      tilePosition = new int[]{r.nextInt(12), r.nextInt(12)};
      health = 1;
   }
   
   // Constructs a new SnakeEnemy at a random valid position.
   // Parameters:
   //    ArrayList<Integer[]> invalidTiles - the places that the enemy can't spawn
   
   public SnakeEnemy(ArrayList<Integer[]> invalidTiles) {
      Random r = new Random();
      boolean overlap = true;
      while (overlap) {
         overlap = false;
         tilePosition = new int[]{r.nextInt(12), r.nextInt(12)};
         for (int i = 0; i < invalidTiles.size(); i++) {
            if (invalidTiles.get(i)[0] == tilePosition[0] && invalidTiles.get(i)[1] == tilePosition[1]) {
               overlap = true;
            }
         }
      }
      health = 1;
   }
}