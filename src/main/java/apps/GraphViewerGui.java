package apps;

import java.util.ArrayList;
import java.util.List;

import elegans.GraphDisplay;
import elegans.GraphImport;
import elegans.GraphSet;
import elegans.GraphTrace;
import sketch.entanglement.ui.EntanglementGui;

public class GraphViewerGui {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GraphImport im = new GraphImport(
				"/home/sbarman/Projects/elegans/egfr/solutions/2013-04-21-14-22-29");	
		List<GraphSet> sets = new ArrayList<GraphSet>();
		sets.add(new GraphSet(im.traces));
		
		GraphDisplay display = new GraphDisplay();

		EntanglementGui<String, String, GraphTrace> gui = new EntanglementGui<String, String, GraphTrace>(
				sets, display);
		
		gui.pack();
		gui.setVisible(true);

	}
}
