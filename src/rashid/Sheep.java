/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rashid;

/**
 *
 * @author Hassan
 */
import pathfinding.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import was.GameBoard;
import was.GameLocation;
import was.Move;
import was.Player;
import java.util.Comparator;
import java.util.PriorityQueue;


/**
 *
 * @author Hassan
 */
public class Sheep extends was.SheepPlayer {
    
    private static Random rand = new Random();
    GameBoard board = null;
    Move direction = null; // direction we're taking
    int pas_Index; //closest pasture location
    GameLocation sh_Loc; //sheep location
    
    //basic and diagonal cost
    int DIAG_COST = 14;
    int NS_COST = 10;
    
    Cell [][] map = new Cell[0][0];
    
    //use priorityqueue so its faster to get cells
    static PriorityQueue<Cell> openList;
     
    boolean closed_Cells[][];
    int start_row, start_col;
    int end_row, end_col;
 
    void setUnWalkable(int row, int col){
        map[row][col] = null;
    }
    
    void setBeginCell(int row, int col){
        start_row = row;
        start_col = col;
    }
    
    void setEndCell(int row, int col){
        end_row = row;
        end_col = col; 
    }
    
    void update_cost(Cell current, Cell tar, float cost){
        if(tar == null || closed_Cells[tar.row][tar.col])return;
        float t_final_cost = tar.hCost+cost;
        
        boolean Open = openList.contains(tar);
        if(!Open || t_final_cost<tar.finalCost){
            tar.finalCost = t_final_cost;
            tar.parent = current;
            if(!Open)openList.add(tar);
        }
    }
    //start actual astar part
    void startAS(){ 
        
        openList.add(map[start_row][start_col]);
        
        Cell current;
        
        while(true){ 
            current = openList.poll();
            if(current==null)break;
            closed_Cells[current.row][current.col]=true; 

            if(current.equals(map[end_row][end_col])){
                return; 
            } 

            Cell t;  
            if(current.row-1>=0){
                t = map[current.row-1][current.col];
                update_cost(current, t, current.finalCost+NS_COST); 

                if(current.col-1>=0){                      
                    t = map[current.row-1][current.col-1];
                    update_cost(current, t, current.finalCost+DIAG_COST); 
                }

                if(current.col+1<map[0].length){
                    t = map[current.row-1][current.col+1];
                    update_cost(current, t, current.finalCost+DIAG_COST); 
                }
            } 

            if(current.col-1>=0){
                t = map[current.row][current.col-1];
                update_cost(current, t, current.finalCost+NS_COST); 
            }

            if(current.col+1<map[0].length){
                t = map[current.row][current.col+1];
                update_cost(current, t, current.finalCost+NS_COST); 
            }

            if(current.row+1<map.length){
                t = map[current.row+1][current.col];
                update_cost(current, t, current.finalCost+NS_COST); 

                if(current.col-1>=0){
                    t = map[current.row+1][current.col-1];
                    update_cost(current, t, current.finalCost+DIAG_COST); 
                }
                
                if(current.col+1<map[0].length){
                   t = map[current.row+1][current.col+1];
                    update_cost(current, t, current.finalCost+DIAG_COST); 
                }  
            }
        } 
    }
        
    @Override
    public void initialize() {
        // you cannot call "getGameBoard" in the constructor, as the
        // game board is created after all the players.
        findClosestPas();
    }
     
