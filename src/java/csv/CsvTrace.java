package csv;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvTrace {

	final String id;
	final List<String> values;
	final Map<Integer, String> mapping;
	
	public CsvTrace(String id, List<String> values) {
		this.id = id;
		this.values = values;
		
		HashMap<Integer, String> mapping = new HashMap<Integer,String>();
		for (int i = 0, ii = values.size(); i < ii; ++i) {
			mapping.put(i, values.get(i));
		}
		this.mapping = mapping;
		
	}
}
