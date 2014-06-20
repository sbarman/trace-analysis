package csv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kodkod.util.ints.IntBitSet;
import kodkod.util.ints.IntIterator;
import kodkod.util.ints.IntSet;
import kodkod.util.ints.Ints;

import entanglement.EntanglementDetector;
import entanglement.MaxSupportFinder;
import entanglement.TraceSet;
import entanglement.trace.Traces;

import misc.Tuple;

public class CsvSet implements TraceSet<Integer, String, CsvTrace> {
	
	public static Comparator<String> STR_INT = new Comparator<String>() {
        @Override
        public int compare(String s1, String s2) {
        	Integer i1 = Integer.parseInt(s1);
        	Integer i2 = Integer.parseInt(s2);
            return i1.compareTo(i2);
        }
    };
	
	protected final List<Tuple<Integer, ? extends List<String>>> idToDomain;

	public final int[] maxVals;
	public final Set<List<Integer>> simpleTraces;

	private Set<CsvTrace> traceData;

	public CsvSet(Set<CsvTrace> traceData) {
		idToDomain = new ArrayList<Tuple<Integer, ? extends List<String>>>();

		this.traceData = traceData;

		Set<Map<Integer, String>> traces = new HashSet<Map<Integer, String>>();
		for (CsvTrace trace : traceData) {
			traces.add(trace.mapping);
		}

		for (Map<Integer, String> trace : traces) {
			addToDomain(trace);
		}

		int traceLength = idToDomain.size();
		maxVals = new int[traceLength];
		for (int i = 0, ii = idToDomain.size(); i < ii; ++i) {
			maxVals[i] = idToDomain.get(i)._2.size();
		}

		simpleTraces = new HashSet<List<Integer>>();
		for (Map<Integer, String> trace : traces) {
			simpleTraces.add(getSimpleTrace(traceLength, trace));
		}
	}

	protected List<Integer> getSimpleTrace(int traceLength,
			Map<Integer, String> trace) {
		List<Integer> simpleTrace = new ArrayList<Integer>(traceLength);
		for (int i = 0; i < traceLength; ++i) {
			simpleTrace.add(0);
		}

		for (Integer id : trace.keySet()) {
			int index = indexOf(id);
			List<String> domain = idToDomain.get(index)._2;
			simpleTrace.set(index, domain.indexOf(trace.get(id)));
		}
		return simpleTrace;
	}

	public int indexOf(Integer id) {
		for (int i = 0, ii = idToDomain.size(); i < ii; ++i) {
			Integer _id = idToDomain.get(i)._1;
			if (_id.equals(id))
				return i;
		}
		return -1;
	}

	protected void addToDomain(Map<Integer, String> trace) {
		for (Integer key : trace.keySet()) {
			int keyIndex = indexOf(key);
			if (keyIndex == -1) {
				idToDomain.add(new Tuple<Integer, List<String>>(key,
						new ArrayList<String>()));
				keyIndex = idToDomain.size() - 1;
			}

			List<String> domain = idToDomain.get(keyIndex)._2;
			String value = trace.get(key);

			if (!domain.contains(value)) {
				domain.add(value);
			}
		}
	}

	@Override
	public String getName() {
		return "";
	}

	@Override
	public int size() {
		return traceData.size();
	}
	
	@Override
	public List<Integer> idOrder() {
		List<Integer> ids = new ArrayList<Integer>();
		for (Tuple<Integer, ? extends List<String>> idDomain : idToDomain) {
			ids.add(idDomain._1);
		}
		Collections.sort(ids);
		return ids;
	}

	@Override
	public Set<Set<Integer>> getEntangledPartitions() {

		Traces kodkodTraces = Traces.traces(maxVals, simpleTraces);
		List<IntSet> entangledSets = EntanglementDetector
				.entanglement(kodkodTraces);

		Set<Set<Integer>> partitions = new HashSet<Set<Integer>>();
		for (IntSet set : entangledSets) {
			Set<Integer> partition = new HashSet<Integer>();
			IntIterator it = set.iterator();
			while (it.hasNext()) {
				partition.add(idToDomain.get(it.next())._1);
			}
			partitions.add(partition);
		}

		return partitions;
	}