    public void findClosestPas()
    {
          board = getGameBoard();
        
        
        ArrayList pas_Pos;
        pas_Pos=board.getPasturePositions();
               
        sh_Loc=getLocation();

       
        double dist=1000;
        
        for (int i=0;i<pas_Pos.size();i++)
        {
            GameLocation loc;
            loc=(GameLocation)pas_Pos.get(i);
            double diffx=sh_Loc.x-loc.x;
            double diffy=sh_Loc.y-loc.y;
            double loc_dist=Math.sqrt(diffx*diffx+diffy*diffy);
            
            if (loc_dist <dist)
            {
                dist=loc_dist;
                pas_Index=i;
            }
            //System.out.println(loc.x);
            //System.out.println(loc.y);
            //System.out.println("-----");
        }
        
    }
        
        
    public List<Cell> calcPath()   {
        int width=board.getRows();
        int height=board.getCols();
      
        map = new Cell[width][height];
        
        closed_Cells = new boolean[width][height];
        
        openList = new PriorityQueue<Cell>(201, new Comparator<Cell>(){
                @Override
                public int compare(Cell a, Cell b){
                    return a.finalCost < b.finalCost ? -1:
                    a.finalCost > b.finalCost ? 1 : 0;
                }
            });
        sh_Loc=getLocation();
        GameLocation wolf_Pos = board.getWolfPosition();
        //System.out.println(wolf_Pos.x + " " + wolf_Pos.y);
        //myMap.setWalkable(wolf_Pos.x, wolf_Pos.y, false);
        findClosestPas();
        ArrayList pas_Pos;
        pas_Pos=board.getPasturePositions();
        GameLocation tar_Pas=(GameLocation)pas_Pos.get(pas_Index);
        //////////////////////////////////////
        setBeginCell(sh_Loc.x, sh_Loc.y);  
        //////////////////////////////////////
        setEndCell(tar_Pas.x, tar_Pas.y); 
        
        float fearFactor=(float)0.85; //1 means sheep is very fearful, 0 means not fearful at all
        
        for(int i=0;i<width;i++){
             for(int j=0;j<height;j++){
                  map[i][j] = new Cell(i,j);
                  map[i][j].hCost = (fearFactor)*Math.abs(i-tar_Pas.x)+Math.abs(j-tar_Pas.y);
                  if (wolf_Pos != null)
                  {
                    map[i][j].hCost = map[i][j].hCost-(1-fearFactor)*(Math.abs(i-wolf_Pos.x)+Math.abs(j-wolf_Pos.y));
                  }
              }
           }
//        System.out.println(i);
//        System.out.println(j);
//        
//        System.out.println(sh_Loc.x);
//        System.out.println(sh_Loc.y);
        
        map[sh_Loc.x][sh_Loc.y].finalCost = 0;
        
        ArrayList obs_Pos=board.getObstaclePositions();
        for(int i=0;i<obs_Pos.size();i++) //repeat this loop for all sheep locations (except our sheep), wolf location, 
        {
            GameLocation loc;
            loc=(GameLocation)obs_Pos.get(i);
            setUnWalkable(loc.x,loc.y);
        }
        
        ArrayList sh_Pos=board.getSheepPositions();        
        
        for(int i=0;i<sh_Pos.size();i++) //repeat this loop for all sheep locations (except our sheep), wolf location, 
        {
            GameLocation loc;
            loc=(GameLocation)sh_Pos.get(i);
            if (loc.equals(sh_Loc) == false)   {
                setUnWalkable(loc.x,loc.y);
            }
        }   
                
        List<Cell> path=new ArrayList<Cell>();
        startAS();
        
        if(closed_Cells[end_row][end_col])
        {
            Cell current = map[end_row][end_col];
            path.add(current);
            while(current.parent!=null){
                path.add(current.parent);
                current = current.parent;
            } 
        }
        
        return path;
    }

