

import java.io.*;
import java.lang.*;
import java.util.*;


// This class represents a score on the leaderboard of LaserSnake
public class SnakeScore implements Comparable {
   int score;
   String name;
   
   // Constructs a new snakeScore object
   // Parameters:
   //    String nameIn - the name for the score
   //    int scoreIn - the score for it to be set to
   public SnakeScore (String nameIn, int scoreIn) {
      score = scoreIn;
      name = nameIn;
   }
   
   public int compareTo (Object o) {
      SnakeScore s = (SnakeScore)o;
      return this.score - s.score;
   }
}