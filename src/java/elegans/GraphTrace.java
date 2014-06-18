package elegans;

import java.util.Map;

public class GraphTrace {

	final Map<String, String> mapping;
	final String graphFn;
	final String activationFn;
	
	public GraphTrace(Map<String, String> mapping, String graphFn, String activationFn) {
		this.mapping = mapping;
		this.graphFn = graphFn;
		this.activationFn = activationFn;
	}
}
