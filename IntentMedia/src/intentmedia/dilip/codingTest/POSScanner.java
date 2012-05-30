package intentmedia.dilip.codingTest;

import java.util.Scanner;

/**
 * Tester class which gets user inputs and display the total price of the Products entered.
 * @author Dilip
 *
 */
public class POSScanner {

	public static void main(String args[]) {
		System.out.println("Please enter the scanned products");
		Scanner input = new Scanner(System.in);
		String productsList = input.nextLine().toUpperCase();
		
		Terminal terminal = new ProductPriceCalculatorTerminal();
			try {
				terminal.scan(productsList);
				terminal.displayPrice();
			} catch (Exception e) {
				if(e.getMessage()!=null)
					System.out.println(e.getMessage());
				else
					System.out.println("Some Exception occured!");
			}
			
	}

	
}
