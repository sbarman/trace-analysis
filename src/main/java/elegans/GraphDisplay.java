package elegans;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.swing.JEditorPane;

import entanglement.TraceDisplay;
import entanglement.TraceSet;

public class GraphDisplay implements TraceDisplay<String, String, GraphTrace> {

	@Override
	public void setUpperPane(JEditorPane pane,
			TraceSet<String, String, GraphTrace> traces, GraphTrace selected) {
		Map<String, String> mapping = selected.mapping;
		String out = "";
		for (String id : mapping.keySet()) {
			out += id + " -> " + mapping.get(id) + "<br>";
		}
		pane.setText(out);
	}

	@Override
	public void setLowerPane(JEditorPane pane,
			TraceSet<String, String, GraphTrace> traces, GraphTrace selected) {
		File f = new File(selected.graphFn);
		try {
			URL url = f.toURI().toURL();
			
			String html = "<html><img src='"+ url +"' ></img>"; 
			pane.setText(html);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
