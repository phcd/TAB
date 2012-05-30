/**
 * 
 */
package quantcast.dilip.codingTest;

import java.util.Scanner;

/**
 * @author Dilip
 *
 */
public class Spreadsheet {

	private static int rows =0; 
	private static int cols =0;
	
	private static String[][] entries;
	private static Object[][] output;
	
	public static void main(String[] args) {
		System.out.println("Welcome to the spreadsheet calculator!");
		
		getInputsFromUser();
		
		printEntries();
		
		try {
			output = new SpreadsheetEvaluator().evaluate(entries);
		} catch (Exception e) {
			System.out.println("\n"+e.getMessage());
			System.out.println("Please check your input for cyclic dependecies!");
			return;
		} 
		printOutput();
	}

	/**
	 * This method takes the input for the spreadsheet
	 * and fills in the entries of the spreadsheet to the input array.
	 */
	public static void getInputsFromUser() {
		System.out.println("Please enter inputs :");
		
		Scanner input = new Scanner(System.in);
			cols = input.nextInt();
			rows = input.nextInt();
			input.nextLine(); 
			entries = new String[rows][cols];
			
			for (int i = 0; i <rows; i++) {
				for (int j = 0; j < cols; j++) {
					entries[i][j] = input.nextLine().toUpperCase();
				}
				
			}

	}
	
	/**
	 * Prints the entries of the spreadsheet
	 */
	public static void printEntries() {
		System.out.println("\nPrinting Input\n");
		System.out.println(cols +" " + rows);
		for (int i = 0; i <rows; i++) {
			for (int j = 0; j < cols; j++) {
				System.out.println(entries[i][j]);
			}
		}
	}
	
	public static void printOutput() {
		System.out.println("\nPrinting output\n");
		System.out.println(cols +" " + rows);
		for (int i = 0; i <output.length; i++) {
			for (int j = 0; j < output[0].length; j++) {
				System.out.println(String.format("%.5f", output[i][j]));
			}
			
		}
	}

}
