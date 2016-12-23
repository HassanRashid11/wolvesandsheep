package pathfinding;



//class that defines cell, hcost, final cost, row, and col
public class Cell{  
    
    public float hCost = 0;
    public float finalCost = 0; //G+H
    public int row, col;
    public Cell parent; 
//constructor        
    public Cell(int row, int col){
            this.row = row;
            this.col = col; 
        }
    }
