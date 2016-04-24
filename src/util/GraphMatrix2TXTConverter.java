/**
 * 
 */
package util;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVReader;


/**
 * @author LU
 * 
 * The sparse matrix of Facebook Data Scrape (2005) is stored in CSV format.
 * GraphMatrix2TXTConverter takes the CSV format, convert it to a list of from node and to node.
 * Example (from node is 1, to node is 23):
 * 1 23
 * 
 * Converted data is stored under the same file name with extension ".txt"
 *
 */
public class GraphMatrix2TXTConverter {
	
	// This is like a C routine.  "static" makes the method at class level.
	public static void convertMatrix2TXT(String filename) {
		
		CSVReader reader = null;
		FileOutputStream writer = null;
		String s = "";
		
		try {
			writer = new FileOutputStream("C:\\Users\\LU\\JAVA_workspace\\SocialNetworks\\data\\Cal65.txt");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			reader = new CSVReader(new FileReader(filename));
			String[] myEntries = null;
			
			int i = 0;
			while ((myEntries = reader.readNext()) != null) {

				i++;
				for (int j = 0; j < myEntries.length ; j++) {
					if (myEntries[j].matches("1")) {
						//System.out.println(myEntries.get(i)[j]);
						
						Integer from = i;
						Integer to = j + 1;
						s = from.toString() + " " + to.toString() + "\n";
						
						writer.write(s.getBytes());
						s = "";
					}	
				}
			
			}

			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
			  reader.close();

			  writer.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
