package elegans;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class GraphImport {

	public final Set<GraphTrace> traces;

	public GraphImport(String solutionDir) {
		File f = new File(solutionDir);
		File[] files = f.listFiles();
		List<File> solutions = new ArrayList<File>();

		for (int i = 0, ii = files.length; i < ii; ++i) {
			File child = files[i];
			if (child.getName().endsWith("sol")) {
				solutions.add(child);
			}
		}

		traces = new HashSet<GraphTrace>();
		for (int i = 0, ii = solutions.size(); i < ii; ++i) {
			traces.add(getSolution(solutions.get(i)));
		}

	}

	protected GraphTrace getSolution(File file) {
		Map<String, String> graph = new HashMap<String, String>();
		String intFile = "";
		String graphFile = "";

		try {
			Scanner scanner = new Scanner(file);
			intFile = scanner.nextLine();
			graphFile = file.getParent() + "/" + scanner.nextLine() + ".png";

			while (scanner.hasNextLine()) {
				String edge = scanner.nextLine();
				String[] keyValue = edge.split(":");
				graph.put(keyValue[0], keyValue[1]);
			}
		} catch (IOException e) {
			// do nothing
		}

		return new GraphTrace(graph, graphFile, intFile);
	}
}