    @Override
    public Move move() {
        
        List<Cell> path=calcPath();
        
//        for (int i = 0; i < path.size(); i++) {
//            System.out.print("(" + path.get(i).getxPosition() + ", " + path.get(i).getyPosition() + ") -> ");
//        }
        
        sh_Loc=getLocation();
        if (path.size() <= 1)
        {
            int mv_x=path.get(path.size()-1).row-sh_Loc.x;
            int mv_y=path.get(path.size()-1).col-sh_Loc.y;
            direction = new Move(mv_x,mv_y);            
        }else
        {
            int mv_x=path.get(path.size()-2).row-sh_Loc.x;
            int mv_y=path.get(path.size()-2).col-sh_Loc.y;
            direction = new Move(mv_x,mv_y);
        }
//        Scanner sc = new Scanner(System.in);
//        System.out.println("\n***\n");
//        sc.next();
       
        
        // if direction is not yet set, choose a random one
//        ArrayList pas_Pos; //pasture positions
//        pas_Pos=board.getPasturePositions();
//        GameLocation tar_Pas=(GameLocation)pas_Pos.get(pas_Index);
//        sh_Loc=getLocation();
//        double uv_x=tar_Pas.x-sh_Loc.x;
//        double uv_y=tar_Pas.y-sh_Loc.y;
//        
//        if (Math.abs(uv_x) > Math.abs(uv_y))
//        {
//            uv_y=uv_y/Math.abs(uv_x);
//            uv_x=uv_x/Math.abs(uv_x);
//        }else
//        {
//            uv_x=uv_x/Math.abs(uv_y);
//            uv_y=uv_y/Math.abs(uv_y);
//        }
//        //System.out.println(tar_Pas);
//        //System.out.println(sh_Loc);
//        //System.out.println("here is uv_x: " + uv_x);
//        //System.out.println("-------");
//        //System.out.println("here is uv_y: " + uv_y);
//        
//        int mv_x = 0;
//        int mv_y = 0;
//        //if (direction == null) {
//        //if uv_x and uv_y are positive we have to move towards south east
//        if ( (uv_x >=0 && uv_y >= 0) && (Math.abs(uv_x-uv_y) < .15) ){
//            mv_x=1;
//            mv_y=1;
//            direction = new Move(mv_x,mv_y);
//            direction = direction.scaledToLength(getMaxAllowedDistance()).quantized();
//        }else if ( (uv_x >=0 && uv_y >= 0) && (Math.abs(uv_x) > Math.abs(uv_y)) ){
//            mv_x=1;
//            mv_y=0;
//            direction = new Move(mv_x,mv_y);
//            direction = direction.scaledToLength(getMaxAllowedDistance()).quantized();
//        }else if ( (uv_x >=0 && uv_y >= 0) && (Math.abs(uv_x) < Math.abs(uv_y)) ){
//            mv_x=0;
//            mv_y=1;
//            direction = new Move(mv_x, mv_y);
//            direction = direction.scaledToLength(getMaxAllowedDistance()).quantized();
//        }
//        
//        //if uv_x and uv_y are negative we have to move towards 
//        else if ( (uv_x < 0 && uv_y < 0) && (Math.abs(uv_x-uv_y) < .15) )  {
//            mv_x=-1;
//            mv_y=-1;
//            direction = new Move(mv_x,mv_y);
//            direction = direction.scaledToLength(getMaxAllowedDistance()).quantized();
//        }else if ( (uv_x < 0 && uv_y < 0) && (Math.abs(uv_x) > Math.abs(uv_y)) ){
//            mv_x=-1;
//            mv_y=0;
//            direction = new Move(mv_x, mv_y);
//            direction = direction.scaledToLength(getMaxAllowedDistance()).quantized();
//        }else if ( (uv_x < 0 && uv_y < 0) && (Math.abs(uv_x) < Math.abs(uv_y)) ){
//            mv_x=0;
//            mv_y=-1;
//            direction = new Move(mv_x, mv_y);
//            direction = direction.scaledToLength(getMaxAllowedDistance()).quantized();
//        }
//        
//        //if uv_x is negative and uv_y is positive we move towards 
//        else if ( (uv_x <= 0 && uv_y >= 0) && (Math.abs(uv_x-uv_y) < .15) )  {
//            mv_x=-1;
//            mv_y=1;
//            direction = new Move(mv_x,mv_y);
//            direction = direction.scaledToLength(getMaxAllowedDistance()).quantized();
//        }else if ( (uv_x <= 0 && uv_y >= 0) && (Math.abs(uv_x) > Math.abs(uv_y)) ){
//            mv_x=-1;
//            mv_y=0;
//            direction = new Move(mv_x, mv_y);
//            direction = direction.scaledToLength(getMaxAllowedDistance()).quantized();
//        }else if ( (uv_x <= 0 && uv_y >= 0) && (Math.abs(uv_x) < Math.abs(uv_y)) ){
//            mv_x=0;
//            mv_y=1;
//            direction = new Move(mv_x, mv_y);
//            direction = direction.scaledToLength(getMaxAllowedDistance()).quantized();
//        }
//        
//        //if uv_x is positive and uv_y is negative 
//        else if ((uv_x > 0 && uv_y < 0) && (Math.abs(uv_x-uv_y) < .15) )  {
//            mv_x=1;
//            mv_y=-1;
//            direction = new Move(mv_x, mv_y);
//            direction = direction.scaledToLength(getMaxAllowedDistance()).quantized();
//        }else if ( (uv_x > 0 && uv_y < 0) && (Math.abs(uv_x) > Math.abs(uv_y)) ){
//            mv_x=1;
//            mv_y=0;
//            direction = new Move(mv_x, mv_y);
//            direction = direction.scaledToLength(getMaxAllowedDistance()).quantized();
//        }else if ( (uv_x > 0 && uv_y < 0) && (Math.abs(uv_x) < Math.abs(uv_y)) ){
//            mv_x=0;
//            mv_y=-1;
//            direction = new Move(mv_x, mv_y);
//            direction = direction.scaledToLength(getMaxAllowedDistance()).quantized();
//        }
//        
//        //sort of working 
//        boolean fixed=true;
//        
//        if ((board.getPiece(sh_Loc.x+mv_x, sh_Loc.y+mv_y) == GamePiece.OBSTACLE) || (board.getPiece(sh_Loc.x+mv_x, sh_Loc.y+mv_y) == GamePiece.WOLF) || (board.getPiece(sh_Loc.x+mv_x, sh_Loc.y+mv_y) == GamePiece.SHEEP))
//        {
//            fixed = false;
//        }       
//        //System.out.println(board.getPiece(sh_Loc.x+mv_x, sh_Loc.y+mv_y));
//        
//        while(fixed == false)
//        {
//            if ((board.getPiece(sh_Loc.x+mv_x, sh_Loc.y+mv_y) == GamePiece.OBSTACLE) || (board.getPiece(sh_Loc.x+mv_x, sh_Loc.y+mv_y) == GamePiece.WOLF) || (board.getPiece(sh_Loc.x+mv_x, sh_Loc.y+mv_y) == GamePiece.SHEEP))
//            {
//                int  n = (int) (rand.nextInt() + 2.0 * 1);
//                int  m = (int) (rand.nextInt() + 2.0 * 1);
//                mv_x=n;
//                mv_y=m;
//            }
//        
//            
//                fixed=true;
//                direction = new Move(mv_x,mv_y);
//                direction = direction.scaledToLength(getMaxAllowedDistance()).quantized();
//            
//        }
        return direction;
    }
}

        
        /* Note:
         * You may visualize a path using the visualizeTrack() method from Player.
         * The following code shows a path from the player's location to
         * location <5,5>.
         * You may visualize as many paths as you like.

            List<GameLocation> trk = new ArrayList();
            trk.add(getLocation());
            trk.add(new GameLocation(5,5));            
            removeVisualizations(); // remove all previously set tracks
            visualizeTrack(trk);
        */
