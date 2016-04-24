/**
 * 
 */
package analysis;

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
	public static void main(String[] args) {

        CapGraph graph = new CapGraph();
        //GraphLoader.loadGraph(graph, "data/Cal65.txt");
        //GraphLoader.loadGraph(graph, "data/Amherst41.txt");
        //GraphLoader.loadGraph(graph, "data/test1.txt");
        GraphLoader.loadGraph(graph, "data/SnowflakeNetwork.txt");
        
        //System.out.println(graph);
        
		List<Graph> scc;
		//scc = graph.getSCCs();	
		//System.out.println("Initial SCC with graph counts: " + scc.size());
		
		/*
		for (Graph g : scc) {
			System.out.println("SCC:");
			System.out.println((CapGraph)g);
		}
		*/
		
        //EdgeUtilization eu = new EdgeUtilization();      
        //graph.BFS(1, 4, eu);
        
        graph.getCommunities();
		
		// remove most used edges to create communities
        graph.createCommunities("data/edge_utilization.txt", 150);

        
        
		scc = graph.getSCCs();
		
		System.out.println("After remove most used edges SCC with graph counts: " + scc.size());
		
		//Integer i = 0;
		//String filename_base = "data/snowflake_community_";
		 
		for (Graph g : scc) {
			//i++;
			//String filename = filename_base + i.toString();		
			//((CapGraph)g).saleVertices(filename);
			CapGraph cg = (CapGraph)g;
			System.out.println(cg);
		}
		

        

	}

}
