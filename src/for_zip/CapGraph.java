/**
 * 
 */
package graph;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.opencsv.CSVReader;


/**
 * @author Lu
 * I implement graph as a hash of vertex (from node) and a list of vertices (to nodes).  Not using matrix so as to save memory.
 * 
 * For the warm up assignment, you must implement your Graph in a class
 * named CapGraph.  Here is the stub file.
 * 
 * GRAPH should not have duplicate vertice, i.e. differnt nodes with the same key.
 *
 */
public class CapGraph implements Graph {

	// In Java, interface can be used to declare variables.  Map is an interface.
	// initially I made it Map<Integer, ArrayList<Integer>>.  But, I really don't need it to be a list.  Set will do.
	private HashMap<Integer, HashSet<Integer>> graphAdjVertex;
	
	/** 
	 * Create a new empty CapGraph 
	 */
	public CapGraph()
	{
		// HashMap is a class - using hash table to implement Map interface
		graphAdjVertex = new HashMap<Integer, HashSet<Integer>>();
	}	
	
	/* (non-Javadoc)
	 * @see graph.Graph#addVertex(int)
	 */
	@Override
	public void addVertex(int num) {
		if (!graphAdjVertex.containsKey(num))
			graphAdjVertex.put(num, new HashSet<Integer>());
	}
	
	public int getNumVertices() {
		return graphAdjVertex.size();
	}
	
	public boolean containsKey(int num) {
		return (graphAdjVertex.keySet()).contains(num);
	}

	public Set<Integer> getVertices() {
		return graphAdjVertex.keySet();
	}
	
