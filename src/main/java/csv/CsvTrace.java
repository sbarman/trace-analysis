package csv;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvTrace {

	public final String id;
	public final List<String> values;
	public final Map<Integer, String> mapping;
	public final String stringConcat;
	
	public CsvTrace(String id, List<String> values) {
		this.id = id;
		this.values = values;
		
		HashMap<Integer, String> mapping = new HashMap<Integer,String>();
		String stringConcat = "";
		for (int i = 0, ii = values.size(); i < ii; ++i) {
			String value = values.get(i);
			mapping.put(i, value);
			stringConcat += value;
		}
		this.mapping = mapping;
		this.stringConcat = stringConcat;
	}
}
