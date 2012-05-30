package quantcast.dilip.codingTest;

import java.util.HashMap;
import java.util.HashSet;

/**
 * This class evaluates the various expressions of the spreadsheet entries
 * @author Dilip
 *
 */
public class SpreadsheetEvaluator {
	final String rowNumbers = "abcdefghijklmnopqrstuvwxyz".toUpperCase();
	String referenceValue ="";
	Exception cyclicRefernceException = new Exception("Cyclic Dependency found");
	
	final char[] letters = rowNumbers.toCharArray();
	HashMap<String,Object> hash= new HashMap<String,Object>();
	
	Object[][] evaluatedArray;
	
	public Object[][] evaluate(String[][] entries) throws Exception {
		return constructCellValues(entries);
	}


	private Object[][] constructCellValues(String[][] entries) throws Exception {
		String expressionWithValues ="";
		if(entries.length > 0)
			evaluatedArray = new Object[entries.length][entries[0].length]; 
		for (int i = 0; i < entries.length; i++) {
			for (int j = 0; j < entries[i].length; j++) {
				
				Object obj = hash.get(entries[i][j]);
				if(obj == null || (!isNum(obj.toString()))) {
					referenceValue ="";
					try {
						expressionWithValues = evaluateReferences(entries[i][j], entries);
						obj = new RPNExpressionEvaluator().calculateCellValue(expressionWithValues);
					}
					catch(Exception e) {
						throw cyclicRefernceException;
					}
				}
					hash.put(letters[i]+""+ (j+1), obj); 
				evaluatedArray[i][j] = hash.get(letters[i]+""+ (j+1));
			
			}
		}
		return evaluatedArray;
		
	}
	


	private String evaluateReferences(String reference, String[][] entries) throws Exception {
		HashSet<String> setToCheckCyclicDependency = new HashSet<String>();
		if(setToCheckCyclicDependency.contains(reference) || reference.equals(hash.get(reference))) {
			throw cyclicRefernceException;
		}
		else
			setToCheckCyclicDependency.add(reference);
		String result = "";
		String[] cellElements = (reference).split(" ");
		if(cellElements.length == 1)
			result = evaluateSingleReference(cellElements[0], entries);
		else {
			for (int i = 0; i < cellElements.length; i++) {
				if(isNum(cellElements[i]) && isOperator(cellElements[i]))
					result = " " + cellElements[i];
				else {
					result = " " + evaluateSingleReference(cellElements[i], entries);
				}
				
			}
		}
		return result;
}
	
	private String evaluateSingleReference(String reference, String[][] entries) throws Exception {
		
		if(!isOperator(reference)) {
			try {
				referenceValue += " "+Double.parseDouble((reference));
			} catch (NumberFormatException nfe) {
				String newReference = entries[rowNumbers.indexOf(reference.substring(0, 1))][Integer.parseInt(reference.substring(1))-1];
				if(isNum(newReference))
					hash.put(reference, Double.parseDouble(newReference));
				else
					hash.put(reference, newReference);
					evaluateReferences(newReference, entries);
			}
		}
		else
			referenceValue += " "+ reference;
		
		return referenceValue;
	}
	public boolean isNum(String s) {
		try {
		Double.parseDouble(s);
		}
		catch (NumberFormatException nfe) {
		return false;
		}
		return true;
		}
	
	public boolean isOperator(String s) {
		if(!s.equals("+") && !s.equals("-") && !s.equals("*") && !s.equals("/")) {
			return false;
		}
		else 
			return true;
	}
}
	
