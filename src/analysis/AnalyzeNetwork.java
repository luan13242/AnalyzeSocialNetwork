/**
 * 
 */
package analysis;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import graph.CapGraph;
import graph.EdgeUtilization;
import graph.Graph;
import util.GraphLoader;

/**
 * @author LU
 *
 */
public class AnalyzeNetwork {

	/**
	 * @param args
	 */
	static int MAX_ITERATION = 800;
	static int MIN_GRAPH_SIZE = 10;
	
	public static void main(String[] args) {

        CapGraph graph = new CapGraph();

        //GraphLoader.loadGraph(graph, "data/caltech36.txt");
        //GraphLoader.loadGraph(graph, "data/Amherst41.txt");
        //GraphLoader.loadGraph(graph, "data/test1.txt");
        GraphLoader.loadGraph(graph, "data/SnowflakeNetwork.txt");
        //GraphLoader.loadGraph(graph, "data/test_data.txt");
        
        System.out.println(graph);
        	
        /*
         * I interrupted the execution, so I need to remove some edges to carry on
top edge is 80,223
top edge is 99,585
top edge is 411,744
top edge is 394,522
top edge is 522,743
top edge is 57,184
top edge is 75,171
top edge is 128,221
top edge is 50,583
top edge is 223,387
top edge is 80,440
top edge is 80,689
top edge is 80,432
top edge is 80,327
         

        graph.removeEdge(80,223);
        graph.removeEdge(99,585);
        graph.removeEdge(411,744);
        graph.removeEdge(394,522);
        graph.removeEdge(522,743);
        graph.removeEdge(57,184);
        graph.removeEdge(75,171);
        graph.removeEdge(128,221);
        graph.removeEdge(50,583);
        graph.removeEdge(223,387);
        graph.removeEdge(80,440);
        graph.removeEdge(80,689);
        graph.removeEdge(80,432);
        graph.removeEdge(80,327);
        */
        
        List<Graph> scc;
		scc = graph.getSCCs();
		
		System.out.println("Initial SCC with graph counts: " + scc.size());
		
		
		ArrayList<Graph> final_result = new ArrayList<Graph>();
		for (Graph g : scc) {
			CapGraph cg = (CapGraph)g;
			
			System.out.println("SCC vertex count:" + cg.getNumVertices());
			
			if (cg.getNumVertices() <= MIN_GRAPH_SIZE)
				final_result.add(cg);
			else
				final_result.addAll(getCommunities(cg, 0));
		}
		
		/*
		//700+ iterations were done and top edge captured in a file, removeEdgesFromFile removes them.
		List<Graph> final_result;
        removeEdgesFromFile(graph, "data/caltech36_output.txt");
        final_result = graph.getSCCs();
        */
		
		System.out.println("final result: " + final_result.size());
		int i = 0;
		//String base = "data/caltech36";
		String base = "data/snowflake";
		//String base = "data/test_data";
		for (Graph g : final_result) {
			CapGraph cg = (CapGraph)g;
			System.out.println(cg.getNumVertices());
			i++;
			cg.saveVertices(base + "_out_" + i + ".txt");
		}
	
		
	}
	
	
	private static List<Graph> getCommunities(CapGraph cg, int iteration) {
		
		ArrayList<Graph> result = new ArrayList<Graph>();
		
		if (iteration > MAX_ITERATION) {
			System.out.println("Iteration max reached");
			result.add(cg);
			return result;
		}

		EdgeUtilization eu = new EdgeUtilization();
		
		cg.detectCommunities(eu, null, null);
		if (!cg.removeTopUsedEdges(eu))  {// no edge removed means it is done
			System.out.println("No more edge to remove");
			result.add(cg);
			return result;
		}
				
		//System.out.println(cg);
		
		List<Graph> scc;
		scc = cg.getSCCs();
		
		System.out.println(iteration + ":" + "After remove most used edges, graph has " + scc.size() + " SCC");
		
		for (Graph g : scc) {
			CapGraph ncg = (CapGraph)g;
			System.out.println("SCC vertex count: " + ncg.getNumVertices());
			
			if (ncg.getNumVertices() <= MIN_GRAPH_SIZE) 
				result.add(ncg);
			else {
				iteration++;
				result.addAll(getCommunities(ncg, iteration));
			}
		}
		
		return result;
	}
	

	private static void removeEdgesFromFile(CapGraph g, String filename) {
		try (
			    InputStream fis = new FileInputStream(filename);		
			    InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
			    BufferedReader br = new BufferedReader(isr);
			) 
			{
			    String line;
			    
			    while ((line = br.readLine()) != null) {
			    	if (line.charAt(0) == 't') {
			    		String[] parts = line.split(" ");
			    		String[] nodes = parts[3].split(",");
			    		g.removeEdge(Integer.parseInt(nodes[0]), Integer.parseInt(nodes[1]));
			    	}
			    }
			}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
			 
	}
}
