package csv;

import java.util.List;

import javax.swing.JEditorPane;

import entanglement.TraceDisplay;
import entanglement.TraceSet;

public class CsvDisplay implements TraceDisplay<String, String, CsvTrace> {

	@Override
	public void setUpperPane(JEditorPane pane,
			TraceSet<String, String, CsvTrace> traces, CsvTrace selected) {
		String out = "";
		List<String> values = selected.values;
		for (int i = 0, ii = values.size(); i < ii; ++i) {
			out += i + " -> " + values.get(i) + "<br>";
		}
		pane.setText(out);
	}

	@Override
	public void setLowerPane(JEditorPane pane,
			TraceSet<String, String, CsvTrace> traces, CsvTrace selected) {
		
	}

}
