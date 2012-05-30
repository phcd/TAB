package intentmedia.dilip.codingTest;

import java.util.Arrays;
import java.util.List;

/**
 * This class contains all information about all the products. Use this class to add a new entry (Product) to the system.
 * @author Dilip
 *
 */
public enum Product { 
	PdtA('A',2.00,4,7.00), PdtB('B',12.00,1,0), PdtC('C',1.25,6,6), PdtD('D',0.15,1,0);
	
	private char pdtName;
	private int pdtQty;
	private double pdtUnitPrice;
	private int pdtVolumeQty;
	private double pdtVolumePrice;
	
	private Product(char name, double pdtUnitPrice, int pdtVolumeQty, double pdtVolumePrice) {
		this.pdtName = name;
		this.pdtUnitPrice = pdtUnitPrice;
		this.pdtVolumeQty = pdtVolumeQty;
		this.pdtVolumePrice = pdtVolumePrice;
		this.value = Arrays.asList(name+"");
	}
	
	private final List<String> value;
	
	Product(String ...values) {
	        this.value = Arrays.asList(values);
	    }

	    public List<String> getValues() {
	        return value;
	    }
	    
	public char getPdtName() {
		return pdtName;
	}

	public int getCount() {
		return pdtQty;
	}

	public void setCount(int pdtQty) {
		this.pdtQty = pdtQty;
	}

	public double getPdtPrice() {
		return pdtUnitPrice;
	}

	public void setPdtPrice(double pdtPrice) {
		this.pdtUnitPrice = pdtPrice;
	}

	public int getPdtVolumeQty() {
		return pdtVolumeQty;
	}

	public void setPdtVolumeQty(int pdtVolumeQty) {
		this.pdtVolumeQty = pdtVolumeQty;
	}

	public double getPdtVolumePrice() {
		return pdtVolumePrice;
	}

	public void setPdtVolumePrice(double pdtVolumePrice) {
		this.pdtVolumePrice = pdtVolumePrice;
	}
	
}

