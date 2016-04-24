package graph;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class TestCapGraph {

	@Test
	public void test() {
		CapGraph cg = new CapGraph();
		
		
		cg.addVertex(18);
		cg.addEdge(18, 23);
		cg.addEdge(18, 44);
		
		cg.addEdge(23, 18);
		cg.addEdge(23, 25);
		
		cg.addEdge(25, 18);
		cg.addEdge(25, 23);
		cg.addEdge(25, 65);
		
		cg.addVertex(32);
		cg.addEdge(32, 44);
		cg.addEdge(32, 50);
		
		cg.addEdge(44, 50);
		
		cg.addVertex(50);
		
		cg.addEdge(65, 23);
		
		System.out.println(cg.getNumVertices());
		System.out.println(cg);
		
		
		//CapGraph egonet = (CapGraph)cg.getEgonet(25);
		//System.out.println("egonet:");
		//System.out.println(egonet);
		
		/*
		cg.addEdge(1, 3);
		cg.addEdge(3, 2);
		cg.addEdge(2, 1);
		System.out.println(cg);
		*/

		/*
		CapGraph cgt = cg.transposeGraph();
		System.out.println("transpose is:");
		System.out.println(cgt);
		*/
		
		List<Graph> scc = cg.getSCCs();
		
		for (Graph g : scc) {
			System.out.println("SCC:");
			System.out.println((CapGraph)g);
		}
		
		
		
	}

}
