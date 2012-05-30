package intentmedia.dilip.codingTest;

import java.util.HashMap;

/**
 * This class scans all the entered products and calculates price based on the pricing rules.
 * @author Dilip
 *
 */
public class ProductPriceCalculatorTerminal implements Terminal {
	HashMap<Character, Integer> pdtQtyMapping = new HashMap<Character, Integer>();
	double totalCost = 0;
	
	public void scan(String productsList) throws Exception {
		for(Product productSpec: Product.values()) {
			pdtQtyMapping.put(productSpec.getPdtName(), 0);
		}
		
		for (int i = 0; i < productsList.length(); i++) {
						
			if(pdtQtyMapping.get(productsList.charAt(i)) == null) {
				throw new Exception(productsList.charAt(i) +" is not a valid Product. Please add a new product in the system before scanning");
			}
			else {
				pdtQtyMapping.put(productsList.charAt(i), pdtQtyMapping.get(productsList.charAt(i))+1);
			}
		}
		
		for(Product productSpec: Product.values()) {
			totalCost += calculatePrice(productSpec,pdtQtyMapping.get(productSpec.getPdtName()));
		}
		
	}
	
	private double calculatePrice(Product productSpec, int pdtCount) {
		int qtyForVolumePrice=0;
		double cost =0;
		
			if(pdtCount >= productSpec.getPdtVolumeQty() && productSpec.getPdtVolumeQty() !=1) {
				qtyForVolumePrice = pdtCount/productSpec.getPdtVolumeQty();
				cost = productSpec.getPdtVolumePrice() * qtyForVolumePrice + (productSpec.getPdtPrice()) * (pdtCount % productSpec.getPdtVolumeQty());
			}
			else{
				cost = productSpec.getPdtPrice() * pdtCount;
			}
			if(pdtCount > 0)
				System.out.println("Product "+productSpec.getPdtName()+", Quantity - "+ pdtCount +", Total product cost is $"+cost);
			
			return cost;
	}

	public void displayPrice() {
		System.out.println("The total price of all products is $"+ totalCost);
		System.out.println("Please collect appropriate change and Please feel free to tip the attendant!");
		System.out.println("Have a nice day!!!!!!!!!!");
	}

}
