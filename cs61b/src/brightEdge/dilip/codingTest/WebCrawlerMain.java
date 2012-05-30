package brightEdge.dilip.codingTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class WebCrawlerMain {

	public static void main(String args[]) {
		try {
			URL targetURL = new URL("http://www.shopping.com");
			BufferedReader webpg = new BufferedReader(new InputStreamReader(targetURL.openStream()));
			System.out.println(webpg.readLine());
			System.out.println(webpg.readLine());
			System.out.println(webpg.readLine()); 
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
