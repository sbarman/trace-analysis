package apps;

import java.util.ArrayList;
import java.util.List;

import csv.CsvDisplay;
import csv.CsvImport;
import csv.CsvSet;
import csv.CsvTrace;

import sketch.entanglement.ui.EntanglementGui;

public class CsvViewerGui {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CsvImport im = new CsvImport("SODA.csv");	
		List<CsvSet> sets = new ArrayList<CsvSet>();
		sets.add(new CsvSet(im.traces));
		
		CsvDisplay display = new CsvDisplay();

		EntanglementGui<String, String, CsvTrace> gui = new EntanglementGui<String, String, CsvTrace>(
				sets, display);
		
		gui.pack();
		gui.setVisible(true);

	}
}
