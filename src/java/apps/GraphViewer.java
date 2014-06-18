package apps;

import java.util.List;

import kodkod.util.ints.IntSet;
import elegans.GraphImport;
import elegans.GraphSet;
import entanglement.EntanglementDetector;
import entanglement.trace.Traces;

public class GraphViewer {
	public static void main(String args[]) {
		GraphImport im = new GraphImport(
				"/home/sbarman/Projects/elegans/egfr/solutions/2013-04-21-14-22-29");
		GraphSet set = new GraphSet(im.traces);
		
		Traces kodkodTraces = Traces.traces(set.maxVals, set.simpleTraces);
		List<IntSet> entangledSets =
                EntanglementDetector.entanglement(kodkodTraces);
		System.out.println(entangledSets);
	}
}
