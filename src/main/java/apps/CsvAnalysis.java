package apps;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import sketch.entanglement.ui.PartitionSummaryPanel;
import csv.CsvImport;
import csv.CsvSet;
import csv.CsvTrace;
import entanglement.TraceSet;

public class CsvAnalysis {

	public final CsvSet original;
	public final PrintStream output;
	
	public CsvAnalysis(CsvSet traces, PrintStream output) {
		this.original = traces;
		this.output = output;
	}
	
	public void outputTracesInfo(CsvSet traces) {
		PrintStream output = this.output;
		
		// create a sorted version of the partitions based upon earliest index
		// in the partition
		Set<Set<Integer>> partitions = traces.getEntangledPartitions();
		List<List<Integer>> partitionsList = this.convertPartitionSet(partitions); 
				
		// output the partitions
		List<String> partitionStrings = new ArrayList<>(); 
		for (List<Integer> partition : partitionsList) {
			partitionStrings.add(StringUtils.join(partition, " "));
		}
		output.println("Partitions: " + StringUtils.join(partitionStrings, " | "));
		output.println("Num Partitions: " + partitions.size());
		output.println("Num Traces: " + traces.size());
		
		for (List<Integer> partition: partitionsList) {
			Set<Map<Integer, String>> values = traces.getValues(new HashSet<>(partition));
			List<String> valueStrings = new ArrayList<>();
			
			for (Map<Integer, String> value : values) {
				List<String> valueString = new ArrayList<>();
				for (Integer k : partition) {
					valueString.add(value.get(k));
				}
				valueStrings.add(StringUtils.join(valueString, ""));
			}
			output.println(StringUtils.join(partition, " ") + " (" + values.size() + "): " + 
					StringUtils.join(valueStrings, ", "));
		}
	}
	
	public List<List<Integer>> convertPartitionSet(Set<Set<Integer>> partitions) {
		List<List<Integer>> partitionsList = new ArrayList<>();
		for (Set<Integer> partition : partitions) {
			List<Integer> partitionList = new ArrayList<>(partition);
			Collections.sort(partitionList);
			partitionsList.add(partitionList);
		}
		Collections.sort(partitionsList, new Comparator<List<Integer>>() {
		    @Override
	        public int compare(List<Integer> l1, List<Integer> l2) {
	        	if (l1.isEmpty())
	        		return -1;
	        	if (l2.isEmpty())
	        		return 1;
	        	return l1.get(0) - l2.get(0);
	        }
		});
		return partitionsList;
	}
	
	public void exploreParitionSizes(int maxPartitionLength) {
		for (int i = 1; i <= maxPartitionLength; ++i) {
			explorePartitionSize(i);
		}
	}
	
	public void explorePartitionSize(int partitionLength) {
		CsvSet traces = this.original;
		List<Integer> keys = traces.idOrder();
		
		PrintStream output = this.output;
		
		output.println("Exploring partitions of size " + partitionLength);
		
		for (int i = 0, ii = keys.size() - (partitionLength - 1); i < ii; ++i) {
			List<Integer> subPartition = new ArrayList<Integer>();
			for (int j = 0; j < partitionLength; ++j) {
				subPartition.add(keys.get(i + j));
			}
			Set<Set<Integer>> newPartitioning = new HashSet<>();
			newPartitioning.add(new HashSet<>(subPartition));
			
			output.println("Request supports for: " + StringUtils.join(subPartition, " "));
			
			List<CsvSet> supports = new ArrayList<>(traces.getSupports(newPartitioning));
			
			output.println("Found " + supports.size() + " supports");
			
			for (int j = 0, jj = supports.size(); j < jj; ++j) {
				output.println("Info for support " + j);
				outputTracesInfo(supports.get(j));
			}
		}
	}
	
	public void exploreNumPartitions(int maxPartitions) {
		for (int i = 1; i <= maxPartitions; ++i) {
			exploreNumPartition(i);
		}
	}
	
	public void exploreNumPartition(int numPartitions) {
		CsvSet traces = this.original;
		List<Integer> keys = traces.idOrder();
		
		PrintStream output = this.output;
		
		output.println("Exploring partitions of " + numPartitions + " splits");
		List<List<Integer>> splits = this.getSplits(keys.size(), numPartitions);
		
		for (int i = 0, ii = splits.size(); i < ii; ++i) {
			List<Integer> split = splits.get(i);
			int splitIndex = 0;
			
			Set<Set<Integer>> newPartitioning = new HashSet<>();
			Set<Integer> currentPartition = new HashSet<>();
			
			for (int j = 0, jj = keys.size(); j < jj; ++j) {
			    currentPartition.add(keys.get(j));
			    
			    if (splitIndex >= split.size())
			    	break;
			    
			    if (j == split.get(splitIndex)) {
			    	newPartitioning.add(currentPartition);
			    	currentPartition = new HashSet<>();
			    	splitIndex++;
			    }
			}

			// output the partitions
			List<String> partitionStrings = new ArrayList<>(); 
			for (List<Integer> partition : this.convertPartitionSet(newPartitioning)) {
				partitionStrings.add(StringUtils.join(partition, " "));
			}
			output.println("Request supports for: " + StringUtils.join(partitionStrings, " | "));
			
			List<CsvSet> supports = new ArrayList<>(traces.getSupports(newPartitioning));
			
			output.println("Found " + supports.size() + " supports");
			
			for (int j = 0, jj = supports.size(); j < jj; ++j) {
				output.println("Info for support " + j);
				outputTracesInfo(supports.get(j));
			}
		}
	}
	
	protected List<List<Integer>> getSplits(int size, int splits) {
		List<Integer> set = new ArrayList<>();
		for (int i = 0; i < size - 1; ++i) {
			set.add(i);
		}
		List<List<Integer>> output = new ArrayList<>();
	    processLargerSubsets(set, new ArrayList<Integer>(), splits, 0, output);
	    return output;
	}

	protected void processLargerSubsets(List<Integer> set, List<Integer> subset, int subsetSize, int nextIndex, List<List<Integer>> output) {
	    if (subsetSize == subset.size()) {
	        output.add(subset);
	    } else {
	        for (int i = nextIndex; i < set.size(); i++) {
	        	List<Integer> newSubset = new ArrayList<>(subset);
	            newSubset.add(set.get(i));
	            processLargerSubsets(set, newSubset, subsetSize, i + 1, output);
	        }
	    }
	}

	public static void main(String[] args) {
		CsvImport im = new CsvImport("SODA.csv");	
		CsvSet traces = new CsvSet(im.traces, "base");
		CsvAnalysis csv = new CsvAnalysis(traces, System.out);
		csv.outputTracesInfo(csv.original);
		
		csv.exploreNumPartitions(3);
	}
}