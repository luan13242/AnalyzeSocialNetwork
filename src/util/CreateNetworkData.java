/**
 * 
 */
package util;

import java.io.FileOutputStream;
import java.io.FileReader;

import com.opencsv.CSVReader;

/**
 * @author LU
 * 
 * This is to generate a few snowflake data to test CapGraph community algorithm.
 *
 */
public class CreateNetworkData {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	
		FileOutputStream writer = null;
		String s = "";
		
		int CLUSTER_COUNT = 4;
		
		try {
			writer = new FileOutputStream("C:\\Users\\LU\\JAVA_workspace\\SocialNetworks\\data\\SnowflakeNetwork.txt");
			
			// make the snowflakes
			int base;
			for (int i = 0; i < CLUSTER_COUNT; i++) {
				base = i * 10;
				for (int j = 1; j < 10 ; j++) {
					s = s + Integer.toString(base) + " " + Integer.toString(j+base) + "\n";
					s = s + Integer.toString(j+base) + " " + Integer.toString(base) + "\n";
				}

				//link snowflakes
				if (i > 0) {
					s = s + Integer.toString((i-1)*10) + " " + Integer.toString(i*10) + "\n";
					s = s + Integer.toString(i*10) + " " + Integer.toString((i-1)*10) + "\n";
				}
				else {
					s = s + Integer.toString(0) + " " + Integer.toString((CLUSTER_COUNT-1)*10) + "\n";
					s = s + Integer.toString((CLUSTER_COUNT-1)*10) + " " + Integer.toString(0) + "\n";					
				}
			}		
			writer.write(s.getBytes());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
			  writer.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

}
