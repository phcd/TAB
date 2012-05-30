package hw.hw1;

/* OpenCommercial.java */

import java.net.*;
import java.io.*;

/**  A class that provides a main function to read five lines of a commercial
 *   Web page and print them in reverse order, given the name of a company.
 */

class OpenCommercial {

  /** Prompts the user for the name X of a company (a single string), opens
   *  the Web site corresponding to www.X.com, and prints the first five lines
   *  of the Web page in reverse order.
   *  @param arg is not used.
   *  @exception Exception thrown if there are any problems parsing the 
   *             user's input or opening the connection.
   */
  public static void main(String[] arg) throws Exception {
//
//    BufferedReader keyboard;
//    String inputLine;
//
//    keyboard = new BufferedReader(new InputStreamReader(System.in));
//
//    System.out.print("Please enter the name of a company (without spaces): ");
//    System.out.flush();        /* Make sure the line is printed immediately. */
//    inputLine = keyboard.readLine();
//
//    /* Replace this comment with your solution.  */
//    String url ="http://www."+inputLine+".com/" ;
//    System.out.println(url);
//    URL targetURL = new URL(url);
//    
//    BufferedReader webpg = new BufferedReader(new InputStreamReader(targetURL.openStream()));
//    String line1 = webpg.readLine();
//    String line2 = webpg.readLine();
//    String line3 = webpg.readLine();
//    String line4 = webpg.readLine();
//    String line5 = webpg.readLine();
//    System.out.println(line5 + "\n" + line4 + "\n" + line3 + "\n" + line2 + "\n" + line1);
	  StringBuffer s1 = new StringBuffer();
	  s1.append("hi");
	  StringBuffer s2;
	  s2=s1;
	  System.out.println(s1 +" " + s2);
	  s1 = s1.append("!!!");
	  System.out.println("******************");
	  System.out.println(s1 +" " + s2);
    

  }
}
