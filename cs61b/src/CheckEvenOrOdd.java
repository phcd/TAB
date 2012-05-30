import java.util.Scanner;

public class CheckEvenOrOdd {
	  public static int EvenOrOdd2(int num)
	    {
	        return (num&1);
	    }

	    public static void main(String[] args){
	        System.out.println("Enter an integer to check for Even/Odd: ");
	        Scanner myscan = new Scanner(System.in);
	        int inputNo = myscan.nextInt();

	        System.out.println("The entered number was: "+(CheckEvenOrOdd.EvenOrOdd2(inputNo)==0?"Even":"Odd"));

	    }

	
}
