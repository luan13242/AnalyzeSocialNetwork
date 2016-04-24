/**
 * 
 */
package graph;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * @author LU
 * Date: Apr-8-2016
 * 
 * In determining communities (closely connected vertices) one needs to know which edges are
 * utilized more in determining the shortest path between vertices.  Then, cutting them would
 * create communities.  This class - EdgeUtilization is created for this purpose.
 * 
 * Edge is represented as a string in the format of from_vertex,to_vertex but the from is always smaller.
 * For example, from 8 to 1 will be represented as 1,8.  This way, when a path is supplied to this class,
 * each vertex forms a "from vertex, to vertex".  The utilization count is incremented each time it is used.
 * 
 */
public class EdgeUtilization {
	
	// edge_utilization_chart is to track for the entire graph how many times each edge is used in shortest path
	private HashMap<String, Integer> edge_utilization_chart;
	
	// most_often_used_edges is to track the top set of edges that are used most often
	private HashMap<Integer, HashSet<String>> most_often_used_edges;
	
	// most_used_edges_computed is a flag to indicate if most_often_used_edges need to be (re)computed
	private boolean most_used_edges_computed;
	
	// a maximum most used count is predefined so that I don't store too much unwanted info.
	private static int MAX_MOST_USED_COUNT = 1000;
	
	// if an edge is used more than this number of times, I have check to see if it is most often used.  this is to save time
	private static int CUT_OFF = 10;
	
	// current minimum: the min edge used count in the most used set
	private int curr_min;
	
	
	public EdgeUtilization() {
		edge_utilization_chart = new HashMap<String, Integer>();
		most_often_used_edges = new HashMap<Integer, HashSet<String>>();
		most_used_edges_computed = false;
		curr_min = 0;
	}
	
	// path is a sequential list of integers (from, to vertices)
	public boolean processPath(List<Integer> path) {
		
		// path must be two vertices or longer
		if (path.size() < 2)
			return false;
		
		for (int i = 0; i < path.size()-1; i++) {
			Integer from = path.get(i);
			Integer to = path.get(i+1);
			
			// make sure from is smaller
			if (from.compareTo(to) > 0) {
				Integer temp = from;
				from = to;
				to = temp;
			}
			
			String s = from.toString() + "," + to.toString();
			
			if (edge_utilization_chart.containsKey(s)) {
				Integer count = edge_utilization_chart.get(s);
				count += 1;
				edge_utilization_chart.put(s, count);
			}
			else {
				edge_utilization_chart.put(s, 1);
			}
		}
		
		most_used_edges_computed = false;
		
		return true;
	}
	
	/*
	 * My assumption is that edges used once, twice, maybe 5 times are not the most used edges.
	 * A most used count, e.g. 20, may have a few edges with this count value.  Therefore, the
	 * data structure is a hash map of count to edges (strings).
	 * Once all edges are looked at, I can sort the edge counts easily.
	 * 
	 * Also, a maximum most used count is predefined so that I don't store too much unwanted info.
	 */
	public void computeMostUsedEdges() {
		
		// flip through all edges to determine which ones are most often used
		for (String k : edge_utilization_chart.keySet()) {
			Integer cnt = edge_utilization_chart.get(k);
			
			if (cnt > CUT_OFF) {
				qualifyMostUsed(k, cnt);
			}
		}
		
		most_used_edges_computed = true;
	}
	
	/*
	 * Most used is a hash of used count and set of edges.
	 * The most used count is defined by the MAX_MOST_USED_COUNT.
	 * If currently my count values are less than the MAX_MOST_USED_COUNT, I will just add the count and edge, and update my curr_min.
	 * If my values is at MAX_MOST_USED_COUNT, I need to see if the count is greater than curr_min.  If so, need to update min.
	 * See code for details.
	 */
	private void qualifyMostUsed(String edge, Integer count) {
		
		// the count is in the most often used set, so one more edge for it
		if (most_often_used_edges.containsKey(count)) {
			most_often_used_edges.get(count).add(edge);
		}
		
		// if there is still room in the most often used set, add it and update the curr_min
		else if (most_often_used_edges.size() < MAX_MOST_USED_COUNT) {
			if (curr_min > count)
				curr_min = count;
			
			HashSet<String> s = new HashSet<String>();
			s.add(edge);
			most_often_used_edges.put(count, s);
		}
		
		// no more room, I will only add it if it is greater than curr_min
		else if (count > curr_min){
			most_often_used_edges.remove(curr_min);
			
			HashSet<String> s = new HashSet<String>();
			s.add(edge);
			most_often_used_edges.put(count, s);
			
			// need to find the new curr_min
			curr_min = count;
			for (Integer i : most_often_used_edges.keySet()) {
				if (i < curr_min) {
					curr_min = i;
				}
			}
		}
	}
	
	public void printEdgeUtilizationChart() {
		for (String k : this.edge_utilization_chart.keySet()) {
			System.out.println(k + ":" + this.edge_utilization_chart.get(k));
		}
	}
	
	// I thought about passing the data out as a string.  The fear is that the string might be too big
	public void saveEdgeUtilizationChart(String filename) {
		FileOutputStream writer = null;
		String s = "";
		
		try {
			writer = new FileOutputStream(filename);
	
			for (String k : this.edge_utilization_chart.keySet()) {
				s = s + k + ":" + this.edge_utilization_chart.get(k).toString() + "\n";
				writer.write(s.getBytes());
				s = "";
			}
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
		
	public void printMostOftenUsedEdges() {
		for (Integer k : this.most_often_used_edges.keySet()) {
			System.out.println("count: " + k);
			for (String s : this.most_often_used_edges.get(k)) {
				System.out.println(s);
			}
		}
	}	
	
	public void saveMostUsedEdges(String filename) {
		FileOutputStream writer = null;
		String s = "";
		
		try {
			writer = new FileOutputStream(filename);
			
			// get all the keys and sort
			Integer[] all_counts = this.most_often_used_edges.keySet().toArray(new Integer[this.most_often_used_edges.size()]);	
			Arrays.sort(all_counts);

			int length = all_counts.length;
			for (int i = 0; i < length; i++) {
				int index = length -1 -i;
				s = s + "count " + Integer.toString(all_counts[index]) + ":\n";
				
				for (String edge : this.most_often_used_edges.get(all_counts[index])) {
					s = s + edge + "\n";
				}
				
				writer.write(s.getBytes());
				s = "";
			}
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
