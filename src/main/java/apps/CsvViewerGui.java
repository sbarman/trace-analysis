package apps;

import java.awt.Dimension;
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
		CsvImport im = new CsvImport(args[0]);	
		openGui(new CsvSet(im.traces));
	}
	
	public static EntanglementGui<Integer, String, CsvTrace> openGui(CsvSet set) {
		List<CsvSet> sets = new ArrayList<CsvSet>();
		sets.add(set);
		return openGui(sets);
	}
	
	public static EntanglementGui<Integer, String, CsvTrace> openGui(List<CsvSet> sets) {
		CsvDisplay display = new CsvDisplay();

		EntanglementGui<Integer, String, CsvTrace> gui = 
				new EntanglementGui<Integer, String, CsvTrace>(
				sets, display);
		
		gui.setMaximumSize(new Dimension(200, 400));
		gui.pack();
		gui.setVisible(true);
		return gui;
	}
}