	@Override
	public Set<Map<Integer, String>> getValues(Set<Integer> partition) {
		HashSet<Map<Integer, String>> values = new HashSet<Map<Integer, String>>();
		for (CsvTrace t : traceData) {
			Map<Integer, String> trace = t.mapping;

			Map<Integer, String> subtrace = new HashMap<Integer, String>();
			for (Integer id : partition) {
				subtrace.put(id, trace.get(id));
			}
			values.add(subtrace);
		}

		return values;
	}

	@Override
	public Set<Map<Integer, String>> getTraces() {
		Set<Map<Integer, String>> mappings = new HashSet<Map<Integer, String>>();
		for (CsvTrace trace : traceData) {
			mappings.add(trace.mapping);
		}
		return mappings;
	}

	@Override
	public CsvTrace getTrace(Map<Integer, String> mapping) {
		for (CsvTrace trace : traceData) {
			Map<Integer, String> traceMap = trace.mapping;
			if (traceMap.equals(mapping)) {
				return trace;
			}
		}
		return null;
	}

	@Override
	public Set<CsvSet> getSupports(
			Set<Set<Integer>> subpartitions) {

		Set<Set<Integer>> oldPartitions = getEntangledPartitions();
		Set<Set<Integer>> newPartitions = new HashSet<Set<Integer>>();

		// calculate the intersections of all oldPartitions and the new
		// subpartitions
		for (Set<Integer> oldPartition : oldPartitions) {
			HashSet<Integer> partitionClone = new HashSet<Integer>(oldPartition);
			for (Set<Integer> subpartition : subpartitions) {
				HashSet<Integer> projection = new HashSet<Integer>();
				for (Integer id : subpartition) {
					if (oldPartition.contains(id)) {
						projection.add(id);
					}
				}
				if (!projection.isEmpty()) {
					newPartitions.add(projection);
					partitionClone.removeAll(projection);
				}
			}
			if (!partitionClone.isEmpty()) {
				newPartitions.add(partitionClone);
			}
		}

		// convert the newPartitions to a List<IntSet>
		List<IntSet> intSetPartitions = new ArrayList<IntSet>();

		for (Set<Integer> partition : newPartitions) {
			if (partition.size() == 1) {
				intSetPartitions.add(Ints.singleton(indexOf(partition
						.iterator().next())));
			} else {
				int maxValue = -1;
				List<Integer> indexes = new ArrayList<Integer>();
				for (Integer id : partition) {
					int index = indexOf(id);
					indexes.add(index);
					if (index > maxValue) {
						maxValue = index;
					}
				}
				IntSet partitionIndexes = new IntBitSet(maxValue + 1);
				for (Integer index : indexes) {
					partitionIndexes.add(index);
				}
				intSetPartitions.add(partitionIndexes);
			}
		}

		Traces kodkodTraces = Traces.traces(maxVals, simpleTraces);

		Set<CsvSet> set = new HashSet<CsvSet>();

		for (Iterator<Traces> supports = MaxSupportFinder.findMaximalSupports(
				kodkodTraces, intSetPartitions); supports.hasNext();) {
			Traces support = supports.next();

			Set<CsvTrace> traces = new HashSet<CsvTrace>();
			for (Iterator<entanglement.trace.Trace> it = support.iterator(); it
					.hasNext();) {
				entanglement.trace.Trace t = it.next();
				Map<Integer, String> mapping = new HashMap<Integer, String>();
				for (int j = 0; j < t.length(); j++) {
					Tuple<Integer, ? extends List<String>> idDomain = idToDomain
							.get(j);
					mapping.put(idDomain._1, idDomain._2.get(t.get(j)));
				}
				traces.add(getTrace(mapping));
			}
			set.add(new CsvSet(traces));
		}
		return set;
	}
}