	public void saleVertices(String filename) {
		
		FileOutputStream writer = null;
		
		try {
			writer = new FileOutputStream(filename);
			
			String s = "";
			
			for (Integer i : this.getVertices()) {
				s = s + i.toString() + ",";
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
	
	/* (non-Javadoc)
	 * @see graph.Graph#addEdge(int, int)
	 */
	@Override
	public void addEdge(int from, int to) {
		if (graphAdjVertex.containsKey(from)) {
			// since l is a pointer to the memory location of the ArrayList of key, add new vertex to it is all needed.
			HashSet<Integer> s = graphAdjVertex.get(from);
			if (!s.contains(to))
				s.add(to);
		}
		else {
			// Initially I did graphAdjVertex.put(from, new ArrayList<Integer)(to)), but the to element was not added???
			HashSet<Integer> l = new HashSet<Integer>();
			l.add(to);
			graphAdjVertex.put(from, l);
		}

		if (!graphAdjVertex.containsKey(to)) {
			this.addVertex(to);
		}
	}
	
	/*
	 * Need to work for both directed and undirected graph
	 */
	public boolean removeEdge(int from, int to) {
		return (graphAdjVertex.get(from).remove(to) || graphAdjVertex.get(to).remove(from));
	}
	
	public Set<Integer> getNeighbours(int v) {
		return graphAdjVertex.get(v);
	}
	
	public int getNumEdges() {
		int total = 0;
		
		for (int k : this.getVertices()) {
			total += this.getNeighbours(k).size();
		}
		
		return total;
	}
	
	/*
	 * Transpose a graph is just reverse the direction of the from and to vertex, i.e.
	 * originally from A to B, now is from B to A.
	 */
	public CapGraph transposeGraph() {
		CapGraph tg = new CapGraph();
		
		for (Integer k : this.getVertices()) {
			tg.addVertex(k);
			for (Integer v : this.getNeighbours(k)) {
				tg.addEdge(v, k);
			}
		}
		return tg;
	}
	
	public String toString() {
		String s = "";
		
		// Using collection iterator
		for (Integer k : graphAdjVertex.keySet()) {
			s = s + k.toString() + ":";
			for (Integer item : graphAdjVertex.get(k)) {
				s = s + item.toString() + ',';
			}
			s += "\n";
		}
		return s;
	}

	/* (non-Javadoc)
	 * @see graph.Graph#getEgonet(int)
	 * 
	 * ego net is a sub graph with the center and all the vertices the center link to and all edges between all vertices
	 */
	@Override
	public Graph getEgonet(int center) {
		CapGraph cg = new CapGraph();
		
		// center is not a vertex on the graph, return empty
		if (!graphAdjVertex.containsKey(center))
			return cg;
		
		// center is good, so add center and all its to vertices
		cg.addVertex(center);
		for (Integer v : graphAdjVertex.get(center)) {
			cg.addVertex(v);
		}
		
		for (Integer k : cg.getVertices()) {
			for (Integer v : graphAdjVertex.get(k)) {
				if (cg.containsKey(v)) {
					cg.addEdge(k, v);
				}
			}
		}
		return cg;
	}

	/* (non-Javadoc)
	 * @see graph.Graph#getSCCs()
	 * 
	 * SCC = Strongly Connected Component of a directed graph
	 * A pair of vertices (u, v) are strongly connected if there is a path from u to v and a path from v to u.
	 * An undirected graph is always a strongly connected graph.
	 * 
	 * Algorithm:
	 * 1. DFS(depth first search) graph and keep track the order in which vertices finish
	 * 2. transpose graph
	 * 3. DFS(transposed graph) in the reverse order of the way vertices are visited.  (See note in code for details)
	 */
	@Override
	public List<Graph> getSCCs() {
		// algorithm step 1: DFS and keep track of order in which vertices finish
		/*
		 * DFS(G, vertices):
		 * 	init set visited and stack finished
		 * 	while (vertices not empty):
		 * 		v = vertices.pop()  // THIS REASON IS HAS TO BE POP IS STEP 3
		 * 		if (v not visisted):
		 * 			DFS-VISIT(G, v, visited, finished)
		 * 	return finished
		 * 
		 * DFS-VISIT(G, v, visited, finished):
		 * 	add v to the visited
		 * 	for (n in getNeighbours(v)):
		 * 		if (n not in visited):
		 * 			DFS-VISIT(G, n, visited, finished)
		 * 	push v on finished
		 */
		
		/*
		 *  First time call DFS the vertices order doesn't matter.  
		 *  But, a stack is created to make 2nd time work.
		 *  Also, scc_list is useful for 2nd DFS call.
		 */
		ArrayDeque<Integer> vertices = new ArrayDeque<Integer>();
		for (Integer v: this.getVertices()) {
			vertices.add(v);
		}
		ArrayList<Integer> scc_l1 = new ArrayList<Integer>();
		ArrayDeque<Integer> finished = DFS(this, vertices, scc_l1);
		
		/*
		Integer val;
		System.out.println("first finished list");
		while ((val = finished.pollLast()) != null) {
			System.out.println(val);
		}
		*/
		
		// step 2: transpose graph
		CapGraph GT = this.transposeGraph();
		
		/*
		 *  step 3: call DFS in reverse
		 *  Within DFS routine, each time DFS_VISIT_VERTEX reaches the end of its recursion, we have just completed
		 *  a strongly connected component and about the start a new one.  I create a list to capture this fact so
		 *  that I can use this info and the new finished sequence to reconstruct the graph components.  This is
		 *  the variable scc_list.
		 */
		ArrayList<Integer> scc_l2 = new ArrayList<Integer>();
		ArrayDeque<Integer> finished_2 = DFS(GT, finished, scc_l2);
		
		/*
		Integer val;
		System.out.println("2nd finished list");
		while ((val = finished_2.pollLast()) != null) {
			System.out.println(val);
		}
		
	    for (Integer i : scc_l2) {
	    	System.out.println("SCC elm: " + i);
	    }
	    */
		
		// build SCCs list of graph from finished list and SCC list.  See funciton for details.
		List<Graph> SCCs = buildSCCs(finished_2, scc_l2);
		
		return SCCs;
	}
	
	/*
	 * This is the implementation of Depth First Search - the outer shall: loop through each vertex to go deep in search.  Of course
	 * , if the vertex has been already visited during some other vertex's depth dive, I will skip it, and this is how the inner
	 * connection of the graph is discovered.
	 * 
	 * Notes on parameter "vertices":
	 *  Set is an interface (with super interfaces COLLECTION, ITERABLE) with no dups. But, one can't access by index.
	 *  List is a COLLECTION and ITERABLE where dups are allowed and access by index. 
	 *  Queue and Deque is a COLLECTION, ITERABLE with FIFO and FILO capabilities.
	 *  What I need is a stack, which is an interface of DEQUE (COLLECTION, ITERABLE, QUEUE).
	 */
	private ArrayDeque<Integer> DFS(CapGraph G, Deque<Integer> vertices, List<Integer> scc_list)	{
		HashSet<Integer> visited = new HashSet<Integer>();  //Hash implementation of set is efficient
		ArrayDeque<Integer> finished = new ArrayDeque<Integer>();  //ArrayDeque implements stack	

		Integer v = vertices.pollLast();
		while (v != null) {
			if (!visited.contains(v)) {
				//System.out.println("SCC start:" + v);
				scc_list.add(v);  // 2nd time (i.e. step 3) DFS is called, this is the moment we know v is the start of SCC.
				DFS_VISIT_VERTEX(G, v, visited, finished); //since the visit is depth first, visited and finished will be populated by depth
			}
			v = vertices.pollLast();
		}
		return finished;
	}
	
	/*
	 * DFS_VISIT_VERTEX is the working horse - the recursive routine that does depth first.
	 */
	private void DFS_VISIT_VERTEX(CapGraph G, Integer v, HashSet<Integer> visited, ArrayDeque<Integer> finished) {

		visited.add(v);
		for (Integer n : G.getNeighbours(v)) {
			
			if (!visited.contains(n)) {
				DFS_VISIT_VERTEX(G, n, visited, finished);  //depth first search
			}
		}
		finished.addLast(v); //for stack, add last, pop last.  If it were queue, add last, pop first.
	}
	
	
	/*
	 * buildSCCs:
	 * Use scc_list, finished_2 and the graph (graphAdjVertex) to build a list of SCC components in the graph
	 * 
	 * For example:
	 * finished_2 stack (from head to tail): 32, 65, 18, 23, 25, 44, 50
	 * SCC list: 32, 25, 44, 50.  SCC list indicates the completion of the Strongly Connected Component.
	 */
	private List<Graph> buildSCCs(ArrayDeque<Integer> finished, ArrayList<Integer> SCC_list) {
		
		ArrayList<Graph> SCCs = new ArrayList<Graph>();

		
		// Take the first element from finished stack, as long as it doesn't equal to the SCC item, add it to the graph.
		// When it equals, add this as last item to the graph.		
		for (int i = 0; i < SCC_list.size(); i++) {
			CapGraph g = new CapGraph();

			Integer SCC_end_element = SCC_list.get(i);
			Integer val;
			while (((val = finished.pollFirst()) != SCC_end_element) && (val != null)) {
				g.addVertex(val);
			}
			if (val == null) {
				//throw new IndexOutOfBoundsException();  // I can't throw exception because the Graph interface doesn't include exception
				System.out.println("buildSCCs index out of bounds");
			}
			else 
				g.addVertex(SCC_end_element);
			
			// fill in edges
			for (Integer v : g.getVertices()) {
				for (Integer e: this.getNeighbours(v)) {
					if (g.containsKey(e)) // e is one of the vertex in graph g
						g.addEdge(v, e);
				}
			}
			
			//add graph g to SCCs List<Graph>
			SCCs.add(g);
		}
		
		return SCCs;
	}

	/*
	 * Detect Community in undirected graph
	 * 
	 * The definition of a community is one where there are more connections among members within a community then between
	 * members of two different communities.
	 * 
	 * I am going to use the outside in method, that is by keeping a count of times an edge is utilized by the shortest distance
	 * from one vertex to another.  If the edge most utilized is removed, two communities will form.  If this logic is applied
	 * multiple times, then smaller, and smaller sub communities would form.  It is a question of when to stop it.
	 * 
	 * Steps to determine communities are:
	 * 1. take all combination of start and end vertices to find the shortest path (knowing 1 to 7 and 7 to 1 are the same)
	 * 2. put each shortest path on the edge utilization chart
	 * 3. when step 1 and 2 are done, process the utilization chart to produce a most often used edges list
	 * 4. remove most often used edges from the graph
	 * 5. run SCC (strongly connected component) on the fragmented graph to get communities
	 * 6. export the communities as a set of vertices to get characteristics
	 * 
	 */
	public List<Graph> getCommunities() {
		
		// WHAT IS GOING TO HAPPEN WHEN THERE IS JUST A SINGLE VERTEX IN THE GRAPH?
		
		EdgeUtilization edge_utilization = new EdgeUtilization();
		
		// edge_utilization is used to keep track of which edge is used how many times in shortest path between two vertices
		// one good thing is that the vertices are numbered from 1 sequentially up
		// this loops (n-1)! times, n being the number of vertices. O(n!)
		for (int i = 1; i < this.getNumVertices(); i++) {
			System.out.println(i);
			for (int j = i + 1; j <= this.getNumVertices(); j++) {
				
				// find the shortest path using breath first search
				//System.out.println(i + ":" + j);
				BFS(i, j, edge_utilization);
			}
		}

		edge_utilization.saveEdgeUtilizationChart("data/edge_utilization.txt");
		
		//System.out.println("edge utilization chart:");
		//edge_utilization.printEdgeUtilizationChart();
		
		edge_utilization.computeMostUsedEdges();
		
		//System.out.println("most often used edges:");
		//edge_utilization.printMostOftenUsedEdges();
		
		edge_utilization.saveMostUsedEdges("data/most_often_used_edges.txt");
		
		return null;
	}
	
	/*
	 * BFS - breath first search from a starting vertex to an ending vertex for the shortest path(s).
	 * Once such a path is found, each path has its edge utilization count incremented by 1.  The objective of doing so
	 * is that when all combinations of start and end points are investigated by BFS, the most utilized edges are identified as this
	 * is part of the community identification process.
	 */
	public void BFS (Integer start, Integer end, EdgeUtilization eu) {
		if (start.equals(end))
			return;
		
		if ( !graphAdjVertex.containsKey(start) || (!graphAdjVertex.containsKey(end))  )
			return;
		
		ArrayDeque<Integer> q = new ArrayDeque<Integer>();  // the processing queue that drives it all
		HashSet<Integer> visited = new HashSet<Integer>();  // visited pretty much just mean i have added it to parent structure
		HashMap<Integer, HashSet<Integer>> parents = new HashMap<Integer, HashSet<Integer>>();
		
		Set<ArrayList<Integer>> shortest_paths = null;
		
		// start the breadth first search
		q.add(start);
		// In the MapGraph code, visited.add(start) is here.  But, MapGraph finds a single shortest path, even though multiple path exists.
		// (I need to investigate to be sure.)  So, this logic here is different.
		while (!q.isEmpty()) {
			Integer curr = q.remove(); //remove from the head of the deque and throw exception if queue is empty
			
			if (curr.equals(end)) {
				/*
				 * Now, the breath first search for shortest path is done.
				 * The result is in the parents structure.
				 * Next:
				 * 1. take this structure to construct the path;
				 * 2. mark the path in the edge_utilization chart
				 * 3. when that is done, create the most_utilized chart which is what used to decide communities.
				 */
				//System.out.println(parents);
				
				shortest_paths = processPaths(start, end, parents);
				
				for (ArrayList<Integer> path : shortest_paths) {
					//System.out.println("start -> end :" + start + " " + end);
					//printPath(path);
					
					if (!eu.processPath(path)) {
						// best to throw exception.  need to think!
						System.out.println("edge_utlization processPath failed");
					}
				}
				
				return;
			}
			
			if (!visited.contains(curr)) {
				visited.add(curr);
			
				// handling all curr's neighbors (i.e. children, which is an incorrect word)
				for (Integer n : this.getNeighbours(curr)) {
					if (!visited.contains(n)) {
						q.add(n);
					
						//add curr as n's parent in parental mapping
						if (parents.containsKey(curr)) {
							(parents.get(curr)).add(n);
						}
						else {
							HashSet<Integer> s = new HashSet<Integer>();
							s.add(n);
							parents.put(curr, s);
						}
					}
				}
			}	
		}
	}
	
	
	/*
	 * Now, the breath first search for shortest path is done.
	 * The result is in the parents structure.
	 * Next: take this structure to construct a list of paths, notice that there could be multiple paths that are the shortest.
	 * 
	 * BUILD PATH HAS TO BE RECURSIVE IN THE BREATH FIRST WAY
	 */
	private Set<ArrayList<Integer>> processPaths(Integer s, Integer g, HashMap<Integer, HashSet<Integer>> p) {
		
		Set<ArrayList<Integer>> paths = new HashSet<ArrayList<Integer>>();
		ArrayList<Integer> l = new ArrayList<Integer>();
		l.add(g);
		paths.add(l);
		
		// I know for sure s is not g at the very beginning
		Set<ArrayList<Integer>> built_paths = buildPaths(s, p, paths);
		
		return built_paths;
		
	}
	
	/*
	 * Build Paths recursively taking set of paths built so far, the start vertex, the parent structure to build the next level/layer/step backward.  
	 * Even though the constructed paths are in reverse order (from end vertex to start), this matters not
	 * for the edge utilization chart.
	 * 
	 * Algorithm:
	 * take a single unfinished path
	 * 		take the last vertex, get its parent set of vertices
	 * 		if no parent vertex found, the path is a dead one that need to be abandoned
	 * 		if one of the vertex is the start node, the rest of the vertex ought to be threw out because i have a complete path and, i should remember i got a path, but finish off the rest of the paths
	 */
	private Set<ArrayList<Integer>> buildPaths(Integer s, HashMap<Integer, HashSet<Integer>> parents, Set<ArrayList<Integer>> paths) {
		boolean done = false;  //indicator if i have a finished path, because this is the level that recursion is done
		Set<Integer> all_next_level = new HashSet<Integer>();  // i need all keys for this level so that when i am done with this level, i remove them from parents in order for path building to be right
		Set<ArrayList<Integer>> final_new_paths = new HashSet<ArrayList<Integer>>();  //the remaining paths either done or for next level
		
		for (ArrayList<Integer> path : paths) {
			
			//printPath(path);
			
			// retrieve the last vertex on the path to find its parents
			int last = path.size() - 1;

//System.out.println("child is " + path.get(last));

			Set<Integer> next_level = getKey(parents, path.get(last));
			
//System.out.println(next_level );	

			if (next_level.size() > 0) {
				
				if (next_level.contains(s)) {
					done = true;
					path.add(s);
					final_new_paths.add(path);
				}
				else { // this is not the final layer, at least not for this branch (i.e. path)
					all_next_level.addAll(next_level);
					
					// make needed copies to branch out path
					ArrayList<ArrayList<Integer>> new_paths = new ArrayList<ArrayList<Integer>>();
					new_paths.add(path);
					for (int i = 0; i < next_level.size()-1; i++) {
						new_paths.add(deepCopy(path));  // make needed number of copies of current path
					}

//System.out.println("here1");

					// put next level vertices on the branched paths
					int counter = 0;
					for (Integer e : next_level) {
						new_paths.get(counter).add(e);
						counter ++;
					}
					
					final_new_paths.addAll(new_paths);	
					
//System.out.println("here2");
					
				}
				
			}

		}
		
//System.out.println("here3");
		
		if (done) {
//System.out.println("here4");
			
			Set<ArrayList<Integer>> final_paths = new HashSet<ArrayList<Integer>>();
			
			// there is a possibility that final_new_paths contain paths whose last node is not start due to the fact I have to loop all branches through
			// i need to clean up
			for (ArrayList<Integer> l : final_new_paths) {
				if (  l.get(l.size()-1) == s ) {
					final_paths.add(l);
				}
			}
			return final_paths;
		}
		else {
			// remove all consumed keys from parents
			for (Integer k : all_next_level) {
				parents.remove(k);
			}
//System.out.println("here5");
			
			return buildPaths(s, parents, final_new_paths);
		}
	}
	
	// based on the advice not to use object clone, write my own deep copy.  Since the objects are integer, I am wondering
	// if this is needed.  Need to play around to verify.
	private ArrayList<Integer> deepCopy(ArrayList<Integer> l) {
		ArrayList<Integer> nl = new ArrayList<Integer>();
		for (int i = 0; i < l.size(); i++) {
			nl.add(l.get(i));
		}
		return nl;
	}
	
	// given a vertex, find all of its parent vertices
	private Set<Integer> getKey(HashMap<Integer, HashSet<Integer>> p, Integer g) {
		HashSet<Integer> result = new HashSet<Integer>();
		
		for (Integer pt: p.keySet()) {
			if ((p.get(pt)).contains(g)) {
				result.add(pt);
			}
		}
		return result;
	}	
	
	// MAYBE I SHOULD MAKE PATH AN OBJECT CLASS BY ITSELF.  THINK OF IT...
	public void printPath(ArrayList<Integer> p) {
		String s = "path: ";
		for (int i = 0; i < p.size(); i++) {
			s = s + p.get(i).toString() + ",";
		}
		System.out.println(s);
	}
	
	/*
	 * createCommunities:
	 * 1. Takes integer N, read most often used edges file to get the edges that are used above N times.
	 * 2. These edges are removed from the graph to create communities.
	 */
	public void createCommunities(String filename, int N) {
		
		String line;
		
		try (
		    InputStream fis = new FileInputStream(filename);		
		    InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
		    BufferedReader br = new BufferedReader(isr);
		) 
		{
			int count = 0;
		    while ((line = br.readLine()) != null) {
		    	/* have to go to the edge utilization directly to cut more
		        // the file is line "count number" followed by multiple edge lines
		    	if (line.startsWith("count")) {
		    		String[] f = line.split(" ");
		    		count = Integer.parseInt(f[1]);
		    	}
		    	else if (count > N) {
		    		String[] f = line.split(",");
		    		int from = Integer.parseInt(f[0]);
		    		int to = Integer.parseInt(f[1]);
		    		
		    		this.removeEdge(from, to);
		    	}
		    	*/
		    	String[] f = line.split(":");
		    	count = Integer.parseInt(f[1]);
		    	if (count > N) {
		    		String[] p = f[0].split(",");
		    		int from = Integer.parseInt(p[0]);
		    		int to = Integer.parseInt(p[1]);
		    		
		    		this.removeEdge(from, to);
		    	}
		    }
		} 
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	/* (non-Javadoc)
	 * @see graph.Graph#exportGraph()
	 */
    /* Return the graph's connections in a readable format. 
     * The keys in this HashMap are the vertices in the graph.
     * The values are the nodes that are reachable via a directed
     * edge from the corresponding key. 
	 * The returned representation ignores edge weights and 
	 * multi-edges.  */	
	@Override
	public HashMap<Integer, HashSet<Integer>> exportGraph() {
		return graphAdjVertex;
	}

}
