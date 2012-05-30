//
// Class:	   MineSweeper
// Written By: Frank Lee
// Date:	   November 2010
//
// Character based implementation of mine sweeper game.
// Uses a nested class Tile to group Tile variables (aka struct)
// MineSweeper class implements most of the game operation.
//
// BETA: neighboring bombs and clearing spaces implemented.
// 
public class MineSweeper {
	// main method
	public static void main(String[] args){
		int	width, depth, bombs, result;						// game setup		
		boolean gameOver = false;
		java.util.Scanner	console;					// Java	console	Scanner
		console	= new java.util.Scanner(System.in);
		System.out.println("MINE SWEEPER");	  
		// Input data	
		System.out.print("Enter Mine Field Width:");	  
		width = console.nextInt();
		System.out.print("Enter Mine Field Depth:");	  
		depth = console.nextInt();
		System.out.print(" Enter Number of Bombs:");	  
		bombs = console.nextInt();
		// create MineSweeper instance
		MineSweeper mGame = new MineSweeper(width, depth, bombs);
		
		System.out.println("PLAY MINE SWEEPER!!");	 
		
		while (!gameOver) {
			mGame.printTiles();
			mGame.printTileValues(); //DEBUG
			long start = System.currentTimeMillis();   // a System method for elapsed time monitoring
			System.out.println("Enter Row and Column:");	  
			depth = console.nextInt();	
			width = console.nextInt();
			result = mGame.play(depth, width);
			if (result == BAD_COORDINATE) {
				System.out.println("** Incorrect Coordinate - Try Again **");	 
			} else if (result == DONE) {
				long end = System.currentTimeMillis();   // a System method for elapsed time monitoring
				mGame.printTiles();
				System.out.println("Congratulations, mine field cleared.");	
				System.out.println("Elapsed Time = " + ((end-start)/1000/60) + " minutes and " + ((end-start)/1000%60) + " seconds.");
				gameOver = true;
			} else if (result == LOSE) { 
				mGame.printTiles();
				System.out.println("KA BOOM, Game Over.");	
				gameOver = true;
			}
		}
		//System.out.println("Program Done");	 								
	}
	
	// class constants
	private final char  COVERED = 'X';
	private final char    CLEAR = ' ';
	private final int      BOMB = 9;
	private final int      OPEN = 0;
	private final int PROCESSED = -1;	
	private static final int CONTINUE = 0;
	private static final int BAD_COORDINATE = -1;
	private static final int LOSE = -10;	
	private static final int DONE = 10;	
	
	// (A nested Tile class)
	// Class Tile
	// Used to monitor program values and user display information.
	// (with public class variables, a.k.a struct)
	private class Tile {
		public int value;		// program information
		public char display;	// user display info
		// constructor, default settings
		public Tile () {
			value = PROCESSED;
			display = CLEAR;
		}
	}
	
	// class variables
	private Tile[][] tiles;    // 2D array, choosing to view model as [rows][columns]
	private int rowSize, colSize, numBombs;
	
	// Method: printTiles
	// Show tile chars
	public int play(int row, int col) {
		int response = CONTINUE;
		// check for bomb
		
		if (row<1 || row >rowSize || col<1 || col>colSize) {
			response = BAD_COORDINATE;	
		} else if (tiles[row][col].display!=COVERED) {
			response = BAD_COORDINATE;
		} else if (tiles[row][col].value==BOMB) {
			response = LOSE;
			uncoverBombs();
		} else {
			uncoverTile(row, col);
			if (gameOver()) {
				response = DONE;
			}
		}
		// print Tile.display
		return (response);
	}
	
	// Constructor: setup 2D array, initialize bombs, initialize variables.
	public MineSweeper(int w, int d, int b){
		rowSize = d;
		colSize = w;
		numBombs = b;
		// create arrays with a single element buffer around the array rectangle
		// i.e. the top left corner of the playing area is [1][1]	
		tiles = new Tile[rowSize+2][colSize+2];
		// fill to buffer as PROCESSED
		for (int row=0; row<rowSize+2; row++) {
			for (int col=0; col<colSize+2; col++){
				tiles[row][col] = new Tile();
			}
		}		
		//  clear field values
		for (int row=1; row<=rowSize; row++) {
			for (int col=1; col<=colSize; col++){
				tiles[row][col].value=0;
				tiles[row][col].display=COVERED;
			}
		}			
		initialize();
	}
	
