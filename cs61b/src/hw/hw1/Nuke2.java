package hw.hw1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Nuke2 {

	public static void main (String args[]) throws IOException {
		BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
		String str = keyboard.readLine();
		System.out.println(str.substring(0, 1)+str.substring(2));
	}
}
