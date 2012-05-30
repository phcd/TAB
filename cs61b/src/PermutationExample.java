import java.util.*;

public class PermutationExample {
        public static void main(String args[]) throws Exception {
//                Scanner input = new Scanner(System.in);
//                System.out.print("Enter String: ");
//                String chars = input.next();
                test("cat");
                showPattern("", "cat");
        }

        private static void test(String chars) {
        	for (int i = 0; i < chars.length(); i++) {
                try {
                        String newString = chars.substring(0, i) + "-" 
                                        + chars.substring(i + 1);
                        System.out.println("**"+newString);
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }
			
		}

		public static void showPattern(String st, String chars) {
                if (chars.length() <= 1)
                        System.out.println(st + chars);
                else
                        for (int i = 0; i < chars.length(); i++) {
                                try {
                                        String newString = chars.substring(0, i)
                                                        + chars.substring(i + 1);
                                        showPattern(st + chars.charAt(i), newString);
                                } catch (Exception e) {
                                        e.printStackTrace();
                                }
                        }
        }
}