	// Method: printTiles
	// Show tile chars
	public void printTiles() {
		// print Tile.display
		for (int row=0; row<rowSize+2; row++) {
			for (int col=0; col<colSize+2; col++){
				System.out.printf("%2c", tiles[row][col].display);
			}
			System.out.println();
		}	
	}
	// Method: printTileValues
	// Show tile value
	public void printTileValues() {
		// print Tile.display
		for (int row=0; row<rowSize+2; row++) {
			for (int col=0; col<colSize+2; col++){
				System.out.printf("%2d", tiles[row][col].value);
			}
			System.out.println();
		}	
	}	
	
	// Method: uncoverBombs
	// Game is over, uncover all bombs
	public void uncoverBombs() {
		for (int row=1; row<=rowSize; row++) {
			for (int col=1; col<=colSize; col++){
				if (tiles[row][col].value==BOMB){
					tiles[row][col].display=toDisplayChar(tiles[row][col].value);
				}
			}
		}	
	}
	
	// Method: gameOver
	// check if player has one; number of tiles not processed = numBombs
	public boolean gameOver() {
		boolean gameover = false;
		int count = 0;
		for (int row=1; row<=rowSize; row++) {
			for (int col=1; col<=colSize; col++){
				if (tiles[row][col].display==COVERED){
					count++;
				}
			}
		}
		if (count==numBombs) {
			gameover = true;
		}
		return(gameover);
	}
	
	// Method: initialize
	// Fill field with bombs and compute neighboring bombs information
	private void initialize() {	
		java.util.Random rand = new java.util.Random();		// Random class	
		int a, b, count=0;
		

		while (count<numBombs) {
			a = rand.nextInt(rowSize)+1; // pick a row (+1 for buffer compensation)
			b = rand.nextInt(colSize)+1; // pick a col
			// confirm no Bomb is already at the position
			if (tiles[a][b].value != BOMB) {
				// place a Bomb at this position
				tiles[a][b].value = BOMB;
				count++;
			} else {
				//System.out.println("Repeat BOMB");
			}
		}
		
		//
		// TO DO: 
		// Fill tiles with number of neighboring bombs information
		// (quadruple nested loops with conditional)
		for (int row=1; row<=rowSize; row++) {
			for (int col=1; col<=colSize; col++){
				// if no bomb at this position ...
				if (tiles[row][col].value != BOMB) { // create this conditional
					// ... count number of bombs in neighboring tiles
					for (int rowShift=-1; rowShift<=1; rowShift++) {
						for (int colShift=-1; colShift<=1; colShift++){
							// 
							if (tiles[row+rowShift][col+colShift].value==BOMB) {
								tiles[row][col].value++;
							}
							
							
						}
					} // end of neighboring Bomb count
				} // end of if
			}
			
		} // end of filling 				
	}
	
	// Method: uncoverTile
	// Update Tile.display to show selected tile, r,c.
	public void uncoverTile(int r, int c) {
		//java.util.Scanner	console;					// DEBUG
		//console	= new java.util.Scanner(System.in); // DEBUG
		
		tiles[r][c].display=toDisplayChar(tiles[r][c].value);
		// 
		// TO DO: 
		// Use recursive concepts to display the entire open area if r,c value is OPEN
		//
		if (tiles[r][c].value==OPEN) {
			// recursively uncoverTile to all neighboring tiles
			for (int rowShift=-1; rowShift<=1; rowShift++) {
				for (int colShift=-1; colShift<=1; colShift++){
					// ?
					
					if(tiles[r+rowShift][c+colShift].display == COVERED){
						if( (rowShift*colShift) !=0){ 
							if(tiles[r+rowShift][c+colShift].value != 0){ //diagonal
								uncoverTile(r+rowShift,c+colShift);
							}
						} else{ //adjacent
							uncoverTile(r+rowShift,c+colShift);
						}
						
					} else{
						//end recursion
					}
				}
			} 	
		}
		
		//console.nextLine(); // DEBUG PAUSE
	}
	
	// Method: toDisplayChar
	// convert Tile.value to Tile.display character
	private char toDisplayChar(int val) {
		char c;
		if (val==BOMB) {
			c = 'B';		
		} else if (val==OPEN) {
			c = CLEAR;	
		} else if (val==1) {c = '1';	
		} else if (val==2) {c = '2';	
		} else if (val==3) {c = '3';	
		} else if (val==4) {c = '4';	
		} else if (val==5) {c = '5';	
		} else if (val==6) {c = '6';	
		} else if (val==7) {c = '7';	
		} else if (val==8) {c = '8';		
		} else { 
			c = 'E'; // Error
		}
		return (c);
	}
	
}
