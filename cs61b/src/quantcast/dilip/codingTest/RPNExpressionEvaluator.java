package quantcast.dilip.codingTest;

import java.util.Arrays;
import java.util.Stack;

/**
 * This class provides methods to calculate the value from an input in RPN
 * @author Dilip
 * 
 */
public class RPNExpressionEvaluator {

	public double calculateCellValue(String expression) {
		double value = 0;
		Stack<String> evaluationStack = new Stack<String>();
		evaluationStack
				.addAll(Arrays.asList(expression.trim().split("[ \t]+")));
		try {
			value = evaluateRPN(evaluationStack);
			if (!evaluationStack.empty())
				throw new Exception();
//			System.out.println("value - " + value);
		} catch (Exception e) {
			System.out.println("Some exception occured");
			System.out.println(e.getMessage());

		}
		return value;
	}

	private double evaluateRPN(Stack<String> evaluationStack) throws Exception {
		String currentStackValue = evaluationStack.pop();
		double x, y;
		try {
//			System.out.println("currentStackValue ==" + currentStackValue);
			x = Double.parseDouble(currentStackValue);
//			System.out.println("Inside evaluateRPN x =" + x);
		} catch (Exception e) {
			y = evaluateRPN(evaluationStack);
			x = evaluateRPN(evaluationStack);
			if (currentStackValue.equals("+"))
				x += y;
			else if (currentStackValue.equals("-"))
				x -= y;
			else if (currentStackValue.equals("*"))
				x *= y;
			else if (currentStackValue.equals("/"))
				x /= y;
			else
				throw new Exception("Undefined Operation");
		}
		return x;
	}
}
